package io.github.lujian213.simulator.simple;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import javax.net.ssl.SSLParameters;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import io.github.lujian213.simulator.SimRequest;
import io.github.lujian213.simulator.SimScript;
import io.github.lujian213.simulator.http.HTTPSimulator;
import io.github.lujian213.simulator.util.SimLogger;
import io.github.lujian213.simulator.util.SimUtils;

/*
 * a simple static http server
*/
@SuppressWarnings("restriction")
public class SimpleSimulator extends HTTPSimulator {
	
	public class DefaultHttpHandler implements HttpHandler {

		@Override
		public void handle(HttpExchange exchange) {
			SimUtils.setThreadContext(script);
			SimRequest request = null;
			try {
				request = new SimpleSimRequest(exchange, convertor);
				handleRequest(request);
			} catch (Exception e) {
				SimLogger.getLogger().error("match and fill exception", e);
				SimpleSimulator.this.gen500Response(request, e.getMessage() == null ? e.toString() : e.getMessage());
			}
		}
	}

	private HttpServer server;
	
	public SimpleSimulator(SimScript script) throws IOException {
		super(script);		
		this.convertor = SimUtils.createMessageConvertor(script, new DefaultSimpleReqRespConvertor());
	}

	@Override
	protected void doStart() throws IOException {
		super.doStart();
		if (useSSL) {
			server = HttpsServer.create(new InetSocketAddress(port), 0);
		} else {
			server = HttpServer.create(new InetSocketAddress(port), 0);
		}
		server.createContext("/", new DefaultHttpHandler());
		server.setExecutor(null); // creates a default executor
		if (useSSL) {
			((HttpsServer) server).setHttpsConfigurator(new HttpsConfigurator(sslContext) {
				@Override
				public void configure(HttpsParameters params) {
					// get the default parameters
					SSLParameters sslparams = sslContext.getDefaultSSLParameters();
	
					params.setSSLParameters(sslparams);
				}
			});
		}
		server.start();
		runningURL = (useSSL ? "https://" : "http://") + InetAddress.getLocalHost().getHostName() + ":" + port;
	}

	@Override
	protected void doStop() {
		server.stop(0);
	}
}