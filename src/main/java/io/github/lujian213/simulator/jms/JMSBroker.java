package io.github.lujian213.simulator.jms;

import java.io.IOException;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.TopicConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import io.github.lujian213.simulator.SimScript;
import io.github.lujian213.simulator.util.SimLogger;
import static io.github.lujian213.simulator.jms.JMSSimulatorConstants.*;

public class JMSBroker {
	public static final String DESTINATION_TYPE_TOPIC = "Topic";
	public static final String DESTINATION_TYPE_QUEUE = "Queue";

	public static class SimMessageProducer  {
		private MessageProducer producer;
		private Session session;
		
		public SimMessageProducer(Session session, MessageProducer producer) {
			this.session = session;
			this.producer = producer;
		}
		
		public Session getSession() {
			return session;
		}
		
		public void send(Message message) throws IOException {
			try {
				producer.send(message);
			} catch (JMSException e) {
				throw new IOException(e);
			}
		}
		
		@Override
		public String toString() {
			try {
				return producer.getDestination().toString();
			} catch (JMSException e) {
				return "";
			}
		}
	}

	public static class SimMessageConsumer  {
		private MessageConsumer consumer;
		private Session session;
		private Destination dest;
		public SimMessageConsumer(Session session, Destination dest, MessageConsumer consumer) {
			this.session = session;
			this.dest = dest;
			this.consumer = consumer;
		}
		public MessageConsumer getConsumer() {
			return consumer;
		}
		public Session getSession() {
			return session;
		}
		public Destination getDest() {
			return dest;
		}
	}

	private Context context;
	private Connection tc;
	private Connection qc;
	private Session ts;
	private Session qs;
	private SimScript script;

	public JMSBroker(SimScript script) {
		this.script = script;
	}
	
	public String getName() {
		return script.getProperty(PROP_NAME_BROKER_NAME);
	}
	
	public void start() throws IOException {
		try {
			prepare();
			if (tc != null) {
				tc.start();
			}
			if (qc != null) {
				qc.start();
			}
		} catch (JMSException e) {
			throw new IOException(e);
		}
	}
	
	protected void prepare() {
		Properties props = script.getConfigAsProperties();
        try {
        	// create the JNDI initial context.
			context = new InitialContext(props);
            // look up the ConnectionFactory
			String tcfName = script.getProperty(PROP_NAME_TOPIC_FACTORY_NAME);
			if (tcfName != null) {
				TopicConnectionFactory tcf = (TopicConnectionFactory) context.lookup(script.getProperty(PROP_NAME_TOPIC_FACTORY_NAME));
				if (script.getProperty(PROP_NAME_FACTORY_USERNAME) != null) {
					tc = tcf.createConnection(script.getProperty(PROP_NAME_FACTORY_USERNAME), script.getProperty(PROP_NAME_FACTORY_PASSWORD));
				} else {
					tc = tcf.createConnection();
				}
				ts = tc.createSession(false, Session.AUTO_ACKNOWLEDGE);
			}
			String qcfName = script.getProperty(PROP_NAME_QUEUE_FACTORY_NAME);
			if (qcfName != null) {
				QueueConnectionFactory qcf = (QueueConnectionFactory) context.lookup(script.getProperty(PROP_NAME_QUEUE_FACTORY_NAME));
				if (script.getProperty(PROP_NAME_FACTORY_USERNAME) != null) {
					qc = qcf.createConnection(script.getProperty(PROP_NAME_FACTORY_USERNAME), script.getProperty(PROP_NAME_FACTORY_PASSWORD));
				} else {
					qc = qcf.createConnection();
				}
				qs = qc.createSession(false, Session.AUTO_ACKNOWLEDGE);
			}
        } catch (NamingException | JMSException e) {
			throw new RuntimeException(e);
		} finally {
		}
	}
	
	public void stop() {
		if (context != null) {
			try {
				context.close();
			} catch (NamingException e) {
			}
		}
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
	}

	public Destination findDestination(String destName) throws NamingException {
		return (Destination) context.lookup(destName);
	}
	
	public Destination createTempQueue() throws JMSException {
		return qs.createTemporaryQueue();
	}
	
	public Destination createTempTopic() throws JMSException {
		return ts.createTemporaryTopic();
	}

	public SimMessageProducer createProducer(String destName, String destType) throws JMSException, NamingException {
    	Session session = getSession(destType);
		Destination dest = (Destination) context.lookup(destName);
		if (dest == null) {
			throw new RuntimeException("can not find destination [" + destName + "]");
		}
    	SimMessageProducer ret = new SimMessageProducer(session, session.createProducer(dest));
    	SimLogger.getLogger().info("Producer created for [" + dest + "]");
    	return ret;
    }
    
    protected SimMessageConsumer createConsumer(SimScript script, String destName, String destType) throws JMSException, NamingException {
    	Session session = getSession(destType);
		Destination dest = (Destination) context.lookup(destName);
		if (dest == null) {
			throw new RuntimeException("can not find destination [" + destName + "]");
		}
		MessageConsumer consumer = session.createConsumer(dest);
		SimMessageConsumer ret = new SimMessageConsumer(session, dest, consumer);
		SimLogger.getLogger().info("Consumer created for [" + dest + "]");
		return ret;
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

}
