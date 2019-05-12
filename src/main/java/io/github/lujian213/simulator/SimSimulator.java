package io.github.lujian213.simulator;

import static io.github.lujian213.simulator.SimSimulatorConstants.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import io.github.lujian213.simulator.util.BeanRepository;
import io.github.lujian213.simulator.util.ListenerHub;
import io.github.lujian213.simulator.util.SimLogger;

public abstract class SimSimulator implements ListenerHub<SimulatorListener> {
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

	public abstract String getType();
	
	protected void init() throws IOException {
		proxy = Boolean.parseBoolean(script.getProperty(PROP_NAME_PROXY));
		if (proxy) {
			proxyURL = script.getMandatoryProperty(PROP_NAME_PROXY_URL, "no proxy url defined");
		}
	}

	public String getName() {
		return script.getSimulatorName();
	}

	public void start() throws IOException {
		synchronized (this) {
			if (!this.running) {
				try {
					String listenerStr = script.getProperty(PROP_NAME_LISTENER);
					if (listenerStr != null) {
						for (String listenerClass: listenerStr.split(",")) {
							try {
								SimulatorListener listener = SimulatorListener.class.cast(Class.forName(listenerClass.trim(), true, script.getClassLoader()).newInstance());
								listener.init(getName(), script.getConfigAsProperties());
								this.addListener(listener);
							} catch (Exception e) {
								throw new IOException("error when create simpulator listener", e);
							}
						}
					}
					doStart();
					postStart();
				} catch (RuntimeException|IOException e) {
					SimLogger.getLogger().error("error when start simulator [" + getName() + "]", e);
					try {
						doStop();
					} catch (Exception e1) {
					}
					this.listenerHub.removeAllListeners();
					throw e;
				}
			}
		}
	}
	
	protected abstract void doStart() throws IOException;

	protected void postStart() {
		SimLogger.getLogger().info("Simulator [" + this.getName() + "] running at " + runningURL);
		this.running = true;
		try {
			castToSimulatorListener().onStart(getName());
		} catch (Exception e) {
			SimLogger.getLogger().error("error when call listener", e);
		}
	}

	public void stop() {
		synchronized (this) {
			if (running) {
				SimLogger.getLogger().info("about to stop ...");
				try {
					doStop();
				} finally {
					SimLogger.getLogger().info("stopped");
					BeanRepository.getInstance().removeSimulatorBeans(getName());
					castToSimulatorListener().onStop(getName());
					this.listenerHub.removeAllListeners();
					this.script.close();
					this.running = false;
					this.runningURL = null;
				}
			}
		}
	}

	protected abstract void doStop();

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
	
	@Override
	public void addFixedListener(SimulatorListener listener) {
		listenerHub.addFixedListener(listener);
	}

	@Override
	public void removeAllListeners() {
		listenerHub.removeAllListeners();
	}

	protected SimulatorListener castToSimulatorListener() {
		return SimulatorListener.class.cast(listenerHub);
	}
}