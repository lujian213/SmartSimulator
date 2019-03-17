package io.github.lujian213.simulator.webbit;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.webbitserver.WebSocketConnection;

import io.github.lujian213.simulator.AbstractSimRequest;
import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.util.ReqRespConvertor;
import io.github.lujian213.simulator.util.SimUtils;
import static io.github.lujian213.simulator.webbit.WebbitSimulatorConstants.*;

public class WebbitWSSimRequest extends AbstractSimRequest {
	private static final String HEADER_LINE_FORMAT = "%s: %s";
	private static final String TOP_LINE_FORMAT = "%s %s %s";
	private String topLine;
	private String body;
	private WebbitWSHandler handler;
	private WebSocketConnection connection;
	private Map<String, String> headers = new HashMap<>();
	private ReqRespConvertor convertor;
	
	public WebbitWSSimRequest(WebbitWSHandler handler, WebSocketConnection connection, String channel, String type, byte[] message, ReqRespConvertor convertor) {
		this.handler = handler;
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
		if (actualChannel != null) {
			handler.sendResponse(actualChannel, resp);
		} else {
			convertor.fillRawResponse(connection, resp);
		}
	}

	@Override
	public List<String> getAllHeaderNames() {
		return new ArrayList<>(headers.keySet());
	}

	@Override
	public String getRemoteAddress() {
		if (connection != null) {
			String host = ((InetSocketAddress)connection.httpRequest().remoteAddress()).getAddress().getHostAddress();
		    int port = ((InetSocketAddress)connection.httpRequest().remoteAddress()).getPort();
			return host + ":" + port;
		} else {
			return super.getRemoteAddress();
		}
	}
}
