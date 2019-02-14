package io.github.lujian213.simulator.webbit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.webbitserver.WebSocketConnection;

import io.github.lujian213.simulator.AbstractSimRequest;
import io.github.lujian213.simulator.SimRequest;
import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.util.ReqRespConvertor;
import io.github.lujian213.simulator.util.SimUtils;

public class WebbitWSSimRequest extends AbstractSimRequest {
	public static final String HEADER_NAME_CHANNEL = "_Channel";
	public static final String HEADER_NAME_CHANNEL_ID = "_Channel_ID";

	private static final String HEADER_LINE_FORMAT = "%s: %s";
	private static final String TOP_LINE_FORMAT = "%s %s %s";
	private String topLine;
	private String body;
	private WebSocketConnection connection;
	private Map<String, String> headers = new HashMap<>();
	private ReqRespConvertor convertor;
	
	public WebbitWSSimRequest(WebSocketConnection connection, String channel, String type, byte[] message, ReqRespConvertor convertor) {
		this.connection = connection;
		this.convertor = convertor;
		String protocol = "HTTP/1.1";
		this.topLine = SimUtils.formatString(TOP_LINE_FORMAT, type, channel, protocol);
		try {
			this.body = convertor.rawRequestToBody(message);
		} catch (IOException e) {
		}
	}
	
	protected WebbitWSSimRequest() {
		
	}
	
	@Override
	public ReqRespConvertor getReqRespConvertor() {
		return this.convertor;
	}
	
	public WebSocketConnection getConnection() {
		return connection;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.topLine).append("\n");
		sb.append("\n");
		if (body != null) {
			sb.append(body);
		}
		sb.append("\n");
		return sb.toString();
	}
	
	public String getTopLine() {
		return this.topLine;
	}
	
	public String getHeaderLine(String header) {
		String value = headers.get(header);
		if (value == null) {
			return null;
		} else {
			return SimUtils.formatString(HEADER_LINE_FORMAT, header, value);
		}
	}
	
	public String getAutnenticationLine() {
		return null;
	}
	
	public String getBody() {
		return this.body;
	}
	
	@Override
	protected void doFillResponse(SimResponse resp) throws IOException {
		String actualChannel = (String) resp.getHeaders().get(HEADER_NAME_CHANNEL);
		String channelID = (String) resp.getHeaders().get(HEADER_NAME_CHANNEL_ID);
		if (channelID != null) {
			headers.put(HEADER_NAME_CHANNEL_ID, channelID);
		}
		WebSocketConnection conn = connection;
		if (actualChannel != null) {
			conn = WebbitWSHandler.findConnection(actualChannel);
		} 
		convertor.fillRawResponse(conn, resp);
	}

	@Override
	public List<String> getAllHeaderNames() {
		return new ArrayList<>(headers.keySet());
	}
}
