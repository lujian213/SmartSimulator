package org.jingle.simulator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.velocity.VelocityContext;
import org.jingle.simulator.util.ResponseHandler;
import org.jingle.simulator.util.SimUtils;

public class SimResponse {
	public static final String PROP_NAME_RESPONSE_TARGETSIMULATOR = "_response.targetSimulator";
	public static final String HEADER_CONTENT_ENCODING1 = "content-encoding";
	public static final String HEADER_CONTENT_ENCODING2 = "Content-encoding";
	public static final String HEADER_CONTENT_ENCODING3 = "Content-Encoding";

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
		if (isCompressed()) {
			return decompress(body);
		}
		return new String(body);
	}
	
	protected boolean isCompressed() {
		if ("gzip".equals(headers.get(HEADER_CONTENT_ENCODING1))) {
			return true;
		}
		if ("gzip".equals(headers.get(HEADER_CONTENT_ENCODING2))) {
			return true;
		}
		if ("gzip".equals(headers.get(HEADER_CONTENT_ENCODING3))) {
			return true;
		}
		if (body.length > 2 && body[0] == 31 && body[1] == -117) {
			return true;
		}
		return false;
	}
	
	protected String decompress(byte[] bytes) {
		try(GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes)); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[8 * 1024];
			int count = -1;
			while ((count = gis.read(buffer)) != -1) {
				bos.write(buffer, 0, count);
			}
			return bos.toString();
		} catch (IOException e) {
			throw new RuntimeException("error when decompress the content", e);
		}
	}
	
	protected byte[] compress(byte[] bytes) {
		try(ByteArrayOutputStream bos = new ByteArrayOutputStream(); GZIPOutputStream gos = new GZIPOutputStream(bos); ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
			byte[] buffer = new byte[8 * 1024];
			int count = -1;
			while ((count = bis.read(buffer)) != -1) {
				gos.write(buffer, 0, count);
			}
			gos.flush();
			gos.finish();
			return bos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("error when compress the content", e);
		}
	}

	public void setCode(int code) {
		this.code = code;
	}

	public void setHeaders(Map<String, Object> headers) {
		this.headers = headers;
	}

	public void setBody(byte[] body) {
		if (isCompressed()) {
			this.body = compress(body);
		} else {
			this.body = body;
		}
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

