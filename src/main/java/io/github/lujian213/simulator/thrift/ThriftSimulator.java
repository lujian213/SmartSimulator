package io.github.lujian213.simulator.thrift;

import static io.github.lujian213.simulator.thrift.ThriftSimulatorConstants.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.thrift.ProcessFunction;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TBase;
import org.apache.thrift.TBaseProcessor;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolUtil;
import org.apache.thrift.protocol.TType;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.SimScript;
import io.github.lujian213.simulator.SimSimulator;
import io.github.lujian213.simulator.util.BeanRepository;
import io.github.lujian213.simulator.util.ReqRespConvertor;
import io.github.lujian213.simulator.util.SimLogger;
import io.github.lujian213.simulator.util.SimUtils;

public class ThriftSimulator extends SimSimulator {

	protected int port;
	protected boolean useSSL;
	protected String keystore;
	protected String ksPwd;
	protected String proxyKeystore;
	protected String proxyKsPwd;
	protected String proxyTrustManagerType;
	protected String proxyTrustStoreType;
	protected String handlerClassName;
	protected String handlerSimClassName;
	protected TProcessor processor;
	protected Object handler;
	protected ReqRespConvertor convertor;
	protected TServer server;
	protected ThreadLocal<CallTrace<?>> trace = new ThreadLocal<>();

	public static class CallTrace<T> {
		private String remoteAddress;
		private ProcessFunction<T, ? extends TBase> fn;
		private TProtocol in;
		private TProtocol out;
		private int seqid;
		private T simInst;
		private boolean proxy = false;

		public CallTrace() {
		}

		public String getRemoteAddress() {
			return remoteAddress;
		}

		public void setRemoteAddress(String remoteAddress) {
			this.remoteAddress = remoteAddress;
		}

		public ProcessFunction<T, ? extends TBase> getFunction() {
			return fn;
		}

		public void setFunction(ProcessFunction<T, ? extends TBase> fn) {
			this.fn = fn;
		}

		public TProtocol getProtocolIn() {
			return in;
		}

		public void setProtocolIn(TProtocol in) {
			this.in = in;
		}

		public TProtocol getProtocolOut() {
			return out;
		}

		public void setProtocolOut(TProtocol out) {
			this.out = out;
		}

		public int getSeqid() {
			return seqid;
		}

		public void setSeqid(int seqid) {
			this.seqid = seqid;
		}

		public T getSimInst() {
			return simInst;
		}

		public void setSimInst(T simInst) {
			this.simInst = simInst;
		}

		public boolean isProxy() {
			return proxy;
		}

		public void setProxy(boolean proxy) {
			this.proxy = proxy;
		}
	}

	public class ProcessorWrapper<T, F extends TBase> extends TBaseProcessor<T> {
		private T iface;
		private Map<String, ProcessFunction<T, ? extends TBase>> map;

		public ProcessorWrapper(T iface, TBaseProcessor processor) {
			super(iface, processor.getProcessMapView());
			this.iface = iface;
			this.map = processor.getProcessMapView();
		}

		protected void sendErrorMessage(TProtocol in, TProtocol out, TMessage msg, String errorMsg) throws TException {
			TProtocolUtil.skip(in, TType.STRUCT);
			in.readMessageEnd();
			TApplicationException x = new TApplicationException(TApplicationException.UNKNOWN_METHOD, errorMsg);
			out.writeMessageBegin(new TMessage(msg.name, TMessageType.EXCEPTION, msg.seqid));
			x.write(out);
			out.writeMessageEnd();
			out.getTransport().flush();
		}

		@Override
		public boolean process(TProtocol in, TProtocol out) throws TException {
	    	SimUtils.setThreadContext(script);
	    	CallTrace<T> ct = new CallTrace<T>();
	    	trace.set(ct);
			String remoteAddress = "UNKNOWN";
			TTransport t = in.getTransport();
			if (t instanceof TSocket) {
				TSocket socket = TSocket.class.cast(t);
				remoteAddress = socket.getSocket().getInetAddress().getHostAddress() + socket.getSocket().getLocalPort();
			}
			ct.setRemoteAddress(remoteAddress);

			final TMessage msg = in.readMessageBegin();
			final ProcessFunction<T, ? extends TBase> fn = map.get(msg.name);

			if (fn == null) {
				sendErrorMessage(in, out, msg, "Invalid method name: '" + msg.name + "'");
				return true;
			}
			ct.setFunction(fn);
			ct.setProtocolIn(in);
			ct.setProtocolOut(out);
			ct.setSimInst(iface);
			ct.setSeqid(msg.seqid);

			ThriftSimRequest request = null;
			List<SimResponse> respList = new ArrayList<>();
			try {
				request = new ThriftSimRequest(ct, convertor);
				SimUtils.logIncomingMessage(request.getRemoteAddress(), ThriftSimulator.this.getName(), request);
				respList = script.genResponse(request);
	    	} catch (IOException e) {
				if (proxy) {
					ct.setProxy(true);
					SimLogger.getLogger().info("send to remote ...");
					fn.process(msg.seqid, in, out, iface);
					respList.add(new SimResponse(200, null, null));
				} else {
					SimLogger.getLogger().error("match and fill error", e);
					SimResponse response = new SimResponse("match and fill error '" + msg.name + "'");
					try {
						sendErrorMessage(in, out, msg, "match and fill error '" + msg.name + "'");
					} catch (Exception e1) {
						SimLogger.getLogger().error("send error message error", e1);
					}
					respList.add(response);
				}
			} finally {
				castToSimulatorListener().onHandleMessage(getName(), request, respList, !respList.isEmpty());
	    	}
			return true;
		}

	}

