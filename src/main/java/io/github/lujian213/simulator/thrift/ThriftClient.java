package io.github.lujian213.simulator.thrift;

import java.io.Closeable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class ThriftClient<T> implements Closeable{
	private TTransport transport;
	private Class<? extends T> clientClass;
	private T client = null;

	public ThriftClient(String urlStr, Class<? extends T> clientClass) throws TTransportException {
		try {
			URI url = new URI(urlStr);
			String host = url.getHost();
			int port = url.getPort();

			transport = new TSocket(host, port);
			transport.open();
			this.clientClass = clientClass;
			open();
		} catch (URISyntaxException e) {
			throw new TTransportException(e);
		}
	}

	public ThriftClient(String urlStr, String trustStore, String passwd, String trustManagerType, String trustStoreType, Class<? extends T> clientClass) throws TTransportException {
		try {
			URI url = new URI(urlStr);
			String host = url.getHost();
			int port = url.getPort();
	        TSSLTransportParameters params = new TSSLTransportParameters();
	        params.setTrustStore(trustStore, passwd, trustManagerType, trustStoreType);
	        transport = TSSLTransportFactory.getClientSocket(host, port, 0, params);
	        this.clientClass = clientClass;
	        open();
		} catch (URISyntaxException e) {
			throw new TTransportException(e);
		}
	}

	protected void open() throws TTransportException {
		TProtocol protocol = new TBinaryProtocol(transport);
		try {
			Constructor<? extends T> cons = clientClass.getConstructor(TProtocol.class);
			client = cons.newInstance(protocol);
		} catch (Exception e) {
			throw new TTransportException(e);
		}
	}

	public Object invoke(Method method, Object[] args) throws Throwable {
		try {
			return method.invoke(client, args);
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}

	@Override
	public void close() {
		transport.close();
	}
}