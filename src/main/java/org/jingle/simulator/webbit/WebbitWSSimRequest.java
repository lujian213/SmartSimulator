package org.jingle.simulator.webbit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jingle.simulator.SimRequest;
import org.jingle.simulator.SimResponse;
import org.jingle.simulator.util.SimUtils;
import org.webbitserver.WebSocketConnection;

public class WebbitWSSimRequest implements SimRequest {
	public static final String HEADER_NAME_CHANNEL = "Channel";
	public static final String HEADER_NAME_CHANNEL_ID = "Channel_ID";

	private static final String HEADER_LINE_FORMAT = "%s: %s";
	private static final String TOP_LINE_FORMAT = "%s %s %s";
	private String topLine;
	private String body;
	private WebSocketConnection connection;
	private Map<String, String> headers = new HashMap<>();
	
	public WebbitWSSimRequest(WebSocketConnection connection, String channel, String type, String message) {
		this.connection = connection;
		String protocol = "HTTP/1.1";
		this.topLine = SimUtils.formatString(TOP_LINE_FORMAT, type, channel, protocol);
		this.body = message;
	}
	
	protected WebbitWSSimRequest() {
		
	}
	
	public WebSocketConnection getConnection() {
		return connection;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.topLine).append("\n");
		sb.append("\n");
		if (body != null) {
			sb.append(body);
		}
		sb.append("\n");
		return sb.toString();
	}
	
	public String getTopLine() {
		return this.topLine;
	}
	
	public String getHeaderLine(String header) {
		String value = headers.get(header);
		if (value == null) {
			return null;
		} else {
			return SimUtils.formatString(HEADER_LINE_FORMAT, header, value);
		}
	}
	
	public String getAutnenticationLine() {
		return null;
	}
	
	public String getBody() {
		return this.body;
	}
	
	public void fillResponse(SimResponse resp) throws IOException {
		String actualChannel = (String) resp.getHeaders().get(HEADER_NAME_CHANNEL);
		String channelID = (String) resp.getHeaders().get(HEADER_NAME_CHANNEL_ID);
		if (channelID != null) {
			headers.put(HEADER_NAME_CHANNEL_ID, channelID);
		}
		if (actualChannel == null) {
			connection.send(resp.getBodyAsString());
		} else {
			WebbitWSHandler.sendMessage(actualChannel, resp.getBodyAsString());
		}
	}

	@Override
	public List<String> getAllHeaderNames() {
		return new ArrayList<>(headers.keySet());
	}
}
