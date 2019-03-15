package io.github.lujian213.simulator.jms;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import io.github.lujian213.simulator.AbstractSimRequest;
import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.SimScript;
import io.github.lujian213.simulator.jms.JMSBroker.SimMessageProducer;
import io.github.lujian213.simulator.jms.JMSSimulator.ProducerFactory;
import io.github.lujian213.simulator.util.ReqRespConvertor;
import io.github.lujian213.simulator.util.SimLogger;
import io.github.lujian213.simulator.util.SimUtils;
import static io.github.lujian213.simulator.SimSimulatorConstants.*;

public class JMSSimRequest extends AbstractSimRequest {
	public static final String CHANNEL_NAME_JMSREPLYTO = "JMSReplyTo";
	
	private static final String[] JMS_HEADER_NAMES = {"JMSCorrelationID", "JMSDeliveryMode", "JMSDeliveryTime", "JMSDestination", "JMSExpiration", "JMSMessageID", "JMSPriority", "JMSRedelivered", "JMSReplyTo", "JMSTimestamp", "JMSType"};
	private static final String HEADER_LINE_FORMAT = "%s: %s";
	private Message message;
	private String unifiedDestName;
	private String topLine;
	private Map<String, Object> headers = new HashMap<>();
	private String body;
	private Session session;
	private ReqRespConvertor convertor;
	private SimScript script;
	private ProducerFactory producerFactory;
	
	public JMSSimRequest(SimScript script, Message message, Session session, ProducerFactory producerFactory, String unifiedDestName, ReqRespConvertor convertor) throws IOException {
		this.script = script;
		this.message = message;
		this.unifiedDestName = unifiedDestName;
		this.session = session;
		this.convertor = convertor;
		this.producerFactory = producerFactory;
		if (message instanceof TextMessage) {
			this.topLine = "TextMessage";
		} else if (message instanceof BytesMessage) {
			this.topLine = "BytesMessage";
		} else if (message instanceof ObjectMessage) {
			this.topLine = "ObjectMessage";
		} else {
			this.topLine = "OtherMessage";
		}
		genHeaders();
		genBody();
	}
	
	protected JMSSimRequest() {
		
	}
	
	@Override
	public ReqRespConvertor getReqRespConvertor() {
		return this.convertor;
	}
	
	public Message getMessage() {
		return this.message;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.topLine).append("\n");
		for (Map.Entry<String, Object> entry: headers.entrySet()) {
			sb.append(this.getHeaderLine(entry.getKey())).append("\n");
		}
		sb.append("\n");
		if (body != null) {
			sb.append(body);
		}
		sb.append("\n");
		return sb.toString();
	}

	protected void genHeaders() throws IOException {
		try {
			@SuppressWarnings("unchecked")
            Enumeration<String> propNames = message.getPropertyNames();
			while (propNames.hasMoreElements()) {
				String name = (String) propNames.nextElement();
				Object value = message.getObjectProperty(name);
				headers.put(name, value);
			}
			headers.putAll(getMessageHeaderAsMap(message));
		} catch (JMSException e) {
			throw new IOException(e);
		}
	}
	
	protected Map<String, Object> getMessageHeaderAsMap(Message message) {
		Map<String, Object> ret = new HashMap<>();
		for (String jmsHeader: JMS_HEADER_NAMES) {
			try {
				Method m = Message.class.getMethod("get" + jmsHeader, new Class[0]);
				Object value = m.invoke(message, new Object[0]);
			if (value != null) {
				ret.put(jmsHeader, m.invoke(message, new Object[0]));
			}
			} catch (Exception e) {
				SimLogger.getLogger().warn("problem when get JMS header [" + jmsHeader + "], " + e);
			}
		}
		return ret;
	}
	
	protected void genBody() throws IOException {
		this.body = convertor.rawRequestToBody(message);
	}
	
	public String getTopLine() {
		return this.topLine;
	}
	
	public String getHeaderLine(String header) {
		Object value = headers.get(header);
		return SimUtils.formatString(HEADER_LINE_FORMAT, header, value == null ? "" : value.toString());
	}
	
	public String getAutnenticationLine() {
		return null;
	}
	
	public String getBody() {
		return this.body;
	}
	
	@Override
	protected void doFillResponse(SimResponse response) throws IOException {
		try {
			Map<String, Object> respHeaders = response.getHeaders();
			String channel = (String) respHeaders.get(HEADER_NAME_CHANNEL);
			if (channel == null) {
				respHeaders.put(HEADER_NAME_CHANNEL, unifiedDestName);
			}
			if (CHANNEL_NAME_JMSREPLYTO.equals(channel)) {
				SimLogger.getLogger().info("use destination in JMSReplyTo header [" + message.getJMSReplyTo() + "]");
				String unifiedReplyToName = SimUtils.createUnifiedName(message.getJMSReplyTo().toString(), SimUtils.getBrokerName(unifiedDestName));
				SimMessageProducer producer = producerFactory.getProducer(unifiedReplyToName);
				if (producer == null) {
					producer = new SimMessageProducer(session, session.createProducer(message.getJMSReplyTo()));
					producerFactory.addProducer(unifiedReplyToName, producer);
				}
				respHeaders.put(HEADER_NAME_CHANNEL, unifiedReplyToName);
			}
			respHeaders.put(PROP_NAME_RESPONSE_TARGETSIMULATOR, script.getSimulatorName());
		} catch (JMSException e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public List<String> getAllHeaderNames() {
		return new ArrayList<>(headers.keySet());
	}

	@Override
	public String getRemoteAddress() {
		try {
			if (this.message != null) {
				return this.message.getJMSDestination().toString();
			}
			return super.getRemoteAddress();
		} catch (JMSException e) {
			return super.getRemoteAddress();
		}
	}
}
