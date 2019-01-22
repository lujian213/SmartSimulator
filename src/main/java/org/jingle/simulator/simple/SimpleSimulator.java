package org.jingle.simulator.simple;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;
import javax.net.ssl.SSLParameters;

import org.jingle.simulator.SimRequest;
import org.jingle.simulator.SimScript;
import org.jingle.simulator.http.HTTPSimulator;
import org.jingle.simulator.util.SimLogger;
import org.jingle.simulator.util.SimUtils;

import com.sun.net.httpserver.Headers;
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
	public void stop() {
		super.stop();
		SimLogger.getLogger().info("about to stop ...");
		server.stop(0);
		SimLogger.getLogger().info("stopped");
		this.running = false;
		this.runningURL = null;
	}
}