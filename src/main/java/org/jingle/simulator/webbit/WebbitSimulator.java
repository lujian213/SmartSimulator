package org.jingle.simulator.webbit;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.jingle.simulator.SimRequest;
import org.jingle.simulator.SimScript;
import org.jingle.simulator.http.HTTPSimulator;
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

	protected void gen500Response(HttpResponse resp, String errorMsg) {
		try {
			byte[] response = errorMsg.getBytes();
			resp.content(response).status(500).end();
		} catch (Exception e) {
			logger.error("error when write 500 error response", e);
		}
	}
	
	@Override
	public void handleHttpRequest(HttpRequest req, HttpResponse resp, HttpControl ctrl) throws Exception {
		try {
			SimRequest request = new WebbitSimRequest(req, resp);
			handleRequest(request);
		} catch (Exception e) {
			logger.error("", e);
			gen500Response(resp, e.getMessage() == null ? e.toString() : e.getMessage());
		}
		
	}
  
	@Override
	public void start() throws IOException {
		webServer = WebServers.createWebServer(port);
		WebbitWSHandlerBundle bundle = new WebbitWSHandlerBundle();
		for (Map.Entry<String, SimScript> entry: this.script.getSubScripts().entrySet()) {
			String channelName = "/" + reformatChannelName(entry.getKey());
			webServer.add(channelName, new WebbitWSHandler(channelName, entry.getValue(), bundle));
		}
		webServer.add(this);
		
		if (useSSL) {
			try (InputStream is = new FileInputStream(keystore)){
				webServer.setupSsl(is, ksPwd);
			} 
		}
        webServer.start();
        logger.info("Simulator [" + this.getName() + "] running at " + webServer.getUri());
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
}