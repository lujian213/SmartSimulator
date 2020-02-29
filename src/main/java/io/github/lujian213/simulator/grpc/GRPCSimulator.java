package io.github.lujian213.simulator.grpc;

import static io.github.lujian213.simulator.grpc.GRPCSimulatorConstants.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.SimScript;
import io.github.lujian213.simulator.SimSimulator;
import io.github.lujian213.simulator.util.BeanRepository;
import io.github.lujian213.simulator.util.ReqRespConvertor;
import io.github.lujian213.simulator.util.SimLogger;
import io.github.lujian213.simulator.util.SimUtils;
import io.grpc.BindableService;
import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.stub.StreamObserver;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class GRPCSimulator extends SimSimulator {

	protected int port;
	protected boolean useSSL;
	protected File certChainFile;
	protected File privateKeyFile;
	protected File trustCertFile;
	protected String clientAuthority;
	protected String handlerClassName;
	protected String handlerSimClassName;
	protected String clientClassName;
	protected Object handler;
	protected Server server;
	protected ReqRespConvertor convertor;
	protected BindableService simInstance;
	protected ThreadLocal<CallTrace<BindableService>> trace = new ThreadLocal<>();

	public static class CallTrace<T> {
		private String remoteAddress;
		private Method method;
		private T simInst;
		private Object result;
		private Object[] args;
		private Throwable throwable;

		public CallTrace() {
		}

		public String getRemoteAddress() {
			return remoteAddress;
		}

		public void setRemoteAddress(String remoteAddress) {
			this.remoteAddress = remoteAddress;
		}

		public Method getMethod() {
			return method;
		}

		public void setMethod(Method method) {
			this.method = method;
		}

		public T getSimInst() {
			return simInst;
		}

		public void setSimInst(T simInst) {
			this.simInst = simInst;
		}

		public Object getResult() {
			return result;
		}

		public void setResult(Object result) {
			this.result = result;
		}

		public Object[] getArgs() {
			return args;
		}

		public void setArgs(Object[] args) {
			this.args = args;
		}

		public Throwable getThrowable() {
			return throwable;
		}

		public void setThrowable(Throwable throwable) {
			this.throwable = throwable;
		}
	}


	public GRPCSimulator(SimScript script) throws IOException {
		super(script);
		convertor = SimUtils.createMessageConvertor(script, new DefaultGRPCReqRespConvertor());
	}

	protected GRPCSimulator() throws IOException {
	}

	@Override
	protected void init() throws IOException {
		super.init();
		port = script.getMandatoryIntProperty(PROP_NAME_PORT, "no socket port defined");
		useSSL = script.getBooleanProperty(PROP_NAME_USE_SSL, false);
		handlerClassName = script.getMandatoryProperty(PROP_NAME_HANDLER_CLASSNAME, "no handler class name defined");
		handlerSimClassName = script.getMandatoryProperty(PROP_NAME_HANDLER_SIM_CLASSNAME, "no handler sim class name defined");
		if (proxy) {
			clientClassName = script.getMandatoryProperty(PROP_NAME_CLIENT_CLASSNAME, "no client class name defined");
		}
		if (useSSL) {
			certChainFile = script.getMandatoryFileProperty(PROP_NAME_CERTCHAIN_FILE, "no certchain file defined");
			privateKeyFile = script.getMandatoryFileProperty(PROP_NAME_PRIVATEKEY_FILE, "no private key file defined");
		}

		if (proxy && useSSL) {
			trustCertFile = script.getMandatoryFileProperty(PROP_NAME_TRUSTCERT_FILE, "no trust cert file defined");
			clientAuthority = script.getProperty(PROP_NAME_CLIENT_AUTHORITY);
		}
	}

	@Override
	protected void doStart() throws IOException {
    	SimUtils.setThreadContext(script);
		Class<?> handlerClass = SimUtils.load(handlerClassName);
		Class<?> simClass = SimUtils.load(handlerSimClassName);
		Class<?> clientClass = proxy ? SimUtils.load(clientClassName) : null;
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(handlerClass);
		enhancer.setCallback(new MethodInterceptor() {
			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy mProxy) throws Throwable {
				GRPCSimRequest request = null;
				CallTrace<BindableService> ct = trace.get();
				List<SimResponse> respList = new ArrayList<>();
				try {
					ct.setMethod(method);
					ct.setArgs(args);
					ct.setSimInst(simInstance);
					request = new GRPCSimRequest(ct, convertor);
					SimUtils.logIncomingMessage(request.getRemoteAddress(), GRPCSimulator.this.getName(), request);
					respList = script.genResponse(request);
		    	} catch (IOException e) {
					if (proxy) {
						try (GRPCClient client = (useSSL? new GRPCClient(proxyURL, clientClass, trustCertFile, clientAuthority) : new GRPCClient(proxyURL, clientClass))) {
							Object result = client.invoke(method.getName(), args[0]);
							((StreamObserver)args[1]).onNext(result);
							((StreamObserver)args[1]).onCompleted();
							ct.setResult(result);
						} catch (Throwable t) {
							ct.setThrowable(t);
						}
						SimLogger.getLogger().info("call finished");
						respList.add(new SimResponse(200, null, null));
					} else {
						SimLogger.getLogger().error("match and fill error", e);
						ct.setThrowable(new IOException("no such method"));
					}
				} finally {
					castToSimulatorListener().onHandleMessage(getName(), request, respList, !respList.isEmpty());
		    	}
				if (ct.getThrowable() != null)
					throw ct.getThrowable();
				return ct.getResult();
			}
		});
		try {
			BindableService serviceInst = (BindableService) enhancer.create();
			simInstance = (BindableService) BeanRepository.getInstance().addBean(simClass, script.getConfigAsProperties()).getBean();
			ServerBuilder<?> builder = ServerBuilder.forPort(port);
			if (useSSL) {
				builder.useTransportSecurity(certChainFile, privateKeyFile);
			}
			server = builder.intercept(new ServerInterceptor() {
				@Override
				public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
						ServerCallHandler<ReqT, RespT> next) {
					SimUtils.setThreadContext(script);
					CallTrace<BindableService> ct = new CallTrace<>();
					trace.set(ct);
					ct.setRemoteAddress(call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR).toString());
					return next.startCall(call, headers);
				}
			}).addService(serviceInst).build();
			server.start();
			runningURL = "gRPC://" + InetAddress.getLocalHost().getHostName() + ":" + port;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	protected void doStop() {
		if (server != null) {
			server.shutdown();
		}
	}


	@Override
	public String getType() {
		return "gRPC";
	}
}