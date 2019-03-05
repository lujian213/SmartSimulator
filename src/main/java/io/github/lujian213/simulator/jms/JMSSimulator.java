package io.github.lujian213.simulator.jms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.naming.NamingException;

import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.SimScript;
import io.github.lujian213.simulator.SimSesseionLessSimulator;
import io.github.lujian213.simulator.SimSimulator;
import io.github.lujian213.simulator.jms.JMSBroker.SimMessageConsumer;
import io.github.lujian213.simulator.jms.JMSBroker.SimMessageProducer;
import io.github.lujian213.simulator.util.ReqRespConvertor;
import io.github.lujian213.simulator.util.SimLogger;
import io.github.lujian213.simulator.util.SimUtils;

public class JMSSimulator extends SimSimulator implements SimSesseionLessSimulator {
	public static final String PROP_NAME_DESTINATION_TYPE = "simulator.jms.destination.type";
	public static final String PROP_NAME_DESTINATION_BROKER = "simulator.jms.destination.broker";
	public static final String PROP_NAME_DESTINATION_NAME = "simulator.jms.destination.name";
	public static final String PROP_NAME_CLIENT_TYPE = "simulator.jms.client.type";

	public static final String HEADER_NAME_CHANNEL = "_Channel";
	public static final String HEADER_NAME_MESSAGE_TYPE = "_Message.Type";
	public static final String HEADER_NAME_PRODUCER = "_Producer";
	public static final String MESSAGE_TYPE_TEXT = "Text";
	public static final String MESSAGE_TYPE_BYTES = "Bytes";
	public static final String MESSAGE_TYPE_OBJECT = "Object";

	
	public static final String CLIENT_TYPE_PUB = "Pub";
	public static final String CLIENT_TYPE_SUB = "Sub";

	private Map<String, SimMessageProducer> producerMap;
	private Map<String, JMSBroker> brokerMap = new HashMap<>();
	private boolean proxy;
	private String proxyURL;
	private ReqRespConvertor convertor;

	
	public class SimMessageListener implements MessageListener {
		private SimScript script;
		private String unifiedDestName;
		private Session session;
		private ReqRespConvertor convertor;
		
		public SimMessageListener(SimScript script, Session session, String unifiedDestName, ReqRespConvertor convertor) {
			this.script = script;
			this.unifiedDestName = unifiedDestName;
			this.session = session;
			this.convertor = convertor;
		}
		
		@Override
		public void onMessage(Message message) {
			SimUtils.setThreadContext(script);
			JMSSimRequest request = null;
			try {
				request = new JMSSimRequest(script, message, session, unifiedDestName, convertor);
				SimLogger.getLogger().info("incoming request from [" + unifiedDestName + "]: [" + request.getTopLine() + "]\n" + request.getBody());
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
							SimResponse resp = SimUtils.doJMSProxy(proxyURL, request);
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
	
	
	public JMSSimulator(SimScript script) throws IOException {
		super(script);
		convertor = SimUtils.createMessageConvertor(script, new DefaultJMSReqRespConvertor());
	}
	
	protected JMSSimulator() {
	}

	protected List<String> prepare() throws IOException {
		producerMap = new HashMap<>();
		List<SimScript> destinationScripts = new ArrayList<>();
		List<String> ret = new ArrayList<>();
		
		for (Map.Entry<String, SimScript> entry: this.script.getSubScripts().entrySet()) {
			SimScript subScript = entry.getValue();
			SimLogger.getLogger().info("handle folder [" + entry.getKey() + "]");
			if (subScript.getProperty(JMSBroker.PROP_NAME_BROKER_NAME) != null) {
				JMSBroker broker = new JMSBroker(subScript);
				brokerMap.put(broker.getName(), broker);
				broker.start();
			} else if (subScript.getProperty(PROP_NAME_DESTINATION_NAME) != null) {
				destinationScripts.add(subScript);
			} else {
				SimLogger.getLogger().info("skip .. folder [" + entry.getKey() + "]");
			}
		}
		
		for (SimScript destinationScript: destinationScripts) {
			String subDestName = handleDestination(destinationScript);
			if (subDestName != null) {
				ret.add(subDestName);
			}
		}
		return ret;
	}

	protected String handleDestination(SimScript script) {
		String ret = null;
		try {
			String destName = script.getMandatoryProperty(PROP_NAME_DESTINATION_NAME, "no destination name defined");
			String destType = script.getMandatoryProperty(PROP_NAME_DESTINATION_TYPE, "no destination type defined");
			String clientType = script.getMandatoryProperty(PROP_NAME_CLIENT_TYPE, "no destination client type defined");
			String brokerName = script.getMandatoryProperty(PROP_NAME_DESTINATION_BROKER, "no destination broker defined");
			JMSBroker broker = brokerMap.get(brokerName);
			if (broker == null) {
				throw new RuntimeException("can not find broker [" + brokerName + "]");
			}
			boolean handled = false;
			if (clientType.contains(CLIENT_TYPE_PUB)) {
				SimMessageProducer simProducer = broker.createProducer(destName, destType);
				producerMap.put(SimUtils.createUnifiedName(destName, brokerName), simProducer);
				handled = true;
			} 
			if (clientType.contains(CLIENT_TYPE_SUB)) {
				SimMessageConsumer simConsumer = broker.createConsumer(script, destName, destType);
				simConsumer.getConsumer().setMessageListener(new SimMessageListener(script, simConsumer.getSession(), SimUtils.createUnifiedName(destName, brokerName), convertor));
				handled = true;
				ret = SimUtils.createUnifiedName(destName, brokerName);
			}
			if (!handled) {
				throw new RuntimeException("unsupported client type [" + clientType + "]");
			}
			return ret;
		} catch (JMSException | NamingException e) {
			throw new RuntimeException("handle destination error", e);
		}

	}

	@Override
	protected void doStart() throws IOException {
		boolean success = false;
		try {
			List<String> subDestNameList = prepare();
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
		for (JMSBroker broker: brokerMap.values()) {
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
		return "JMS";
	}

	@Override
	public void fillResponse(SimResponse response) throws IOException {
		try {
			Map<String, Object> headers = response.getHeaders();
			SimMessageProducer producer = (SimMessageProducer) headers.remove(HEADER_NAME_PRODUCER);
			String channel = (String) headers.remove(HEADER_NAME_CHANNEL);
			if (producer == null) {
				producer = producerMap.get(channel);
			}
			if (producer == null) {
				throw new IOException("can not find producer for channel [" + channel + "]");
			}
			String messageType = (String) headers.remove(HEADER_NAME_MESSAGE_TYPE);
			Message respMsg = createMessage(producer.getSession(), messageType);
			for (Map.Entry<String, Object> entry : headers.entrySet()) {
				respMsg.setObjectProperty(entry.getKey(), entry.getValue());
			}
			convertor.fillRawResponse(respMsg, response);
			SimLogger.getLogger().info("Use producer [" + producer.getProducer() + "] to send out message");
			producer.getProducer().send(respMsg);
		} catch (JMSException e) {
			throw new IOException(e);
		}
	}
	
	protected static Message createMessage(Session session, String type) throws JMSException {
		if (MESSAGE_TYPE_TEXT.equals(type) || type == null) {
			return session.createTextMessage();
		} else if (MESSAGE_TYPE_BYTES.equals(type)) {
			return session.createBytesMessage();
		} else if (MESSAGE_TYPE_OBJECT.equals(type)) {
			return session.createObjectMessage();
		} else {
			throw new JMSException("can't support message type [" + type + "]");
		}
	}


}
