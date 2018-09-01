package org.jingle.simulator.webbit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jingle.simulator.SimRequest;
import org.jingle.simulator.SimResponse;
import org.jingle.simulator.util.SimUtils;

public class WebbitWSSimRequest implements SimRequest {
	private static final String HEADER_NAME_CHANNEL = "Channel";

	private static final String TOP_LINE_FORMAT = "%s %s %s";
	private WebbitWSHandlerBundle bundle; 
	private String topLine;
	private String body;
	private String channel;
	
	public WebbitWSSimRequest(WebbitWSHandlerBundle bundle, String channel, String type, String message) throws IOException {
		this.bundle = bundle;
		this.channel = channel;
		String protocol = "HTTP/1.1";
		this.topLine = SimUtils.formatString(TOP_LINE_FORMAT, type, channel, protocol);
		this.body = message;
	}
	
	protected WebbitWSSimRequest() {
		
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
		return null;
	}
	
	public String getAutnenticationLine() {
		return null;
	}
	
	public String getBody() {
		return this.body;
	}
	
	public void fillResponse(SimResponse resp) throws IOException {
		String actualChannel = (String) resp.getHeaders().get(HEADER_NAME_CHANNEL);
		bundle.sendMessage(actualChannel == null ? channel : actualChannel, resp.getBodyAsString());
	}

	@Override
	public List<String> getAllHeaderNames() {
		return new ArrayList<>();
	}
}
