package org.jingle.simulator.jms;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.TopicConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jingle.simulator.SimRequest;
import org.jingle.simulator.SimScript;
import org.jingle.simulator.SimSimulator;
import org.jingle.simulator.util.SimLogger;

public class JMSSimulator extends SimSimulator {
	public static final String PROP_NAME_TOPIC_FACTORY_NAME = "simulator.jms.topicconnectionfactory";
	public static final String PROP_NAME_QUEUE_FACTORY_NAME = "simulator.jms.queueconnectionfactory";
	public static final String PROP_NAME_DESTINATION_TYPE = "simulator.jms.destination.type";
	public static final String PROP_NAME_DESTINATION_NAME = "simulator.jms.destination.name";
	public static final String PROP_NAME_CLIENT_TYPE = "simulator.jms.client.type";
	
	public static final String DESTINATION_TYPE_TOPIC = "Topic";
	public static final String DESTINATION_TYPE_QUEUE = "Queue";
	public static final String CLIENT_TYPE_PUB = "Pub";
	public static final String CLIENT_TYPE_SUB = "Sub";

	protected Context context;
	protected Connection tc;
	protected Connection qc;
	protected Session ts;
	protected Session qs;
	protected Map<String, SimMessageProducer> producerMap;
	
	public class SimMessageListener implements MessageListener {
		private SimScript script;
		private String destName;
		private Session session;
		
		public SimMessageListener(SimScript script, Session session, String destName) {
			this.script = script;
			this.destName = destName;
			this.session = session;
		}
		
		@Override
		public void onMessage(Message message) {
			SimLogger.setLogger(script.getLogger());
			try {
				SimRequest request = new JMSSimRequest(message, session, destName, producerMap);
				SimLogger.getLogger().info("incoming request from [" + destName + "]: [" + request.getTopLine() + "]\n" + request.getBody());
				script.genResponse(request);
			} catch (Exception e) {
				SimLogger.getLogger().error("", e);
			}
		}
		
	}
	
	public static class SimMessageProducer  {
		private MessageProducer producer;
		private Session session;
		public SimMessageProducer(Session session, MessageProducer producer) {
			this.session = session;
			this.producer = producer;
		}
		public MessageProducer getProducer() {
			return producer;
		}
		public Session getSession() {
			return session;
		}
	}
	
	public JMSSimulator(SimScript script) throws IOException {
		super(script);
	}
	
	protected JMSSimulator() {
	}

	protected void prepare() {
		producerMap = new HashMap<>();
		Properties props = script.getProps();
        try {
        	// create the JNDI initial context.
			context = new InitialContext(props);
            // look up the ConnectionFactory
			TopicConnectionFactory tcf = (TopicConnectionFactory) context.lookup(props.getProperty(PROP_NAME_TOPIC_FACTORY_NAME));
			QueueConnectionFactory qcf = (QueueConnectionFactory) context.lookup(props.getProperty(PROP_NAME_QUEUE_FACTORY_NAME));
			 // create the connection
			tc = tcf.createConnection();
			qc = qcf.createConnection();
            // create the session
            ts = tc.createSession(false, Session.AUTO_ACKNOWLEDGE);
            qs = qc.createSession(false, Session.AUTO_ACKNOWLEDGE);

    		for (Map.Entry<String, SimScript> entry: this.script.getSubScripts().entrySet()) {
    			SimScript destinationScript = entry.getValue();
    			SimLogger.getLogger().info("handle folder [" + entry.getKey() + "]");
    			handleDestination(destinationScript);
    		}
        } catch (NamingException | JMSException e) {
			throw new RuntimeException(e);
		} finally {
			if (context != null) {
				try {
					context.close();
				} catch (NamingException e) {
				}
			}
		}
	}

	protected void handleDestination(SimScript script) {
		try {
			Properties subProps = script.getProps();
			String destName = subProps.getProperty(PROP_NAME_DESTINATION_NAME);
			if (destName == null) {
				throw new RuntimeException("no destination name defined");
			}
			String destType = subProps.getProperty(PROP_NAME_DESTINATION_TYPE);
			String clientType = subProps.getProperty(PROP_NAME_CLIENT_TYPE);
			Destination dest = (Destination) context.lookup(destName);
			if (dest == null) {
				throw new RuntimeException("can not find topic [" + destName + "]");
			}
			boolean handled = false;
			if (clientType != null && clientType.contains(CLIENT_TYPE_PUB)) {
				createProducer(destName, dest, destType);
				handled = true;
			} 
			if (clientType != null && clientType.contains(CLIENT_TYPE_SUB)) {
				createConsumer(script, destName, dest, destType);
				handled = true;
			}
			if (!handled) {
				throw new RuntimeException("unsupported client type [" + clientType + "]");
			}
		} catch (JMSException | NamingException e) {
			throw new RuntimeException("handle destination error", e);
		}

	}

    protected void createProducer(String destName, Destination dest, String destType) throws JMSException {
    	Session session = getSession(destType);
    	producerMap.put(destName, new SimMessageProducer(session, session.createProducer(dest)));
    	SimLogger.getLogger().info("Producer created for [" + dest + "]");
    }
    
    protected void createConsumer(SimScript script, String destName, Destination dest, String destType) throws JMSException {
    	Session session = getSession(destType);
		MessageConsumer consumer = session.createConsumer(dest);
		consumer.setMessageListener(new SimMessageListener(script, session, destName));
		SimLogger.getLogger().info("Consumer created for [" + dest + "]");
    }
    
    protected Session getSession(String destType) {
    	if (DESTINATION_TYPE_TOPIC.equals(destType)) {
    		return ts;
		} else if (DESTINATION_TYPE_QUEUE.equals(destType)) {
			return qs;
		} else {
			throw new RuntimeException("unsupported destination type [" + destType + "]");
		}
    }

	@Override
	public void start() throws IOException {
		try {
			prepare();
			tc.start();
			qc.start();
		} catch (JMSException e) {
			throw new IOException(e);
		}
		this.running = true;
	}

	@Override
	public void stop() {
		SimLogger.getLogger().info("about to stop ...");

		if (tc != null) {
			try {
				tc.close();
			} catch (JMSException e) {
			}
		}
		if (qc != null) {
			try {
				qc.close();
			} catch (JMSException e) {
			}
		}
		SimLogger.getLogger().info("stopped");
		this.running = false;
	}

	@Override
	protected void init() throws IOException {
	}
}
