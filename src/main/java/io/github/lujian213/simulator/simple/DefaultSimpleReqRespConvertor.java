package io.github.lujian213.simulator.simple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.util.ReqRespConvertor;
import io.github.lujian213.simulator.util.SimLogger;

public class DefaultSimpleReqRespConvertor implements ReqRespConvertor {

	@Override
	public String rawRequestToBody(Object rawRequest) throws IOException {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(((HttpExchange) rawRequest).getRequestBody()))) {
			StringBuffer sb = new StringBuffer();

			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			return sb.toString();
		}
	}

	@Override
	public void fillRawResponse(Object rawResponse, SimResponse simResponse) throws IOException {
		HttpExchange exchange = (HttpExchange)rawResponse;
		Headers respHeaders = exchange.getResponseHeaders();
		List<String> values = respHeaders.get("Transfer-Encoding");
		if (values != null && values.contains("chunked")) {
			SimLogger.getLogger().info("set contentLength to 0 since TransferEncoding is chunked");
			exchange.sendResponseHeaders(simResponse.getCode(), 0);
		} else {
			exchange.sendResponseHeaders(simResponse.getCode(), simResponse.getBody().length);
		}
		try (OutputStream os = ((HttpExchange) rawResponse).getResponseBody()) {
			os.write(simResponse.getBody());
		}
	}
}
