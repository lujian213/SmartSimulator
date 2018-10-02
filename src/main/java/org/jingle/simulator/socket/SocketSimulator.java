package org.jingle.simulator.socket;

import java.io.IOException;
import java.net.InetAddress;

import org.jingle.simulator.SimRequest;
import org.jingle.simulator.SimScript;
import org.jingle.simulator.SimSimulator;
import org.jingle.simulator.util.ReqRespConvertor;
import org.jingle.simulator.util.SimLogger;
import org.jingle.simulator.util.SimUtils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.util.ReferenceCountUtil;

public class SocketSimulator extends SimSimulator {
	public class SocketHandler extends ChannelInboundHandlerAdapter {
	    @Override
	    public void channelRead(ChannelHandlerContext ctx, Object msg) {
			SimLogger.setLogger(script.getLogger());
			SimRequest request = null;
			try {
				request = new SocketSimRequest(ctx, (ByteBuf)msg, convertor);
				SimLogger.getLogger().info("incoming request: [" + request.getTopLine() + "]");
				script.genResponse(request);
	    	} catch (IOException e) {
	    		SimLogger.getLogger().error("match and fill error", e);
	    	} finally {
				ReferenceCountUtil.release(msg);
	    	}
	    }

	    @Override
	    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
			SimLogger.getLogger().error("exception caught", cause);
	        ctx.close();
	    }

	}
	public static final String PROP_NAME_PORT = "simulator.socket.port";
	public static final String PROP_NAME_FRAME_DELIMITERS = "simulator.socket.frame.delimiters";
	public static final String PROP_NAME_FRAME_MAXLENGTH = "simulator.socket.frame.maxlength";

	private int port;
	private ChannelFuture cf;
	private EventLoopGroup bossGroup = null;
    private EventLoopGroup workerGroup = null;
	private ReqRespConvertor convertor;
	private int frameMaxLength;
	private ByteBuf[] delimiters;

	public SocketSimulator(SimScript script) throws IOException {
		super(script);
	}
	
	@Override
	protected void init() throws IOException {
		super.init();
		port = script.getMandatoryIntProperty(PROP_NAME_PORT, "no socket port defined");
		convertor = SimUtils.createMessageConvertor(script, new DefualtSocketReqRespConvertor());
		frameMaxLength = script.getProperty(PROP_NAME_FRAME_MAXLENGTH, 8192);
		delimiters = SimUtils.parseDelimiters(script.getProperty(PROP_NAME_FRAME_DELIMITERS, "0x0D0x0A,0x0A"));
	}


  
	@Override
	public void start() throws IOException {
		if (!running) {
			bossGroup = new NioEventLoopGroup();
		    workerGroup = new NioEventLoopGroup();
	        try {
	            ServerBootstrap b = new ServerBootstrap();
	            b.group(bossGroup, workerGroup)
	             .channel(NioServerSocketChannel.class)
	             .childHandler(new ChannelInitializer<SocketChannel>() {
	                 @Override
	                 public void initChannel(SocketChannel ch) throws Exception {
	                	 ch.pipeline().addLast(new DelimiterBasedFrameDecoder(frameMaxLength, delimiters));
	                     ch.pipeline().addLast(new SocketHandler());
	                 }
	             })
	             .option(ChannelOption.SO_BACKLOG, 128)          
	             .childOption(ChannelOption.SO_KEEPALIVE, true);
	            cf = b.bind(port);
				runningURL = "tcp://" + InetAddress.getLocalHost().getHostName() + ":" + port;
				SimLogger.getLogger().info("Simulator [" + this.getName() + "] running at " + runningURL);
				this.running = true;
	        } catch (IOException | RuntimeException e) {
	        	workerGroup.shutdownGracefully();
	        	bossGroup.shutdownGracefully();
	        	throw e;
	        }
		}
	}

	@Override
	public void stop() {
		SimLogger.getLogger().info("about to stop ...");
        try {
        	if (cf != null) {
        		cf.channel().closeFuture();
        	}
		} catch (Exception e) {
		}
        if (workerGroup != null) {
        	workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
        	bossGroup.shutdownGracefully();
        }
		SimLogger.getLogger().info("stopped");
		this.running = false;
	}
}