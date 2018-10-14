package org.jingle.simulator.http;

import java.io.IOException;
import java.util.HashMap;

import javax.net.ssl.SSLContext;

import org.jingle.simulator.SimRequest;
import org.jingle.simulator.SimResponse;
import org.jingle.simulator.SimScript;
import org.jingle.simulator.SimSimulator;
import org.jingle.simulator.util.ReqRespConvertor;
import org.jingle.simulator.util.SimLogger;
import org.jingle.simulator.util.SimUtils;

public abstract class HTTPSimulator extends SimSimulator {
	public static final String PROP_NAME_PORT = "simulator.http.port";
	public static final String PROP_NAME_USE_SSL = "simulator.http.useSSL";
	public static final String PROP_NAME_KEYSTORE = "simulator.http.keystore";
	public static final String PROP_NAME_KS_PASSWD = "simulator.http.keystore.password";
	protected int port;
	protected boolean useSSL;
	protected String keystore;
	protected String ksPwd;
	protected SSLContext sslContext;
	protected ReqRespConvertor convertor;

	public HTTPSimulator(SimScript script) throws IOException {
		super(script);
	}

	protected HTTPSimulator() {
	}

	@Override
	protected void init() throws IOException {
		super.init();
		port = script.getMandatoryIntProperty(PROP_NAME_PORT, "no http port defined");
		useSSL = script.getConfig().getBoolean(PROP_NAME_USE_SSL, false); 
		if (useSSL) {
			keystore = script.getMandatoryProperty(PROP_NAME_KEYSTORE, "no keystore defined");
			ksPwd = script.getMandatoryProperty(PROP_NAME_KS_PASSWD, "no keystore passwd defined");
			sslContext = SimUtils.initSSL(keystore, ksPwd);
		}
	}

	protected void handleRequest(SimRequest request) {
		try {
			SimLogger.getLogger().info("incoming request: [" + request.getTopLine() + "]");
			script.genResponse(request);
		} catch (Exception e) {
			if (proxy) {
				try {
					SimResponse resp = SimUtils.doHttpProxy(proxyURL, request);
					request.fillResponse(resp);
				} catch (IOException e1) {
					SimLogger.getLogger().error("proxy error", e1);
					gen500Response(request, e1.getMessage() == null ? e1.toString() : e1.getMessage());
				}
			} else {
				SimLogger.getLogger().error("match and fill error", e);
				gen500Response(request, e.getMessage() == null ? e.toString() : e.getMessage());
			}
		}
	}

	protected void gen500Response(SimRequest request, String message) {
		try {
			SimResponse response = new SimResponse(500, new HashMap<String, Object>(), message.getBytes());
			request.fillResponse(response);
		} catch (Exception e) {
			SimLogger.getLogger().error("error when generate 500 response", e);
		}
	}

}
