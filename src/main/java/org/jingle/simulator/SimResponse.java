package org.jingle.simulator;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.jingle.simulator.util.SimUtils;

public class SimResponse {
	public static final String HEADER_NAME_BRIDGE = "Bridge";
	public static final String HEADER_NAME_BRIDGE_CONTENT_TYPE = "Bridge.Content-Type";
	public static final String BRIDGE_CONTENT_TYPE_BINARY = "Binary";
	public static final String BRIDGE_CONTENT_TYPE_TEXT = "Text";
	public static final String BRIDGE_CONTENT_TYPE_VM = "VM";
	private int code;
	private Map<String, Object> headers = new HashMap<>();
	private byte[] body;
	
	public SimResponse(Map<String, Object> context, SimResponseTemplate response) throws IOException {
		generate(context, response);
	}

	public SimResponse(int code, Map<String, Object> headers, byte[] body) throws IOException {
		this.code = code;
		this.headers = headers;
		this.body = body;
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
		for (Map.Entry<Object, Object> entry: System.getProperties().entrySet()) {
			vc.put((String) entry.getKey(), entry.getValue());
		}
		for (Map.Entry<String, Object> contextEntry : context.entrySet()) {
			vc.put(contextEntry.getKey(), contextEntry.getValue());
		}
		for (Map.Entry<String, String> entry : resp.getHeaders().entrySet()) {
			headers.put(entry.getKey(), SimUtils.mergeResult(vc, entry.getKey(), entry.getValue()));
		}
		
		String tunnel = (String) headers.remove(HEADER_NAME_BRIDGE);
		if (tunnel != null) {
			String contentType = resp.getHeaders().get(HEADER_NAME_BRIDGE_CONTENT_TYPE);
			byte[] bodyBytes = handleTunnelRequest(tunnel);
			if (BRIDGE_CONTENT_TYPE_BINARY.equals(contentType)) {
				body = bodyBytes;
			} else if (BRIDGE_CONTENT_TYPE_TEXT.equals(contentType)) {
				body = bodyBytes;
			} else if (BRIDGE_CONTENT_TYPE_TEXT.equals(contentType)) {
				body = SimUtils.mergeResult(vc, "body", handleTunnelRequest(tunnel)).getBytes();
			} else {
				throw new IOException ("unsupported tunnel content type: [" + contentType + "]");
			}
		} else {
			body = SimUtils.mergeResult(vc, "body", resp.getBody()).getBytes();
		}
		code = resp.getCode();
	}
	
	protected byte[] handleTunnelRequest(String urlStr) throws IOException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			URL url = new URL(urlStr);
			URLConnection conn = url.openConnection();
			conn.connect();
			try (BufferedInputStream bis = new BufferedInputStream(conn.getInputStream())) {
				byte[] buffer = new byte[8 * 1024];
				int count = -1;
				while ((count = bis.read(buffer)) != -1) {
					baos.write(buffer, 0, count);
				}
			}
			return baos.toByteArray();
		}
	}
	
}

