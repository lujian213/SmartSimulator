package org.jingle.simulator.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public static final String PROP_NAME_STATIC = "simulator.http.static";
	public static final String PROP_NAME_STATIC_ROOT = "simulator.http.static.root";
	public static final String PROP_NAME_STATIC_FOLDER = "simulator.http.static.folder";
	public static final String PROP_NAME_STATIC_MIME = "simulator.http.static.mime";
	protected int port;
	protected boolean useSSL;
	protected boolean staticWeb;
	protected String keystore;
	protected String ksPwd;
	protected SSLContext sslContext;
	protected ReqRespConvertor convertor;
	protected String webRoot;
	protected String webFolder;
	protected Map<String, String> mimeMap;

	
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
		staticWeb = script.getConfig().getBoolean(PROP_NAME_STATIC, false); 
		if (useSSL) {
			keystore = script.getMandatoryProperty(PROP_NAME_KEYSTORE, "no keystore defined");
			ksPwd = script.getMandatoryProperty(PROP_NAME_KS_PASSWD, "no keystore passwd defined");
		}
		if (staticWeb) {
			webRoot = script.getProperty(PROP_NAME_STATIC_ROOT);
			webFolder = script.getMandatoryProperty(PROP_NAME_STATIC_FOLDER, "no web folder defined");
			String mimeStr = script.getProperty(PROP_NAME_STATIC_MIME);
			mimeMap = new HashMap<>();
			if (mimeStr != null) {
				for (String mimeItem: mimeStr.split(",")) {
					mimeItem = mimeItem.trim();
					int index = mimeItem.indexOf('/');
					if (index != -1) {
						String key = mimeItem.substring(0, index);
						String value = mimeItem.substring(index + 1);
						mimeMap.put(key, value);
					}
				}
			}
		}
	}

	protected void handleRequest(SimRequest request) {
		List<SimResponse> respList = new ArrayList<>();
		try {
			SimLogger.getLogger().info("incoming request: [" + request.getTopLine() + "]");
			respList = script.genResponse(request);
		} catch (Exception e) {
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
			SimResponse response = new SimResponse(500, new HashMap<String, Object>(), message.getBytes());
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
