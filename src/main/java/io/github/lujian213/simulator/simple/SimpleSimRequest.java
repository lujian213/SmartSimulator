package io.github.lujian213.simulator.simple;

import static io.github.lujian213.simulator.SimSimulatorConstants.HEADER_NAME_CONTENT_TYPE;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import io.github.lujian213.simulator.AbstractSimRequest;
import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.util.ReqRespConvertor;
import io.github.lujian213.simulator.util.SimLogger;
import io.github.lujian213.simulator.util.SimUtils;

@SuppressWarnings("restriction")
public class SimpleSimRequest extends AbstractSimRequest {
	private static final String TOP_LINE_FORMAT = "%s %s %s";
	private static final String HEADER_LINE_FORMAT = "%s: %s";
	private static final String AUTHENTICATION_LINE_FORMAT = "Authentication: %s,%s";
	private HttpExchange httpExchange;
	private String topLine;
	private Map<String, List<String>> headers;
	private String[] authentications = new String[] {"", ""};
	private String body;
	private byte[] rawBody;
	private ReqRespConvertor convertor;

	public SimpleSimRequest(HttpExchange exchange, ReqRespConvertor convertor) throws IOException {
		this.httpExchange = exchange;
		this.convertor = convertor;
		String method = exchange.getRequestMethod();
		URI uri = exchange.getRequestURI();
		String protocol = exchange.getProtocol();
		this.topLine = SimUtils.formatString(TOP_LINE_FORMAT, method, SimUtils.decodeURL(uri.toASCIIString()), protocol);
		genAuthentications(exchange);
		genHeaders(exchange);
		genBody(exchange);
	}

	protected SimpleSimRequest() {

	}

	@Override
	public String getRemoteAddress() {
		if (this.httpExchange != null) {
			return httpExchange.getRemoteAddress().getHostName() + ":" + httpExchange.getRemoteAddress().getPort();
		} else {
			return super.getRemoteAddress();
		}
	}

	@Override
	public ReqRespConvertor getReqRespConvertor() {
		return this.convertor;
	}

	protected void genHeaders(HttpExchange exchange) {
		this.headers = exchange.getRequestHeaders();
	}

	protected void genAuthentications(HttpExchange exchange) {
		try {
			List<String> val = exchange.getRequestHeaders().get("Authorization");
			if (val != null) {
				if (val.get(0).startsWith("Basic ")) {
					String base64Str = val.get(0).substring(6);
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

	protected void genBody(HttpExchange exchange) throws IOException {
		try (BufferedInputStream bis = new BufferedInputStream(exchange.getRequestBody())) {
			this.rawBody = SimUtils.readStream2Array(bis);
		}
		exchange.setStreams(new ByteArrayInputStream(this.rawBody), null);
		this.body = convertor.rawRequestToBody(exchange);
	}

	public String getTopLine() {
		return this.topLine;
	}

	public String getHeaderLine(String header) {
		List<String> values = headers.get(header);
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

	public String getAuthenticationLine() {
		return SimUtils.formatString(AUTHENTICATION_LINE_FORMAT, authentications);
	}

	public String getBody() {
		return this.body;
	}

	@Override
	public byte[] getRawBodyAsBytes() {
		return this.rawBody;
	}

	@Override
	protected void doFillResponse(SimResponse response) throws IOException {
		Headers respHeaders = httpExchange.getResponseHeaders();

		for (Map.Entry<String, Object> entry : response.getAllInternalHeaders().entrySet()) {
			if (HEADER_NAME_CONTENT_TYPE.equals(entry.getKey())) {
				respHeaders.add("Content-Type", entry.getValue().toString());
			}
		}
		for (Map.Entry<String, Object> entry : response.getAllPublicHeaders().entrySet()) {
			respHeaders.add(entry.getKey(), entry.getValue().toString());
		}
		List<String> values = respHeaders.get("Transfer-Encoding");
		if (values != null && values.contains("chunked")) {
			SimLogger.getLogger().info("set contentLength to 0 since TransferEncoding is chunked");
		}
		convertor.fillRawResponse(httpExchange, response);
	}

	@Override
	public List<String> getAllHeaderNames() {
		return new ArrayList<>(headers.keySet());
	}
}
