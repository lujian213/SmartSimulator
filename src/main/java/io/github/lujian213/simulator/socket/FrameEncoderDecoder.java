package io.github.lujian213.simulator.socket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;

public interface FrameEncoderDecoder {
	public ChannelHandler getFrameDecoder();
	public ByteBuf transformOutboundMsg(ByteBuf msg);
}
