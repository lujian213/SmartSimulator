package org.jingle.simulator.webbit;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import org.jingle.simulator.SimRequest;
import org.jingle.simulator.SimResponseTemplate;
import org.jingle.simulator.util.SimUtils;

public class WebbitSimRequest implements SimRequest {
	private static final String TOP_LINE_FORMAT = "%s %s %s";
	private static final String HEADER_LINE_FORMAT = "%s: %s";
	private static final String AUTHENTICATION_LINE_FORMAT = "Authentication: %s,%s";
	private HttpRequest request; 
	private HttpResponse response;
	private String topLine;
	private String[] authentications = new String[] {"", ""};
	private String body;
	
	public WebbitSimRequest(HttpRequest request, HttpResponse response) throws IOException {
		this.request = request;
		this.response = response;
		String method = request.method();
		String uri = request.uri();
		String protocol = "HTTP/1.1";
		this.topLine = SimUtils.formatString(TOP_LINE_FORMAT, method, SimUtils.decodeURL(uri), protocol);
		genAuthentications();
		genBody();
	}
	
	protected WebbitSimRequest() {
		
	}
	
	protected void genAuthentications() {
		try {
			String val = request.header("Authorization");
			if (val != null) {
				if (val.startsWith("Basic ")) {
					String base64Str = val.substring(6);
					String str = new String(Base64.getDecoder().decode(base64Str), "utf-8");
					String[] parts = str.split(":");
					authentications[0] = parts[0];
					authentications[1] = parts[1];
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void genBody() throws IOException {
		this.body = request.body();
	}
	
	public String getTopLine() {
		return this.topLine;
	}
	
	public String getHeaderLine(String header) {
		List<String> values = request.headers(header);
		StringBuffer value = new StringBuffer();
		if (values != null) {
			for (int i = 1; i <= values.size(); i++) {
				value.append(values.get(i - 1));
				if (i != values.size())
					value.append(",");
			}
		}
		return SimUtils.formatString(HEADER_LINE_FORMAT, header, value.toString());
	}
	
	public String getAutnenticationLine() {
		return SimUtils.formatString(AUTHENTICATION_LINE_FORMAT, authentications);
	}
	
	public String getBody() {
		return this.body;
	}
	
	public void fillResponse(Map<String, Object> context, SimResponseTemplate resp) throws IOException {
		VelocityContext vc = new VelocityContext();
		for (Map.Entry<String, Object> contextEntry : context.entrySet()) {
			vc.put(contextEntry.getKey(), contextEntry.getValue());
		}
		for (Map.Entry<String, String> entry : resp.getHeaders().entrySet()) {
			response.header(entry.getKey(), SimUtils.mergeResult(vc, entry.getKey(), entry.getValue()));
		}
		String bodyResult = SimUtils.mergeResult(vc, "body", resp.getBody());
		byte[] body = bodyResult.getBytes();
		response.content(body);
		response.status(resp.getCode());
		response.end();
	}
}
