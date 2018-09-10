package org.jingle.simulator.webbit;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.jingle.simulator.SimRequest;
import org.jingle.simulator.SimScript;
import org.jingle.simulator.http.HTTPSimulator;
import org.jingle.simulator.util.SimLogger;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;

public class WebbitSimulator extends HTTPSimulator implements HttpHandler {
	private WebServer webServer;
	
	public WebbitSimulator(SimScript script) throws IOException {
		super(script);
	}
	
	protected WebbitSimulator() {
	}

	@Override
	public void handleHttpRequest(HttpRequest req, HttpResponse resp, HttpControl ctrl) throws Exception {
		SimLogger.setLogger(script.getLogger());
		SimRequest request = null;
		try {
			request = new WebbitSimRequest(req, resp);
			handleRequest(request);
		} catch (Exception e) {
			SimLogger.getLogger().error("match and fill exception", e);
			gen500Response(request, e.getMessage() == null ? e.toString() : e.getMessage());
		}
		
	}
  
	@Override
	public void start() throws IOException {
		webServer = WebServers.createWebServer(port);
		for (Map.Entry<String, SimScript> entry: this.script.getSubScripts().entrySet()) {
			String channelName = "/" + reformatChannelName(entry.getKey());
			webServer.add(channelName, new WebbitWSHandler(channelName, entry.getValue()));
		}
		webServer.add(this);
		
		if (useSSL) {
			try (InputStream is = new FileInputStream(keystore)){
				webServer.setupSsl(is, ksPwd);
			} 
		}
        webServer.start();
        URI uri = webServer.getUri();
        this.runningURL = (useSSL ? "https" : "http") + "://" + uri.getHost() + ":" + uri.getPort();
        SimLogger.getLogger().info("Simulator [" + this.getName() + "] running at " + runningURL);
        this.running = true;
	}
	
	@Override
	protected void init() throws IOException {
		super.init();
		if (useSSL) {
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
		}
	}

	protected String reformatChannelName(String channelName) {
		return channelName.replaceAll("\\.", "/");
	}

	@Override
	public void stop() {
		SimLogger.getLogger().info("about to stop");
		webServer.stop();
		WebbitWSHandler.closeAllConnections();
		SimLogger.getLogger().info("stopped");
		this.running = false;
	}
}