package io.github.lujian213.simulator.socket;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

public class SocketClient {
	private String host;
	private int port;
	private boolean useSSL;
	private Channel ch;
	private EventLoopGroup group;
	private ChannelHandler[] handlers;

	public SocketClient(String urlStr, ChannelHandler ... handlers) throws IOException {
		try {
			URI url = new URI(urlStr);
			this.host = url.getHost();
			this.port = url.getPort();
			this.useSSL = url.getScheme().equalsIgnoreCase("ssl");
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
		this.handlers = handlers;
	}

    public void start() throws IOException {
        // Configure SSL.
        final SslContext sslCtx;
        if (useSSL) {
            sslCtx = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }

        group = new NioEventLoopGroup();
//        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .handler(new ChannelInitializer<SocketChannel>() {
            	    @Override
            	    public void initChannel(SocketChannel ch) {

            	        ChannelPipeline pipeline = ch.pipeline();

            	        if (sslCtx != null) {
            	            pipeline.addLast(sslCtx.newHandler(ch.alloc(), host, port));
            	        }
            	        pipeline.addLast(handlers);
            	    }

             });
            // Start the connection attempt.
            try {
				ch = b.connect(host, port).sync().channel();
			} catch (InterruptedException e) {
				throw new IOException(e);
			}
    }

    public void sendMsg(ByteBuf msg) {
    	ch.writeAndFlush(msg);
    }

    public void stop() {
    	if (ch != null) {
    		ch.close();
    	}
    	if (group != null) {
    		group.shutdownGracefully();
    	}
    }
}