package org.jingle.simulator;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.jingle.simulator.util.ResponseHandler;
import org.jingle.simulator.util.SimUtils;

public class SimResponse {
	private int code;
	private Map<String, Object> headers = new HashMap<>();
	private byte[] body;
	
	public SimResponse(Map<String, Object> context, SimResponseTemplate response) throws IOException {
		generate(context, response);
	}

	public SimResponse(int code, Map<String, Object> headers, byte[] body) {
		this.code = code;
		this.headers = headers;
		this.body = body;
	}

	public SimResponse(String message) {
		this.code = 200;
		this.body = message.getBytes();
	}

	public int getCode() {
		return code;
	}

	public Map<String, Object> getHeaders() {
		return headers;
	}

	public byte[] getBody() {
		return body;
	}
	
	public String getBodyAsString() {
		return new String(body);
	}
	
	protected void generate(Map<String, Object> context, SimResponseTemplate resp) throws IOException {
		VelocityContext vc = new VelocityContext();
		for (Map.Entry<String, Object> contextEntry : context.entrySet()) {
			vc.put(contextEntry.getKey(), contextEntry.getValue());
			vc.put(contextEntry.getKey().replace('.', '_'), contextEntry.getValue());
		}
		for (Map.Entry<String, String> entry : resp.getHeaders().entrySet()) {
			headers.put(entry.getKey(), SimUtils.mergeResult(vc, entry.getKey(), entry.getValue()));
		}
		body = ResponseHandler.getHandlerChain().handle(headers, vc, resp);
		code = resp.getCode();
	}
	
	public void print(PrintWriter pw) {
		pw.println("HTTP/1.1 " + getCode());
		for (Map.Entry<String, Object> entry: headers.entrySet()) {
			pw.println(entry.getKey() + ": " + entry.getValue());
		}
		pw.println(getBodyAsString());
		pw.flush();
	}

}

