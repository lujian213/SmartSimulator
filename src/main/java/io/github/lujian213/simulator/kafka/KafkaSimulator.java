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
import static io.github.lujian213.simulator.kafka.KafkaSimulatorConstants.*;

public class KafkaSimulator extends SimSimulator implements SimSesseionLessSimulator {
	public static final String ENDPOINT_TYPE_PUB = "Pub";
	public static final String ENDPOINT_TYPE_SUB = "Sub";

	private Map<String, KafkaBroker> brokerMap = new HashMap<>();
	private ReqRespConvertor convertor;

	
	public class SimMessageListener implements KafkaMessageListener {
		private String unifiedEndpointName;
		private SimScript script;
		private boolean proxy;
		private String proxyChannel;
		
		public SimMessageListener(SimScript script, String unifiedEndpointName) {
			this.script = script;
			this.unifiedEndpointName = unifiedEndpointName;
			this.proxy = script.getBooleanProperty(PROP_NAME_ENDPOINT_PROXY, false);
			if (proxy) {
				proxyChannel = script.getMandatoryProperty(PROP_NAME_ENDPOINT_PROXY_CHANNEL, "no proxy channel defined");
			}

		}
		
		public void onMessage(ConsumerRecord<?, ?> record) {
			SimUtils.setThreadContext(script);
			KafkaSimRequest request = null;
			try {
				request = new KafkaSimRequest(script, record, unifiedEndpointName, convertor);
				SimUtils.logIncomingMessage(unifiedEndpointName, KafkaSimulator.this.getName(), request);
			} catch (Exception e) {
				SimLogger.getLogger().error("error when create SimRequest", e);
			}
			if (request != null) {
				List<SimResponse> respList = new ArrayList<>();
				try {
					respList = script.genResponse(request);
				} catch (Exception e) {
					SimLogger.getLogger().info(e.toString() + "try proxy if proxy is setup");
					if (proxy) {
						try {
							String brokerName = SimUtils.getBrokerName(proxyChannel);
							KafkaBroker broker = brokerMap.get(brokerName);
							if (broker == null) {
								throw new IOException("no such broker [" + brokerName + "] exists");	
							}
							broker.sendMessage(proxyChannel, record);
							respList.add(new SimResponse("Unknown due to proxy mechanism"));
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
			if (subScript.getProperty(PROP_NAME_BROKER_NAME) != null) {
				KafkaBroker broker = new KafkaBroker(subScript, convertor);
				brokerMap.put(broker.getName(), broker);
			} else if (subScript.getProperty(PROP_NAME_ENDPOINT_NAME) != null) {
				endPointScripts.add(subScript);
			} else {
				SimLogger.getLogger().info("skip folder [" + entry.getKey() + "]");
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
		List<String> subDestNameList = prepare();
		for (KafkaBroker broker: brokerMap.values()) {
			broker.start();
		}
		this.runningURL = SimUtils.concatContent(subDestNameList, ",");
	}

	@Override
	protected void doStop() {
		for (KafkaBroker broker: brokerMap.values()) {
			broker.stop();
		}
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
