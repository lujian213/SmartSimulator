package org.jingle.simulator.jms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.jingle.simulator.SimRequest;
import org.jingle.simulator.SimResponse;
import org.jingle.simulator.jms.JMSBroker.SimMessageProducer;
import org.jingle.simulator.util.ReqRespConvertor;
import org.jingle.simulator.util.SimLogger;
import org.jingle.simulator.util.SimUtils;

public class JMSSimRequest implements SimRequest {
	public static final String HEADER_NAME_CHANNEL = "_Channel";
	public static final String CHANNEL_NAME_JMSREPLYTO = "JMSReplyTo";
	public static final String CHANNEL_NAME_TEMP_QUEUE = "???";
	public static final String CHANNEL_NAME_TEMP_TOPIC = "###";
	public static final String HEADER_NAME_MESSAGE_TYPE = "_Message.Type";
	public static final String MESSAGE_TYPE_TEXT = "Text";
	public static final String MESSAGE_TYPE_BYTES = "Bytes";
	
	private static final String HEADER_LINE_FORMAT = "%s: %s";
	private Message message;
	private String unifiedDestName;
	private Map<String, SimMessageProducer> producerMap;
	private String topLine;
	private Map<String, Object> headers = new HashMap<>();
	private String body;
	private Session session;
	private ReqRespConvertor convertor;
	
	public JMSSimRequest(Message message, Session session, String unifiedDestName, Map<String, SimMessageProducer> producerMap, Map<String, JMSBroker> brokerMap, ReqRespConvertor convertor) throws IOException {
		this.message = message;
		this.unifiedDestName = unifiedDestName;
		this.producerMap = producerMap;
		this.session = session;
		this.convertor = convertor;
		if (message instanceof TextMessage) {
			this.topLine = "TextMessage";
		} else if (message instanceof BytesMessage) {
			this.topLine = "BytesMessage";
		} else {
			this.topLine = "OtherMessage";
		}
		genHeaders();
		genBody();
	}
	
	protected JMSSimRequest() {
		
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
		} catch (JMSException e) {
			throw new IOException(e);
		}
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
	public void fillResponse(SimResponse response) throws IOException {
		try {
			Map<String, Object> headers = response.getHeaders();
			String channel = (String) headers.remove(HEADER_NAME_CHANNEL);
			channel = (channel == null ? unifiedDestName: channel);
			
			SimMessageProducer producer = null;
			if (CHANNEL_NAME_JMSREPLYTO.equals(channel)) {
				SimLogger.getLogger().info("use destination in JMSReplyTo header [" + message.getJMSReplyTo() + "]");
				producer = new SimMessageProducer(session, session.createProducer(message.getJMSReplyTo()));
			} else {
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
			producer.getProducer().send(respMsg);
		} catch (JMSException e) {
			throw new IOException(e);
		}
	}
	
	protected Message createMessage(Session session, String type) throws JMSException {
		if (MESSAGE_TYPE_TEXT.equals(type) || type == null) {
			return session.createTextMessage();
		} else if (MESSAGE_TYPE_BYTES.equals(type)) {
			return session.createBytesMessage();
		} else {
			throw new JMSException("can't support message type [" + type + "]");
		}
	}

	@Override
	public List<String> getAllHeaderNames() {
		return new ArrayList<>(headers.keySet());
	}
}
