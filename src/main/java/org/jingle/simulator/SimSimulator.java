package org.jingle.simulator;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class SimSimulator {
	public static final String PROP_NAME_PROXY = "simulator.proxy";
	public static final String PROP_NAME_PROXY_URL = "simulator.proxy.url";
	public static final String PROP_NAME_MESSAGE_CONVERTOR = "simulator.messageconvertor";

	protected SimScript script;
	protected boolean running = false;
	protected String runningURL = null;
	protected boolean proxy;
	protected String proxyURL;

	public SimSimulator(SimScript script) throws IOException {
		this.script = script;
		init();
	}

	protected SimSimulator() {
	}

	protected void init() throws IOException {
		proxy = Boolean.parseBoolean(script.getProperty(PROP_NAME_PROXY));
		if (proxy) {
			proxyURL = script.getMandatoryProperty(PROP_NAME_PROXY_URL, "no proxy url defined");
		}
	}

	public String getName() {
		return script.getSimulatorName();
	}

	public abstract void start() throws IOException;

	public abstract void stop();

	public boolean isRunning() {
		return running;
	}

	public String getRunningURL() {
		if (running) {
			return runningURL;
		} else {
			return null;
		}
	}

	public static SimSimulator createSimulator(SimScript script) {
		try {
			Class<? extends SimSimulator> clazz = script.getSimulatorClass();
			Constructor<? extends SimSimulator> con = clazz.getConstructor(SimScript.class);
			return con.newInstance(script);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getCause());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public SimScript getScript() {
		return this.script;
	}
}