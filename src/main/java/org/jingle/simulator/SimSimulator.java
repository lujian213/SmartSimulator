package org.jingle.simulator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;

public abstract class SimSimulator {
	protected static final Logger logger = Logger.getLogger(SimSimulator.class);
	protected SimScript script;
	
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
	
	public static SimSimulator createSimulator(File home, File folder) throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		SimScript script = new SimScript(home, folder);
		Class<? extends SimSimulator> clazz = script.getSimulatorClass();
		Constructor<? extends SimSimulator> con = clazz.getConstructor(SimScript.class);
		try {
			return con.newInstance(script);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getCause());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		if (args.length < 1) {
			throw new RuntimeException("no simulator scripts folder provided");
		}
		SimSimulator inst = null;
		if (args.length == 1) {
			inst = SimSimulator.createSimulator(new File(args[0]), new File(args[0] + "/.."));
		} else {
			inst = SimSimulator.createSimulator(new File(args[0]), new File(args[1]));
		}
		inst.start();
	}
}