package org.jingle.simulator.jms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.jingle.simulator.SimRequest;
import org.jingle.simulator.SimResponse;
import org.jingle.simulator.jms.JMSBroker.SimMessageProducer;
import org.jingle.simulator.util.SimLogger;
import org.jingle.simulator.util.SimUtils;

public class JMSSimRequest implements SimRequest {
	private static final String HEADER_NAME_CHANNEL = "Channel";
	private static final String CHANNEL_NAME_JMSREPLYTO = "JMSReplyTo";
	private static final String CHANNEL_NAME_TEMP_QUEUE = "???";
	
	private static final String HEADER_LINE_FORMAT = "%s: %s";
	private Message message;
	private String unifiedDestName;
	private Map<String, SimMessageProducer> producerMap;
	private String topLine;
	private Map<String, Object> headers = new HashMap<>();
	private String body;
	private Session session;
	
	public JMSSimRequest(Message message, Session session, String unifiedDestName, Map<String, SimMessageProducer> producerMap) throws IOException {
		this.message = message;
		this.unifiedDestName = unifiedDestName;
		this.producerMap = producerMap;
		this.session = session;
		if (message instanceof TextMessage) {
			this.topLine = "TextMessage";
		} else {
			throw new IOException("unsupported message type [" + message.getClass() + "]");
		}
		genHeaders();
		genBody();
	}
	
	protected JMSSimRequest() {
		
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
		try {
			this.body = ((TextMessage)message).getText();
		} catch (JMSException e) {
			throw new IOException(e);
		}
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
			TextMessage respMsg = producer.getSession().createTextMessage();
			for (Map.Entry<String, Object> entry : headers.entrySet()) {
				respMsg.setStringProperty(entry.getKey(), entry.getValue().toString());
			}
			respMsg.setText(response.getBodyAsString());
			producer.getProducer().send(respMsg);
		} catch (JMSException e) {
			throw new IOException(e);
		}
	}

	@Override
	public List<String> getAllHeaderNames() {
		return new ArrayList<>(headers.keySet());
	}
}
