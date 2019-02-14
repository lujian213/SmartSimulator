package io.github.lujian213.simulator.webbit;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;

import io.github.lujian213.simulator.SimRequest;
import io.github.lujian213.simulator.SimScript;
import io.github.lujian213.simulator.http.HTTPSimulator;
import io.github.lujian213.simulator.util.SimLogger;
import io.github.lujian213.simulator.util.SimUtils;

public class WebbitSimulator extends HTTPSimulator implements HttpHandler {
	private WebServer webServer;
	
	public WebbitSimulator(SimScript script) throws IOException {
		super(script);
		this.convertor = SimUtils.createMessageConvertor(script, new DefaultWebbitReqRespConvertor());
	}
	
	protected WebbitSimulator() {
	}

	@Override
	public void handleHttpRequest(HttpRequest req, HttpResponse resp, HttpControl ctrl) throws Exception {
		SimUtils.setThreadContext(script);
		SimRequest request = null;
		try {
			request = new WebbitSimRequest(req, resp, convertor);
			handleRequest(request);
		} catch (Exception e) {
			SimLogger.getLogger().error("match and fill exception", e);
			gen500Response(request, e.getMessage() == null ? e.toString() : e.getMessage());
		}
		
	}
  
	@Override
	protected void doStart() throws IOException {
		super.doStart();
		webServer = WebServers.createWebServer(port);
		for (Map.Entry<String, SimScript> entry: this.script.getSubScripts().entrySet()) {
			String channelName = "/" + reformatChannelName(entry.getKey());
			webServer.add(channelName, new WebbitWSHandler(this, castToSimulatorListener(), channelName, entry.getValue()));
		}
		webServer.add(this);
		
		if (useSSL) {
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
			try (InputStream is = new FileInputStream(keystore)){
				webServer.setupSsl(is, ksPwd);
			} 
		}
        webServer.start();
        URI uri = webServer.getUri();
        this.runningURL = (useSSL ? "https" : "http") + "://" + uri.getHost() + ":" + uri.getPort();
	}
	
	protected String reformatChannelName(String channelName) {
		return channelName.replaceAll("\\.", "/");
	}

	@Override
	public void stop() {
		super.stop();
		SimLogger.getLogger().info("about to stop");
		webServer.stop();
		WebbitWSHandler.closeAllConnections();
		SimLogger.getLogger().info("stopped");
		this.running = false;
		this.runningURL = null;
	}
}