	public ThriftSimulator(SimScript script) throws IOException {
		super(script);
		convertor = SimUtils.createMessageConvertor(script, new DefaultThriftReqRespConvertor());
	}

	protected ThriftSimulator() throws IOException {
	}

	@Override
	protected void init() throws IOException {
		super.init();
		port = script.getMandatoryIntProperty(PROP_NAME_PORT, "no socket port defined");
		useSSL = script.getBooleanProperty(PROP_NAME_USE_SSL, false);
		handlerClassName = script.getMandatoryProperty(PROP_NAME_HANDLER_CLASSNAME, "no handler class name defined");
		handlerSimClassName = script.getMandatoryProperty(PROP_NAME_HANDLER_SIM_CLASSNAME, "no handler class name defined");
		if (useSSL) {
			keystore = script.getMandatoryProperty(PROP_NAME_KEYSTORE, "no keystore defined");
			ksPwd = script.getMandatoryProperty(PROP_NAME_KS_PASSWD, "no keystore passwd defined");
		}

		if (proxy && useSSL) {
			proxyKeystore = script.getMandatoryProperty(PROP_NAME_PROXY_KEYSTORE, "no proxy keystore defined");
			proxyKsPwd = script.getMandatoryProperty(PROP_NAME_PROXY_KS_PASSWD, "no proxy keystore passwd defined");
			proxyTrustManagerType = script.getMandatoryProperty(PROP_NAME_PROXY_TRUSTMANAGER_TYPE, "no proxy trust manager type defined");
			proxyTrustStoreType = script.getMandatoryProperty(PROP_NAME_PROXY_TRUSTSTORE_TYPE, "no proxy trust store type defined");
		}
	}

	@Override
	protected void doStart() throws IOException {
		String processorClassName = handlerClassName + "$Processor";
		String handlerInfClassName = handlerClassName + "$Iface";
		String clientClassName = handlerClassName + "$Client";
		Class<?> handlerInfClass = SimUtils.load(handlerInfClassName);
		Class<?> clientClass = SimUtils.load(clientClassName);
		Class<? extends TBaseProcessor> clazz = (Class<? extends TBaseProcessor>) SimUtils.load(processorClassName);
		try {
	    	trace.set(new CallTrace());
			final Object handler = BeanRepository.getInstance().addBean(handlerSimClassName, script.getConfigAsProperties()).getBean();
			Object handlerWrapper = Proxy.newProxyInstance(handler.getClass().getClassLoader(), new Class[] {handlerInfClass}, new InvocationHandler() {

				@Override
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					if (trace.get().isProxy()) {
						if (useSSL) {
							try (ThriftClient<?> client = new ThriftClient(proxyURL, proxyKeystore, proxyKsPwd, proxyTrustManagerType, proxyTrustStoreType, clientClass)) {
								return client.invoke(method, args);
							}
						} else {
							try (ThriftClient<?> client = new ThriftClient(proxyURL, clientClass)) {
								return client.invoke(method, args);
							}
						}
					} else {
						return method.invoke(handler, args);
					}
				}
			});
			Constructor<? extends TProcessor> constructor = clazz.getConstructor(new Class[] {handlerInfClass});
			TProcessor processor = new ProcessorWrapper(handlerWrapper, (TBaseProcessor) constructor.newInstance(handlerWrapper));

			if (useSSL) {
				TSSLTransportParameters params = new TSSLTransportParameters();
				params.setKeyStore(keystore, ksPwd, null, null);
				TServerTransport serverTransport = TSSLTransportFactory.getServerSocket(port, 0, null, params);
				server = new TSimpleServer(new Args(serverTransport).processor(processor));
			} else {
				TServerTransport serverTransport = new TServerSocket(port);
				server = new TSimpleServer(new Args(serverTransport).processor(processor));
			}
			new Thread(new Runnable() {
				@Override
				public void run() {
					server.serve();
				}
			}).start();
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | TTransportException e) {
			throw new IOException("initial thrift processor error", e);
		}
		runningURL = "thrift://" + InetAddress.getLocalHost().getHostName() + ":" + port;
	}

	@Override
	protected void doStop() {
		if (server != null) {
			server.stop();
		}
	}


	@Override
	public String getType() {
		return "thrift";
	}

	public static void main(String[] args) throws IOException {
		ThriftSimulator inst = new ThriftSimulator();
		inst.handlerClassName = "tutorial.Calculator";
		inst.doStart();
	}
}