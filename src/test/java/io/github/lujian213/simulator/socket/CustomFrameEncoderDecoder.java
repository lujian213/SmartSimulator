package io.github.lujian213.simulator.socket;

import io.github.lujian213.simulator.util.function.SimConstructor;
import io.github.lujian213.simulator.util.function.SimParam;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;

public class CustomFrameEncoderDecoder implements FrameEncoderDecoder {
	private String delimRegex;
	private int maxFrameLength;
	private ChannelHandler decoder;

	@SimConstructor
	public CustomFrameEncoderDecoder(@SimParam("simulator.socket.frame.maxlength") int maxFrameLength,
			@SimParam("simulator.socket.frame.delimiters") String delimRegex) {
		this.delimRegex = delimRegex;
		this.maxFrameLength = maxFrameLength;
		decoder = new CustomFrameDecoder(maxFrameLength, delimRegex);
	}

	@Override
	public ChannelHandler getFrameDecoder() {
		return decoder;
	}

	@Override
	public ByteBuf transformOutboundMsg(ByteBuf msg) {
		return Unpooled.copiedBuffer(msg);
	}
}
