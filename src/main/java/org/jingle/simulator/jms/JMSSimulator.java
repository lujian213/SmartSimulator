package org.jingle.simulator.jms;

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

import org.jingle.simulator.SimResponse;
import org.jingle.simulator.SimScript;
import org.jingle.simulator.SimSimulator;
import org.jingle.simulator.jms.JMSBroker.SimMessageConsumer;
import org.jingle.simulator.jms.JMSBroker.SimMessageProducer;
import org.jingle.simulator.util.ReqRespConvertor;
import org.jingle.simulator.util.SimLogger;
import org.jingle.simulator.util.SimUtils;

public class JMSSimulator extends SimSimulator {
	public static final String PROP_NAME_DESTINATION_TYPE = "simulator.jms.destination.type";
	public static final String PROP_NAME_DESTINATION_BROKER = "simulator.jms.destination.broker";
	public static final String PROP_NAME_DESTINATION_NAME = "simulator.jms.destination.name";
	public static final String PROP_NAME_CLIENT_TYPE = "simulator.jms.client.type";
	
	public static final String CLIENT_TYPE_PUB = "Pub";
	public static final String CLIENT_TYPE_SUB = "Sub";

	private Map<String, SimMessageProducer> producerMap;
	private Map<String, JMSBroker> brokerMap = new HashMap<>();
	private boolean proxy;
	private String proxyURL;

	
	public class SimMessageListener implements MessageListener {
		private SimScript script;
		private String unifiedDestName;
		private Session session;
		private ReqRespConvertor convertor;
		
		public SimMessageListener(SimScript script, Session session, String unifiedDestName) {
			this.script = script;
			this.unifiedDestName = unifiedDestName;
			this.session = session;
			this.convertor = SimUtils.createMessageConvertor(script, new DefaultJMSReqRespConvertor());
		}
		
		@Override
		public void onMessage(Message message) {
			SimLogger.setLogger(script.getLogger());
			JMSSimRequest request = null;
			try {
				request = new JMSSimRequest(message, session, unifiedDestName, producerMap, brokerMap, convertor);
				SimLogger.getLogger().info("incoming request from [" + unifiedDestName + "]: [" + request.getTopLine() + "]\n" + request.getBody());
			} catch (Exception e) {
				SimLogger.getLogger().error("error when create SimRequest", e);
			}
			try {
				if (request != null) {
					script.genResponse(request);
				}
			} catch (Exception e) {
				if (proxy) {
					try {
						SimResponse resp = SimUtils.doJMSProxy(proxyURL, request);
						request.fillResponse(resp);
					} catch (IOException e1) {
						SimLogger.getLogger().error("proxy error", e1);
					}
				} else {
					SimLogger.getLogger().error("match and fill error", e);
				}
			}
		}
		
	}
	
	
	public JMSSimulator(SimScript script) throws IOException {
		super(script);
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
				producerMap.put(getUnifiedDestName(brokerName, destName), simProducer);
				handled = true;
			} 
			if (clientType.contains(CLIENT_TYPE_SUB)) {
				SimMessageConsumer simConsumer = broker.createConsumer(script, destName, destType);
				simConsumer.getConsumer().setMessageListener(new SimMessageListener(script, simConsumer.getSession(), getUnifiedDestName(brokerName, destName)));
				handled = true;
				ret = getUnifiedDestName(brokerName, destName);
			}
			if (!handled) {
				throw new RuntimeException("unsupported client type [" + clientType + "]");
			}
			return ret;
		} catch (JMSException | NamingException e) {
			throw new RuntimeException("handle destination error", e);
		}

	}

	public static String getUnifiedDestName(String brokerName, String destName) {
		return destName + "@" + brokerName;
	}
	
	public static String getBrokerName(String unifiedDestName) {
		String[] parts = unifiedDestName.split("@");
		return parts[1];
	}

	@Override
	public void start() throws IOException {
		boolean success = false;
		try {
			List<String> subDestNameList = prepare();
			this.running = true;
			this.runningURL = SimUtils.concatContent(subDestNameList);
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
}
