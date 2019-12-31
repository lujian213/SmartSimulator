package io.github.lujian213.simulator.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.lujian213.simulator.AbstractSimRequest;
import io.github.lujian213.simulator.SimResponse;

public class XSimRequest extends AbstractSimRequest {
	private static final String TOP_LINE_FORMAT = "%s %s %s";
	private static final String HEADER_LINE_FORMAT = "%s: %s";

	private String topLine;
	private Map<String, String> headers = new HashMap<>();
	private String body;

	public XSimRequest(String method, String url) {
		this.topLine = SimUtils.formatString(TOP_LINE_FORMAT, method, url, HTTP1_1);
	}

	public XSimRequest(String method, String url, String body) {
		this(method, url);
		this.body = body;
	}

	@Override
	public List<String> getAllHeaderNames() {
		return new ArrayList<>(headers.keySet());
	}

	@Override
	public String getHeaderLine(String header) {
		String value = headers.get(header);
		return SimUtils.formatString(HEADER_LINE_FORMAT, header, value == null? "" : value);
	}

	@Override
	public String getAuthenticationLine() {
		return null;
	}

	@Override
	public String getBody() {
		return body;
	}

	@Override
	public String getTopLine() {
		return topLine;
	}

	@Override
	public ReqRespConvertor getReqRespConvertor() {
		return null;
	}

	@Override
	protected void doFillResponse(SimResponse response) throws IOException {
	}

	@Override
	public void fillResponse(SimResponse response) throws IOException {
		throw new IOException("Not supported function");
	}

	public void addHeader(String headerName, String headerValue) {
		headers.put(headerName, headerValue);
	}
}
