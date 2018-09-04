package org.jingle.simulator;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class SimSimulator {
	protected SimScript script;
	protected boolean running = false;

	public SimSimulator(SimScript script) throws IOException {
		this.script = script;
		init();
	}

	protected SimSimulator() {
	}

	protected abstract void init() throws IOException;

	public String getName() {
		return script.getSimulatorName();
	}

	public abstract void start() throws IOException;

	public abstract void stop();

	public boolean isRunning() {
		return running;
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
}