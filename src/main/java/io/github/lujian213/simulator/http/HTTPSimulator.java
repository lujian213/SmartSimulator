package io.github.lujian213.simulator.http;

import static io.github.lujian213.simulator.http.HTTPSimulatorConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import io.github.lujian213.simulator.SimRequest;
import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.SimScript;
import io.github.lujian213.simulator.SimSimulator;
import io.github.lujian213.simulator.util.ReqRespConvertor;
import io.github.lujian213.simulator.util.SimLogger;
import io.github.lujian213.simulator.util.SimUtils;

public abstract class HTTPSimulator extends SimSimulator {
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
	public String getType() {
		return "http/https";
	}

	@Override
	protected void init() throws IOException {
		super.init();
		port = script.getMandatoryIntProperty(PROP_NAME_PORT, "no http port defined");
		useSSL = script.getBooleanProperty(PROP_NAME_USE_SSL, false); 
		if (useSSL) {
			keystore = script.getMandatoryProperty(PROP_NAME_KEYSTORE, "no keystore defined");
			ksPwd = script.getMandatoryProperty(PROP_NAME_KS_PASSWD, "no keystore passwd defined");
		}
	}

	protected void handleRequest(SimRequest request) {
		List<SimResponse> respList = new ArrayList<>();
		try {
			SimUtils.logIncomingMessage(request.getRemoteAddress(), getName(), request);
			respList = script.genResponse(request);
		} catch (Exception e) {
			SimLogger.getLogger().info(e.toString() + "try proxy if proxy is setup");
			if (proxy) {
				try {
					SimResponse resp = SimUtils.doHttpProxy(proxyURL, request);
					request.fillResponse(resp);
					respList.add(resp);
				} catch (IOException e1) {
					SimLogger.getLogger().error("proxy error", e1);
					gen500Response(request, e1.getMessage() == null ? e1.toString() : e1.getMessage());
				}
			} else {
				SimLogger.getLogger().error("match and fill error", e);
				gen500Response(request, e.getMessage() == null ? e.toString() : e.getMessage());
			}
		}
		castToSimulatorListener().onHandleMessage(getName(), request, respList, !respList.isEmpty());
	}
	
	protected void gen500Response(SimRequest request, String message) {
		try {
			Map<String, Object> headers = new HashMap<>();
			headers.put("Access-Control-Allow-Origin", "*");
			SimResponse response = new SimResponse(500, headers, message.getBytes());
			request.fillResponse(response);
		} catch (Exception e) {
			SimLogger.getLogger().error("error when generate 500 response", e);
		}
	}

	@Override
	protected void doStart() throws IOException {
		if (useSSL) {
			sslContext = SimUtils.initSSL(keystore, ksPwd);
		}
	}
}
