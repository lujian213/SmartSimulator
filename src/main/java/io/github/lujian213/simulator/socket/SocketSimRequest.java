package io.github.lujian213.simulator.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.lujian213.simulator.AbstractSimRequest;
import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.socket.SocketSimulator.SocketHandler;
import io.github.lujian213.simulator.util.ReqRespConvertor;
import io.github.lujian213.simulator.util.SimUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import static io.github.lujian213.simulator.socket.SocketSimulatorConstants.*;

public class SocketSimRequest extends AbstractSimRequest {
	private static final String HEADER_LINE_FORMAT = "%s: %s";
	private static final String TOP_LINE_FORMAT = "%s %s";
	
	private SocketHandler handler;
	private ReqRespConvertor convertor;
	private ChannelHandlerContext ctx;
	private String topLine;
	private Map<String, String> headers = new HashMap<>();
	private String body;
	
	public SocketSimRequest(SocketHandler handler, ChannelHandlerContext ctx, String type, ByteBuf buf, ReqRespConvertor convertor) throws IOException {
		this.handler = handler;
		this.convertor = convertor;
		this.ctx = ctx;
		this.body = convertor.rawRequestToBody(buf);
		this.topLine = SimUtils.formatString(TOP_LINE_FORMAT, type, HTTP1_1);

	}
	
	protected SocketSimRequest() {
	}
	
	@Override
	public ReqRespConvertor getReqRespConvertor() {
		return this.convertor;
	}
	
	public String getTopLine() {
		return this.topLine;
	}
	
	public String getHeaderLine(String header) {
		return SimUtils.formatString(HEADER_LINE_FORMAT, header, "");
	}
	
	public String getAutnenticationLine() {
		return null;
	}
	
	public String getBody() {
		return this.body;
	}
	
	@Override
	protected void doFillResponse(SimResponse response) throws IOException {
		String actualChannel = (String) response.getHeaders().get(HEADER_NAME_CHANNEL);
		String channelID = (String) response.getHeaders().get(HEADER_NAME_CHANNEL_ID);
		if (channelID != null) {
			headers.put(HEADER_NAME_CHANNEL_ID, channelID);
		}
		if (actualChannel != null) {
			response.getHeaders().put(PROP_NAME_RESPONSE_TARGETSIMULATOR, handler.getScript().getSimulatorName());
		} else {
			handler.sendResponse(response);
		}
	}

	@Override
	public List<String> getAllHeaderNames() {
		return new ArrayList<>(headers.keySet());
	}

	@Override
	public String getRemoteAddress() {
		if (ctx != null) {
			String host = ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress();
		    int port = ((InetSocketAddress)ctx.channel().remoteAddress()).getPort();
		    return host + ":" + port;
		} else {
			return super.getRemoteAddress();
		}
	}
}
