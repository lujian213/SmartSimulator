package org.jingle.simulator.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import org.jingle.simulator.SimRequest;
import org.jingle.simulator.SimResponse;
import org.jingle.simulator.SimScript;
import org.jingle.simulator.SimSimulator;
import org.jingle.simulator.util.ReqRespConvertor;
import org.jingle.simulator.util.SimLogger;
import org.jingle.simulator.util.SimUtils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class SocketSimulator extends SimSimulator {
	public class SocketHandler extends ChannelInboundHandlerAdapter {
	    @Override
	    public void channelRead(ChannelHandlerContext ctx, Object msg) {
	    	SimUtils.setThreadContext(script);
			SimRequest request = null;
			List<SimResponse> respList = new ArrayList<>();
			try {
				request = new SocketSimRequest(ctx, TYPE_MESSAGE, (ByteBuf)msg, convertor);
				SimLogger.getLogger().info("incoming request: [" + request.getTopLine() + "]");
				respList = script.genResponse(request);
	    	} catch (IOException e) {
				if (proxy) {
					SimLogger.getLogger().info("send to remote ...");
					sClient.sendMsg(Unpooled.copiedBuffer((ByteBuf)msg));
					sClient.sendMsg(Unpooled.copiedBuffer(delimiters[0]));
					respList.add(new SimResponse("Unknown due to proxy mechanism"));
				} else {
					SimLogger.getLogger().error("match and fill error", e);
				}
			} finally {
//				ReferenceCountUtil.release(msg);
				castToSimulatorListener().onHandleMessage(getName(), request, respList, !respList.isEmpty());
	    	}
	    }

	    @Override
	    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
	    	SimUtils.setThreadContext(script);
			SimLogger.getLogger().error("exception caught", cause);
	        ctx.close();
	    }
	    
	    @Override
	    public void channelActive(ChannelHandlerContext ctx) throws Exception {
	    	SimUtils.setThreadContext(script);
			SimRequest request = null;
			List<SimResponse> respList = new ArrayList<>();
			try {
				request = new SocketSimRequest(ctx, TYPE_OPEN, null, convertor);
				SimLogger.getLogger().info("incoming request: [" + request.getTopLine() + "]");
				respList = script.genResponse(request);
	    	} catch (IOException e) {
				if (proxy) {
					sClient = new SocketClient(proxyURL, new DelimiterBasedFrameDecoder(frameMaxLength, delimiters), new ClientHandler(ctx));
					sClient.start();
					respList.add(new SimResponse("Unknown due to proxy mechanism"));
				} else {
					SimLogger.getLogger().error("match and fill error", e);
				}
			} finally {
//				ReferenceCountUtil.release(msg);
				castToSimulatorListener().onHandleMessage(getName(), request, respList, !respList.isEmpty());
	    	}
	    }

	    @Override
	    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
	    	SimUtils.setThreadContext(script);
			SimRequest request = null;
			List<SimResponse> respList = new ArrayList<>();
			try {
				request = new SocketSimRequest(ctx, TYPE_CLOSE, null, convertor);
				SimLogger.getLogger().info("incoming request: [" + request.getTopLine() + "]");
				respList = script.genResponse(request);
	    	} catch (IOException e) {
				if (proxy) {
					SimLogger.getLogger().info("close remote connection");
					sClient.stop();
					respList.add(new SimResponse("Unknown due to proxy mechanism"));
				} else {
					SimLogger.getLogger().error("match and fill error", e);
				}
			} finally {
//				ReferenceCountUtil.release(msg);
				castToSimulatorListener().onHandleMessage(getName(), request, respList, !respList.isEmpty());
	    	}
	    }
	}
	
	@Override
	public String getType() {
		return "socket";
	}

	
	public class ClientHandler extends ChannelInboundHandlerAdapter {
		private ChannelHandlerContext delegator;
		public ClientHandler(ChannelHandlerContext delegator) {
			this.delegator = delegator;
		}
	    @Override
	    public void channelRead(ChannelHandlerContext ctx, Object msg) {
			SimUtils.setThreadContext(script);
			SimLogger.getLogger().info("get message from remote ...");
			
			try {
				delegator.writeAndFlush(Unpooled.copiedBuffer((ByteBuf)msg));
				delegator.writeAndFlush(Unpooled.copiedBuffer(delimiters[0]));
			} finally {
//				ReferenceCountUtil.release(msg);
	    	}
	    }

	    @Override
	    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
			SimUtils.setThreadContext(script);
			SimLogger.getLogger().error("exception caught", cause);
	        ctx.close();
	    }
	    
	    @Override
	    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			SimUtils.setThreadContext(script);
			delegator.close();
	    }

	}
	
	public static final String PROP_NAME_PORT = "simulator.socket.port";
	public static final String PROP_NAME_FRAME_DELIMITERS = "simulator.socket.frame.delimiters";
	public static final String PROP_NAME_FRAME_MAXLENGTH = "simulator.socket.frame.maxlength";
	public static final String PROP_NAME_USE_SSL = "simulator.socket.useSSL";
	private final static String TYPE_OPEN = "OPEN";
	private final static String TYPE_CLOSE = "CLOSE";
	private final static String TYPE_MESSAGE = "MESSAGE";

	private int port;
	private ChannelFuture cf;
	private EventLoopGroup bossGroup = null;
    private EventLoopGroup workerGroup = null;
	private ReqRespConvertor convertor;
	private int frameMaxLength;
	private ByteBuf[] delimiters;
	private boolean useSSL;
	private SslContext sslCtx;
	private SocketClient sClient;

	public SocketSimulator(SimScript script) throws IOException {
		super(script);
	}
	
	@Override
	protected void init() throws IOException {
		super.init();
		port = script.getMandatoryIntProperty(PROP_NAME_PORT, "no socket port defined");
		convertor = SimUtils.createMessageConvertor(script, new DefualtSocketReqRespConvertor());
		frameMaxLength = script.getConfig().getInt(PROP_NAME_FRAME_MAXLENGTH, 8192);
		delimiters = SimUtils.parseDelimiters(script.getConfig().getString(PROP_NAME_FRAME_DELIMITERS, "0x0D0x0A,0x0A"));
		useSSL = script.getConfig().getBoolean(PROP_NAME_USE_SSL, false); 
	}
  
	@Override
	protected void doStart() throws IOException {
		if (!running) {
			if (useSSL) {
				try {
					SelfSignedCertificate ssc = new SelfSignedCertificate();
					sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
				} catch (CertificateException e) {
					throw new IOException(e);
				}
			} else {
				sslCtx = null;
			}
			bossGroup = new NioEventLoopGroup();
		    workerGroup = new NioEventLoopGroup();
	        try {
	            ServerBootstrap b = new ServerBootstrap();
	            b.group(bossGroup, workerGroup)
	             .channel(NioServerSocketChannel.class)
	             .childHandler(new ChannelInitializer<SocketChannel>() {
	                 @Override
	                 public void initChannel(SocketChannel ch) throws Exception {
	                	 if (sslCtx != null) {
	                		 ch.pipeline().addLast(sslCtx.newHandler(ch.alloc(), InetAddress.getLocalHost().getHostName(), port));
	                	 }
	                	 ch.pipeline().addLast(new DelimiterBasedFrameDecoder(frameMaxLength, delimiters));
	                     ch.pipeline().addLast(new SocketHandler());
	                 }
	             })
	             .option(ChannelOption.SO_BACKLOG, 128)          
	             .childOption(ChannelOption.SO_KEEPALIVE, true);
	            cf = b.bind(port);

				runningURL = "tcp://" + InetAddress.getLocalHost().getHostName() + ":" + port;
	        } catch (IOException | RuntimeException e) {
	        	workerGroup.shutdownGracefully();
	        	bossGroup.shutdownGracefully();
	        	throw e;
	        }
		}
	}

	@Override
	public void stop() {
		super.stop();
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
        if (sClient != null) {
        	sClient.stop();
        }
		SimLogger.getLogger().info("stopped");
		this.running = false;
		this.runningURL = null;
	}
}