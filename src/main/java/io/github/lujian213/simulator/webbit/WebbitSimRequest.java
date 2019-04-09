package io.github.lujian213.simulator.webbit;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import io.github.lujian213.simulator.AbstractSimRequest;
import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.util.ReqRespConvertor;
import io.github.lujian213.simulator.util.SimUtils;
import static io.github.lujian213.simulator.http.HTTPSimulatorConstants.*;

public class WebbitSimRequest extends AbstractSimRequest {
	private static final String TOP_LINE_FORMAT = "%s %s %s";
	private static final String HEADER_LINE_FORMAT = "%s: %s";
	private static final String AUTHENTICATION_LINE_FORMAT = "Authentication: %s,%s";
	private HttpRequest request; 
	private HttpResponse response;
	private String topLine;
	private String[] authentications = new String[] {"", ""};
	private String body;
	private ReqRespConvertor convertor;
	
	public WebbitSimRequest(HttpRequest request, HttpResponse response, ReqRespConvertor convertor) throws IOException {
		this.request = request;
		this.response = response;
		this.convertor = convertor;
		String method = request.method();
		String uri = request.uri();
		this.topLine = SimUtils.formatString(TOP_LINE_FORMAT, method, SimUtils.decodeURL(uri), HTTP1_1);
		genAuthentications();
		genBody();
	}
	
	protected WebbitSimRequest() {
		
	}
	
	@Override
	public String getRemoteAddress() {
		if (request != null) {
			String host = ((InetSocketAddress)request.remoteAddress()).getAddress().getHostAddress();
		    int port = ((InetSocketAddress)request.remoteAddress()).getPort();
			return host + ":" + port;
		} else {
			return super.getRemoteAddress();
		}
	}

	@Override
	public ReqRespConvertor getReqRespConvertor() {
		return this.convertor;
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
		this.body = convertor.rawRequestToBody(request);
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
	
	@Override
	protected void doFillResponse(SimResponse resp) throws IOException {
		for (Map.Entry<String, Object> entry : resp.getAllInternalHeaders().entrySet()) {
			if (HEADER_NAME_CONTENT_TYPE.equals(entry.getKey())) {
				response.header("Content-Type", entry.getValue().toString());
			}
		}
		boolean chunked = "chunked".equals(resp.getAllPublicHeaders().get("Transfer-Encoding"));
		for (Map.Entry<String, Object> entry : resp.getAllPublicHeaders().entrySet()) {
			boolean ignore = false;
			if (entry.getKey().equals("Transfer-Encoding")) {
				ignore = chunked;
			} else if (entry.getKey().equals(SimResponse.HEADER_CONTENT_ENCODING1) ||
					   entry.getKey().equals(SimResponse.HEADER_CONTENT_ENCODING2) || 
					   entry.getKey().equals(SimResponse.HEADER_CONTENT_ENCODING3)) {
				ignore = chunked;
			} 
			if (!ignore) {
				response.header(entry.getKey(), entry.getValue().toString());
			}
		}

		convertor.fillRawResponse(response, resp);
		response.status(resp.getCode());
		response.end();
	}

	@Override
	public List<String> getAllHeaderNames() {
		List<String> ret = new ArrayList<>(); 
		for (Map.Entry<String, String> entry : request.allHeaders()) {
			ret.add(entry.getKey());
		}
		return ret;
	}
}
