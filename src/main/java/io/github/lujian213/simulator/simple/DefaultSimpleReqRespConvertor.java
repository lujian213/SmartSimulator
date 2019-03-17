package io.github.lujian213.simulator.simple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;

import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.util.ReqRespConvertor;

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
		((HttpExchange)rawResponse).sendResponseHeaders(simResponse.getCode(), simResponse.getBody().length);
		try (OutputStream os = ((HttpExchange) rawResponse).getResponseBody()) {
			os.write(simResponse.getBody());
		}
	}
}