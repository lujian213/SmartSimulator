package org.jingle.simulator;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.jingle.simulator.util.BeanRepository;
import org.jingle.simulator.util.ListenerHub;

public abstract class SimSimulator implements ListenerHub<SimulatorListener> {
	public static final String PROP_NAME_PROXY = "simulator.proxy";
	public static final String PROP_NAME_PROXY_URL = "simulator.proxy.url";
	public static final String PROP_NAME_MESSAGE_CONVERTOR = "simulator.messageconvertor";
	public static final String PROP_NAME_LISTENER = "simulator.listener";

	protected ListenerHub<SimulatorListener> listenerHub = ListenerHub.createListenerHub(SimulatorListener.class);
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
		String listenerStr = script.getProperty(PROP_NAME_LISTENER);
		if (listenerStr != null) {
			for (String listenerClass: listenerStr.split(",")) {
				try {
					this.addListener((SimulatorListener) Class.forName(listenerClass.trim(), true, script.getClassLoader()).newInstance());
				} catch (Exception e) {
					throw new IOException("error when create simpulator listener", e);
				}
			}
		}
		proxy = Boolean.parseBoolean(script.getProperty(PROP_NAME_PROXY));
		if (proxy) {
			proxyURL = script.getMandatoryProperty(PROP_NAME_PROXY_URL, "no proxy url defined");
		}
	}

	public String getName() {
		return script.getSimulatorName();
	}

	public void start() throws IOException {
		castToSimulatorListener().onStart(getName());
	}

	public void stop() {
		BeanRepository.getInstance().removeSimulatorBeans(getName());
		castToSimulatorListener().onStop(getName());
		this.script.close();
	}

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
	
	@Override
	public void addListener(SimulatorListener listener) {
		listenerHub.addListener(listener);
	}
	
	@Override
	public void removeListener(SimulatorListener listener) {
		listenerHub.removeListener(listener);
	}
	
	protected SimulatorListener castToSimulatorListener() {
		return SimulatorListener.class.cast(listenerHub);
	}

}