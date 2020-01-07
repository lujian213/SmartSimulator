package io.github.lujian213.simulator.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.SimScript;
import io.github.lujian213.simulator.SimSesseionLessSimulator;
import io.github.lujian213.simulator.SimSimulator;
import io.github.lujian213.simulator.util.BeanRepository;
import io.github.lujian213.simulator.util.ReqRespConvertor;
import io.github.lujian213.simulator.util.SimLogger;
import io.github.lujian213.simulator.util.SimUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
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
import static io.github.lujian213.simulator.socket.SocketSimulatorConstants.*;


public class SocketSimulator extends SimSimulator implements SimSesseionLessSimulator {
	public class SocketHandler extends ChannelInboundHandlerAdapter {
		private ChannelHandlerContext ctx;
		private String id;

		public SimScript getScript() {
			return SocketSimulator.this.getScript();
		}

	    @Override
	    public void channelRead(ChannelHandlerContext ctx, Object msg) {
	    	SimUtils.setThreadContext(script);
	    	SocketSimRequest request = null;
			List<SimResponse> respList = new ArrayList<>();
			try {
				request = new SocketSimRequest(this, ctx, TYPE_MESSAGE, (ByteBuf)msg, convertor);
				SimUtils.logIncomingMessage(request.getRemoteAddress(), SocketSimulator.this.getName(), request);
				respList = script.genResponse(request);
				handleIDChange(request);
	    	} catch (IOException e) {
				if (proxy) {
					SimLogger.getLogger().info("send to remote ...");
					sClient.sendMsg(frameEncoderDecoder.transformOutboundMsg((ByteBuf)msg));
					respList.add(new SimResponse("Unknown due to proxy mechanism"));
				} else {
					SimLogger.getLogger().error("match and fill error", e);
					SimResponse response = new SimResponse("match and fill error" + delimiterStrs[0]);
					try {
						this.sendResponse(response);
					} catch (IOException e1) {
						SimLogger.getLogger().error("send to remote ...");
					}
					respList.add(response);
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
	    	this.ctx = ctx;
	    	SimUtils.setThreadContext(script);
	    	SocketSimRequest request = null;
			List<SimResponse> respList = new ArrayList<>();
			try {
				request = new SocketSimRequest(this, ctx, TYPE_OPEN, null, convertor);
				SimLogger.getLogger().info("incoming request: [" + request.getTopLine() + "] from [" + request.getRemoteAddress() + "]");
				respList = script.genResponse(request);
	    	} catch (IOException e) {
				if (proxy) {
					sClient = new SocketClient(proxyURL, frameEncoderDecoder.getFrameDecoder(), new ClientHandler(ctx));
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
	    	SocketSimRequest request = null;
			List<SimResponse> respList = new ArrayList<>();
			try {
				request = new SocketSimRequest(this, ctx, TYPE_CLOSE, null, convertor);
				SimLogger.getLogger().info("incoming request: [" + request.getTopLine() + "] from [" + request.getRemoteAddress() + "]");
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
				if (this.id != null) {
					handlerMap.remove(id);
				}
				castToSimulatorListener().onHandleMessage(getName(), request, respList, !respList.isEmpty());
	    	}
	    }

	    protected void handleIDChange(SocketSimRequest request) {
	    	if (this.id == null) {
		    	String headerLine = request.getHeaderLine(HEADER_NAME_CHANNEL_ID);
		    	if (headerLine != null) {
		    		Map.Entry<String, String> entry = SimUtils.parseHeaderLine(headerLine);
		    		SimLogger.getLogger().info("ID change to " + entry.getValue());
		    		this.id = entry.getValue();
		    		handlerMap.put(this.id, this);
		    	}
	    	}
	    }

	    public void sendResponse(SimResponse response) throws IOException {
			byte[] body = response.getBody();
			long length = body.length;
			ByteBuf buf = ctx.alloc().buffer((int)length);
			convertor.fillRawResponse(buf, response);
			ctx.writeAndFlush(buf);

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
	private String[] delimiterStrs;
	private String encoderDecoderClassName;
	private FrameEncoderDecoder frameEncoderDecoder;
	private boolean useSSL;
	private SslContext sslCtx;
	private SocketClient sClient;
	private Map<String, SocketHandler> handlerMap = new HashMap<>();

	public SocketSimulator(SimScript script) throws IOException {
		super(script);
		convertor = SimUtils.createMessageConvertor(script, new DefaultSocketReqRespConvertor());
	}

	@Override
	protected void init() throws IOException {
		super.init();
		port = script.getMandatoryIntProperty(PROP_NAME_PORT, "no socket port defined");
		frameMaxLength = script.getIntProperty(PROP_NAME_FRAME_MAXLENGTH, 8192);
		String delimStr = script.getProperty(PROP_NAME_FRAME_DELIMITERS, "0x0D0x0A,0x0A");
		delimiters = SimUtils.parseDelimiters(delimStr);
		delimiterStrs = SimUtils.parseDelimitersAsString(delimStr);
		useSSL = script.getBooleanProperty(PROP_NAME_USE_SSL, false);
		encoderDecoderClassName = script.getProperty(PROP_NAME_FRAME_ENCODER_DECODER_CLASS);
		if (encoderDecoderClassName == null) {
			frameEncoderDecoder = new DefaultFrameEncoderDecoder(frameMaxLength, delimiters);
		} else {
			frameEncoderDecoder = (FrameEncoderDecoder) BeanRepository.getInstance().addBean(encoderDecoderClassName, script.getConfigAsProperties()).getBean();
		}
    }

	@Override
	protected void doStart() throws IOException {
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
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
         .channel(NioServerSocketChannel.class)
         .childHandler(new ChannelInitializer<SocketChannel>() {
             @Override
             public void initChannel(SocketChannel ch) throws Exception {
            	 if (sslCtx != null) {
            		 ch.pipeline().addLast(sslCtx.newHandler(ch.alloc(), InetAddress.getLocalHost().getHostName(), port));
            	 }
            	 ch.pipeline().addLast(frameEncoderDecoder.getFrameDecoder());
                 ch.pipeline().addLast(new SocketHandler());
             }
         })
         .option(ChannelOption.SO_BACKLOG, 128)
         .childOption(ChannelOption.SO_KEEPALIVE, true);
        cf = b.bind(port);

		runningURL = "tcp://" + InetAddress.getLocalHost().getHostName() + ":" + port;
	}

	@Override
	protected void doStop() {
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
	}

	@Override
	public void fillResponse(SimResponse response) throws IOException {
		Map<String, Object> headers = response.getHeaders();
		String channel = (String) headers.remove(HEADER_NAME_CHANNEL);
		SocketHandler handler = handlerMap.get(channel);
		if (handler == null) {
			throw new IOException("no such ws channel [" + handler + "] exists");
		}
		handler.sendResponse(response);
		SimLogger.getLogger().info("Use channel [" + channel + "] to send out message");

	}
}