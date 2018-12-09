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
			SimLogger.setLogger(script.getLogger());
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

	public class StaticFileHandler implements HttpHandler {
		private MimetypesFileTypeMap map;
		private final int BUFFER_SIZE = 64 * 1024;
		public StaticFileHandler() {
			map = (MimetypesFileTypeMap) MimetypesFileTypeMap.getDefaultFileTypeMap();
			for (Map.Entry<String, String> entry: mimeMap.entrySet()) {
				map.addMimeTypes(entry.getValue() + " " + entry.getKey());
			}
		}

		@Override
		public void handle(HttpExchange exchange) {
			try {
			SimLogger.setLogger(script.getLogger());
			URI uri = exchange.getRequestURI();
			String path = uri.getPath();
			File file = new File(webFolder + path).getCanonicalFile();
			SimLogger.getLogger().info("looking for: " + file);

			if (!file.isFile()) {
				// Object does not exist or is not a file: reject with 404 error.
				String response = "404 (Not Found)\n";
				exchange.sendResponseHeaders(404, response.length());
				try (OutputStream os = exchange.getResponseBody()) {
					os.write(response.getBytes());
				} catch (IOException e) {
					SimLogger.getLogger().error("error when write 404 error message", e);
				}
			} else {
				// Object exists and is a file: accept with response code 200.
				Headers h = exchange.getResponseHeaders();
				h.set("Content-Type", map.getContentType(file));
				exchange.sendResponseHeaders(200, 0);
				try (OutputStream os = exchange.getResponseBody();
						BufferedInputStream fs = new BufferedInputStream(new FileInputStream(file))) {
					final byte[] buffer = new byte[BUFFER_SIZE];
					int count = 0;
					while ((count = fs.read(buffer)) >= 0) {
						os.write(buffer, 0, count);
					}
				}
			}
			} catch (Exception e) {
				SimLogger.getLogger().error(e);
			}
		}
	}

	private HttpServer server;
	
	public SimpleSimulator(SimScript script) throws IOException {
		super(script);		
		this.convertor = SimUtils.createMessageConvertor(script, new DefaultSimpleReqRespConvertor());
	}

	@Override
	public void start() throws IOException {
		if (useSSL) {
			server = HttpsServer.create(new InetSocketAddress(port), 0);
		} else {
			server = HttpServer.create(new InetSocketAddress(port), 0);
		}
		if (staticWeb) {
			if (webRoot == null) {
				server.createContext("/", new StaticFileHandler());
			} else {
				server.createContext(webRoot, new StaticFileHandler());
				server.createContext("/", new DefaultHttpHandler());
			}
		} else {
			server.createContext("/", new DefaultHttpHandler());
		}
		
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