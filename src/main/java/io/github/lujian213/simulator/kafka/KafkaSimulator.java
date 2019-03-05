package io.github.lujian213.simulator.kafka;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.SimScript;
import io.github.lujian213.simulator.SimSesseionLessSimulator;
import io.github.lujian213.simulator.SimSimulator;
import io.github.lujian213.simulator.kafka.KafkaBroker.KafkaMessageListener;
import io.github.lujian213.simulator.kafka.KafkaBroker.KafkaSubscriber;
import io.github.lujian213.simulator.util.ReqRespConvertor;
import io.github.lujian213.simulator.util.SimLogger;
import io.github.lujian213.simulator.util.SimUtils;

public class KafkaSimulator extends SimSimulator implements SimSesseionLessSimulator {
	public static final String PROP_NAME_ENDPOINT_BROKER = "simulator.kafka.endpoint.broker";
	public static final String PROP_NAME_ENDPOINT_NAME = "simulator.kafka.endpoint.name";
	public static final String PROP_NAME_ENDPOINT_TYPE = "simulator.kafka.endpoint.type";

	public static final String HEADER_NAME_CHANNEL = "_Channel";
	public static final String HEADER_NAME_MESSGAE_KEY = "_Message.key";
	public static final String HEADER_NAME_MESSGAE_TOPIC = "_Message.topic";

	
	public static final String ENDPOINT_TYPE_PUB = "Pub";
	public static final String ENDPOINT_TYPE_SUB = "Sub";

	private Map<String, KafkaBroker> brokerMap = new HashMap<>();
	private boolean proxy;
	private String proxyURL;
	private ReqRespConvertor convertor;

	
	public class SimMessageListener implements KafkaMessageListener {
		private String unifiedEndpointName;
		private SimScript script;
		
		public SimMessageListener(SimScript script, String unifiedEndpointName) {
			this.script = script;
			this.unifiedEndpointName = unifiedEndpointName;
		}
		
		public void onMessage(ConsumerRecord<?, ?> record) {
			SimUtils.setThreadContext(script);
			KafkaSimRequest request = null;
			try {
				request = new KafkaSimRequest(script, record, unifiedEndpointName, convertor);
				SimLogger.getLogger().info("incoming request from [" + unifiedEndpointName + "]: [" + request.getTopLine() + "]\n" + request.getBody());
			} catch (Exception e) {
				SimLogger.getLogger().error("error when create SimRequest", e);
			}
			if (request != null) {
				List<SimResponse> respList = new ArrayList<>();
				try {
					respList = script.genResponse(request);
				} catch (Exception e) {
					if (proxy) {
						try {
							SimResponse resp = SimUtils.doKafkaProxy(proxyURL, request);
							request.fillResponse(resp);
							respList.add(resp);
						} catch (IOException e1) {
							SimLogger.getLogger().error("proxy error", e1);
						}
					} else {
						SimLogger.getLogger().error("match and fill error", e);
					}
				}
				castToSimulatorListener().onHandleMessage(getName(), request, respList, !respList.isEmpty());
			}
		}
		
	}
	
	
	public KafkaSimulator(SimScript script) throws IOException {
		super(script);
		convertor = SimUtils.createMessageConvertor(script, new DefaultKafkaReqRespConvertor());
	}
	
	protected KafkaSimulator() {
	}

	protected List<String> prepare() throws IOException {
		List<SimScript> endPointScripts = new ArrayList<>();
		List<String> ret = new ArrayList<>();
		
		for (Map.Entry<String, SimScript> entry: this.script.getSubScripts().entrySet()) {
			SimScript subScript = entry.getValue();
			SimLogger.getLogger().info("handle folder [" + entry.getKey() + "]");
			if (subScript.getProperty(KafkaBroker.PROP_NAME_BROKER_NAME) != null) {
				KafkaBroker broker = new KafkaBroker(subScript, convertor);
				brokerMap.put(broker.getName(), broker);
			} else if (subScript.getProperty(PROP_NAME_ENDPOINT_NAME) != null) {
				endPointScripts.add(subScript);
			} else {
				SimLogger.getLogger().info("skip .. folder [" + entry.getKey() + "]");
			}
		}
		
		for (SimScript endPointScript: endPointScripts) {
			String endPointName = handleEndpoint(endPointScript);
			if (endPointName != null) {
				ret.add(endPointName);
			}
		}
		return ret;
	}

	protected String handleEndpoint(SimScript script) {
		String ret = null;
		String endpointType = script.getMandatoryProperty(PROP_NAME_ENDPOINT_TYPE, "no endpoint type defined");
		String brokerName = script.getMandatoryProperty(PROP_NAME_ENDPOINT_BROKER, "no endpoint broker defined");
		KafkaBroker broker = brokerMap.get(brokerName);
		if (broker == null) {
			throw new RuntimeException("can not find broker [" + brokerName + "]");
		}
		boolean handled = false;
		if (endpointType.equals(ENDPOINT_TYPE_PUB)) {
			broker.addPublisher(broker.new KafkaPublisher(script));
			handled = true;
		} 
		if (endpointType.equals(ENDPOINT_TYPE_SUB)) {
			KafkaSubscriber sub = broker.new KafkaSubscriber(script);
			sub.setListener(new SimMessageListener(script, sub.getName()));
			broker.addSubscriber(sub);
			handled = true;
			ret = sub.getName();
		}
		if (!handled) {
			throw new RuntimeException("unsupported endpoint type [" + endpointType + "]");
		}
		return ret;
	}

	@Override
	protected void doStart() throws IOException {
		boolean success = false;
		try {
			List<String> subDestNameList = prepare();
			for (KafkaBroker broker: brokerMap.values()) {
				broker.start();
			}
			this.runningURL = SimUtils.concatContent(subDestNameList, ",");
			success = true;
		} finally {
			if (!success) {
				stop();
			}
		}
	}

	@Override
	public void stop() {
		super.stop();
		SimLogger.getLogger().info("about to stop ...");
		for (KafkaBroker broker: brokerMap.values()) {
			broker.stop();
		}
		SimLogger.getLogger().info("stopped");
		this.running = false;
		this.runningURL = null;
	}

	@Override
	protected void init() throws IOException {
		super.init();
	}
	
	@Override
	public String getType() {
		return "Kafka";
	}

	@Override
	public void fillResponse(SimResponse response) throws IOException {
		Map<String, Object> headers = response.getHeaders();
		String channel = (String) headers.remove(HEADER_NAME_CHANNEL);
		if (channel == null) {
			throw new IOException("no Channel defined to send response to");
		}
		String brokerName = SimUtils.getBrokerName(channel);
		KafkaBroker broker = brokerMap.get(brokerName);
		if (broker == null) {
			throw new IOException("no such broker [" + brokerName + "] exists");	
		}
		broker.sendMessage(channel, response);
		SimLogger.getLogger().info("Use channel [" + channel + "] to send out message");
	}
}
