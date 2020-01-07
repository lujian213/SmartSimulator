package io.github.lujian213.simulator.socket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

public class DefaultFrameEncoderDecoder implements FrameEncoderDecoder {
	private ByteBuf[] delimiters;
	private int frameMaxLength;
	private ChannelHandler decoder;

	public DefaultFrameEncoderDecoder(int frameMaxLength, ByteBuf[] delimiters) {
		this.delimiters = delimiters;
		this.frameMaxLength = frameMaxLength;
		decoder = new DelimiterBasedFrameDecoder(frameMaxLength, delimiters);
	}

	@Override
	public ChannelHandler getFrameDecoder() {
		return decoder;
	}

	@Override
	public ByteBuf transformOutboundMsg(ByteBuf msg) {
		return Unpooled.wrappedBuffer(msg, delimiters[0]);
	}

}
