package org.jingle.simulator.socket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jingle.simulator.SimRequest;
import org.jingle.simulator.SimResponse;
import org.jingle.simulator.util.ReqRespConvertor;
import org.jingle.simulator.util.SimUtils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class SocketSimRequest implements SimRequest {
	private static final String HEADER_LINE_FORMAT = "%s: %s";
	private static final String TOP_LINE_FORMAT = "%s %s";

	private ChannelHandlerContext context;
	private String topLine;
	private Map<String, List<String>> headers = new HashMap<>();
	private String body;
	private ReqRespConvertor convertor;
	
	public SocketSimRequest(ChannelHandlerContext context, String type, ByteBuf buf, ReqRespConvertor convertor) throws IOException {
		this.context = context;
		this.convertor = convertor;
		this.body = convertor.rawRequestToBody(buf);
		String protocol = "HTTP/1.1";
		this.topLine = SimUtils.formatString(TOP_LINE_FORMAT, type, protocol);

	}
	
	protected SocketSimRequest() {
	}
	
	@Override
	public ReqRespConvertor getReqRespConvertor() {
		return this.convertor;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.topLine).append("\n");
		for (String key: headers.keySet()) {
			sb.append(this.getHeaderLine(key)).append("\n");
		}
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
		return SimUtils.formatString(HEADER_LINE_FORMAT, header, "");
	}
	
	public String getAutnenticationLine() {
		return null;
	}
	
	public String getBody() {
		return this.body;
	}
	
	public void fillResponse(SimResponse response) throws IOException {
		byte[] body = response.getBody();
		long length = body.length;
		ByteBuf buf = context.alloc().buffer((int)length);
		convertor.fillRawResponse(buf, response);
		context.writeAndFlush(buf);
	}

	@Override
	public List<String> getAllHeaderNames() {
		return new ArrayList<>(headers.keySet());
	}
}
