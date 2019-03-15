package io.github.lujian213.simulator.webbit;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;

import io.github.lujian213.simulator.SimRequest;
import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.SimScript;
import io.github.lujian213.simulator.SimSesseionLessSimulator;
import io.github.lujian213.simulator.http.HTTPSimulator;
import io.github.lujian213.simulator.util.SimLogger;
import io.github.lujian213.simulator.util.SimUtils;
import static io.github.lujian213.simulator.SimSimulatorConstants.*;

public class WebbitSimulator extends HTTPSimulator implements HttpHandler, SimSesseionLessSimulator {
	private WebServer webServer;
	private Map<String, WebbitWSHandler> wsHandlerMap = new HashMap<>();
	
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
			WebbitWSHandler wsHandler = new WebbitWSHandler(this, castToSimulatorListener(), channelName, entry.getValue());
			wsHandlerMap.put(channelName, wsHandler);
			webServer.add(channelName, wsHandler);
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
		wsHandlerMap.values().stream().forEach((handler)-> handler.close());
		SimLogger.getLogger().info("stopped");
		this.running = false;
		this.runningURL = null;
	}

	@Override
	public void fillResponse(SimResponse response) throws IOException {
		Map<String, Object> headers = response.getHeaders();
		String channel = (String) headers.remove(HEADER_NAME_CHANNEL);
		String channelName = SimUtils.getBrokerName(channel);
		WebbitWSHandler wsHandler = wsHandlerMap.get(channelName);
		if (wsHandler == null) {
			throw new IOException("no such ws channel [" + channelName + "] exists");	
		}
		wsHandler.sendResponse(channel, response);
		SimLogger.getLogger().info("Use channel [" + channel + "] to send out message");
	}
}