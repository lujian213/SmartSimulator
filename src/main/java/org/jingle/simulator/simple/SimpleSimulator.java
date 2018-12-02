package org.jingle.simulator.simple;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import javax.net.ssl.SSLParameters;

import org.jingle.simulator.SimRequest;
import org.jingle.simulator.SimScript;
import org.jingle.simulator.http.HTTPSimulator;
import org.jingle.simulator.util.SimLogger;
import org.jingle.simulator.util.SimUtils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

/*
 * a simple static http server
*/
@SuppressWarnings("restriction")
public class SimpleSimulator extends HTTPSimulator implements HttpHandler {
	private HttpServer server;
	
	public SimpleSimulator(SimScript script) throws IOException {
		super(script);		
		this.convertor = SimUtils.createMessageConvertor(script, new DefaultSimpleReqRespConvertor());

	}
  
	@Override
	public void handle(HttpExchange exchange) {
		SimLogger.setLogger(script.getLogger());
		SimRequest request = null;
		try {
			request = new SimpleSimRequest(exchange, convertor);
			handleRequest(request);
		} catch (Exception e) {
			SimLogger.getLogger().error("match and fill exception", e);
			gen500Response(request, e.getMessage() == null ? e.toString() : e.getMessage());
		}
	}

	protected void gen500Response(HttpExchange exchange, String errorMsg) {
		try {
			byte[] response = errorMsg.getBytes();
			exchange.sendResponseHeaders(500, response.length);
			try (OutputStream os = exchange.getResponseBody()) {
				os.write(response);
			}
		} catch (Exception e) {
			SimLogger.getLogger().error("error when write 500 error response", e);
		}
	}

	@Override
	public void start() throws IOException {
		if (useSSL) {
			server = HttpsServer.create(new InetSocketAddress(port), 0);
		} else {
			server = HttpServer.create(new InetSocketAddress(port), 0);
		}
		server.createContext("/", this);
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
		SimLogger.getLogger().info("Simulator [" + this.getName() + "] running at " + runningURL);
		this.running = true;
		super.start();
	}

	@Override
	public void stop() {
		super.stop();
		SimLogger.getLogger().info("about to stop ...");
		server.stop(0);
		SimLogger.getLogger().info("stopped");
		this.running = false;
		this.runningURL = null;
	}
}