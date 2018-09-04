package org.jingle.simulator.socket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jingle.simulator.SimRequest;
import org.jingle.simulator.SimResponse;
import org.jingle.simulator.util.SimUtils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class SocketSimRequest implements SimRequest {
	private static final String HEADER_LINE_FORMAT = "%s: %s";
	private ChannelHandlerContext context;
	private String topLine;
	private Map<String, List<String>> headers = new HashMap<>();
	private String body;
	
	public SocketSimRequest(ChannelHandlerContext context, String msg) throws IOException {
		this.context = context;
		this.body = msg;
		this.topLine = "TextMessage";
	}
	
	protected SocketSimRequest() {
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
		buf.writeBytes(body);
		context.writeAndFlush(buf);
	}

	@Override
	public List<String> getAllHeaderNames() {
		return new ArrayList<>(headers.keySet());
	}
}
