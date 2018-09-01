package org.jingle.simulator.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import javax.net.ssl.SSLContext;

import org.jingle.simulator.SimRequest;
import org.jingle.simulator.SimResponse;
import org.jingle.simulator.SimScript;
import org.jingle.simulator.SimSimulator;
import org.jingle.simulator.util.SimUtils;

public abstract class HTTPSimulator extends SimSimulator { 
	public static final String PROP_NAME_PORT = "simulator.http.port";
	public static final String PROP_NAME_USE_SSL = "simulator.http.useSSL";
	public static final String PROP_NAME_KEYSTORE = "simulator.http.keystore";
	public static final String PROP_NAME_KS_PASSWD = "simulator.http.keystore.password";
	public static final String PROP_NAME_PROXY = "simulator.http.proxy";
	public static final String PROP_NAME_PROXY_URL = "simulator.http.proxy.url";
	protected int port;
	protected boolean useSSL;
	protected String keystore;
	protected String ksPwd;
	protected SSLContext sslContext;	
	protected boolean proxy;
	protected String proxyURL;
	
	public HTTPSimulator(SimScript script) throws IOException {
		super(script);
	}
	
	protected HTTPSimulator() {
	}

	@Override
	protected void init() throws IOException {
		Properties props = script.getProps();
		port = Integer.parseInt(props.getProperty(PROP_NAME_PORT, "8080"));
		useSSL = Boolean.parseBoolean(props.getProperty(PROP_NAME_USE_SSL, "false"));
		proxy = Boolean.parseBoolean(props.getProperty(PROP_NAME_PROXY, "false"));
		if (useSSL) {
			keystore = props.getProperty(PROP_NAME_KEYSTORE);
			ksPwd = props.getProperty(PROP_NAME_KS_PASSWD);
			if (keystore == null || ksPwd == null) {
				throw new RuntimeException("no keystore or keystore passwd defined");
			}
			sslContext = SimUtils.initSSL(keystore, ksPwd);
		}
		if (proxy) {
			proxyURL = props.getProperty(PROP_NAME_PROXY_URL);
			if (proxyURL == null) {
				throw new RuntimeException("no proxy.url defined");
			}
		}
	}
	
	protected void handleRequest(SimRequest request) {
		try {
			logger.info("incoming request: [" + request.getTopLine() + "]");
			script.genResponse(request);
		} catch (Exception e) {
			if (proxy) {
				try {
					SimResponse resp = SimUtils.doProxy(proxyURL, request);
					request.fillResponse(resp);
				} catch (IOException e1) {
					logger.error("proxy error", e1);
					gen500Response(request, e1.getMessage() == null ? e1.toString() : e1.getMessage());
				}
			} else {
				logger.error("match and fill error", e);
				gen500Response(request, e.getMessage() == null ? e.toString() : e.getMessage());
			}
		}
	}
	
	protected void gen500Response(SimRequest request, String message) {
		try {
			SimResponse response = new SimResponse(500, new HashMap<String, Object>(), message.getBytes());
			request.fillResponse(response);
		} catch (Exception e) {
			logger.error("error when generate 500 response", e);
		}
	}

}
