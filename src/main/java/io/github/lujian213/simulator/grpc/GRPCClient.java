package io.github.lujian213.simulator.grpc;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;

import javax.net.ssl.SSLException;

import io.github.lujian213.simulator.util.SimLogger;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;

public class GRPCClient<T> implements Closeable{
	private ManagedChannel channel;
	private Class<? extends T> grpcClass;
	private T client = null;

	public GRPCClient(String urlStr, Class<? extends T> grpcClass) throws IOException {
		try {
			URI url = new URI(urlStr);
			String host = url.getHost();
			int port = url.getPort();
			channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();;
			this.grpcClass = grpcClass;
			open();
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}

	public GRPCClient(String urlStr, Class<? extends T> grpcClass, File trustedCertFile, String authority) throws IOException {
		try {
			URI url = new URI(urlStr);
			String host = url.getHost();
			int port = url.getPort();
			channel = NettyChannelBuilder.forAddress(host, port)
			        .overrideAuthority(authority)
			        .sslContext(buildSslContext(trustedCertFile, null, null))
			        .build();
			this.grpcClass = grpcClass;
			open();
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}
	
	private SslContext buildSslContext(File trustCertCollectionFilePath, File clientCertChainFilePath,
			File clientPrivateKeyFilePath) throws SSLException {
		SslContextBuilder builder = GrpcSslContexts.forClient();
		if (trustCertCollectionFilePath != null) {
			builder.trustManager(trustCertCollectionFilePath);
		}
		if (clientCertChainFilePath != null && clientPrivateKeyFilePath != null) {
			builder.keyManager(clientCertChainFilePath, clientPrivateKeyFilePath);
		}
		return builder.build();
	}


	protected void open() throws IOException {
		try {
			Method newStubMethod = grpcClass.getMethod("newBlockingStub", Channel.class);
			client = (T) newStubMethod.invoke(null, channel);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	public Object invoke(String methodName, Object request) throws Throwable {
		try {
			Method method = findMethod(methodName);
			Object result =  method.invoke(client, request);
			return result;
		} catch (InvocationTargetException e) {
			SimLogger.getLogger().error("error when invoke remote", e.getCause());
			throw e.getCause();
		}
	}
	
	protected Method findMethod(String name) {
		Method[] methods = client.getClass().getMethods();
		for (Method m : methods) {
			if (m.getName().equals(name)) {
				return m;
			}
		}
		return null;
	}
	

	@Override
	public void close() {
		channel.shutdown();
	}
}