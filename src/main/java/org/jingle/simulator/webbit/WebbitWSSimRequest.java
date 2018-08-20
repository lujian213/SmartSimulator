package org.jingle.simulator.webbit;

import java.io.IOException;
import java.util.Map;

import org.apache.velocity.VelocityContext;

import org.jingle.simulator.SimRequest;
import org.jingle.simulator.SimResponseTemplate;
import org.jingle.simulator.util.SimUtils;

public class WebbitWSSimRequest implements SimRequest {
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
	
	public void fillResponse(Map<String, Object> context, SimResponseTemplate resp) throws IOException {
		VelocityContext vc = new VelocityContext();
		for (Map.Entry<String, Object> contextEntry : context.entrySet()) {
			vc.put(contextEntry.getKey(), contextEntry.getValue());
		}
		String bodyResult = SimUtils.mergeResult(vc, "body", resp.getBody());
		String actualChannel = resp.getHeaders().get("Channel");
		
		bundle.sendMessage(actualChannel == null ? channel : actualChannel, bodyResult);
	}
}
