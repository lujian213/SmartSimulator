package org.jingle.simulator;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jingle.simulator.util.SimLogger;

public class SimulatorService {
	private static final Logger logger = Logger.getLogger(SimulatorService.class);
	static {
		SimLogger.setLogger(logger);
	}
	private SimScript script;
	private Map<String, SimSimulator> simulatorMap = new HashMap<>();
	
	public SimulatorService(File folder) throws IOException {
		this.script = new SimScript(folder);
		load(folder);
	}
	
	public List<SimSimulator> getAllSimulators() {
		return new ArrayList<>(simulatorMap.values());
	}
	
	public void start() {
		
	}
	
	public void startSimulator(String name) throws IOException {
		SimSimulator sim = simulatorMap.get(name);
		if (sim == null) {
			throw new RuntimeException("no such simulator [" + name + "]");
		} 
		if (!sim.isRunning()) {
			sim.start();
		}
	}
	
	public void stopSimulator(String name) {
		SimSimulator sim = simulatorMap.get(name);
		if (sim == null) {
			throw new RuntimeException("no such simulator [" + name + "]");
		} 
		if (sim.isRunning()) {
			sim.stop();
		}
	}
	
	protected void load(File folder) throws IOException {
		File[] files = folder.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				if (file.isDirectory()) {
					return true;
				}
				return false;
			}
		});
		for (File file: files) {
			SimScript simScript = new SimScript(script, file);
			simScript.prepareLogger();
			SimSimulator simulator = SimSimulator.createSimulator(simScript);
			simulatorMap.put(simulator.getName(), simulator);
			logger.info("Simulator [" + simulator.getName() +"] loaded");
		}
	}
	
	
//	public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException {
//		SimulatorService inst = new SimulatorService(new File(args[0]));
//		inst.startSimulator("WebSocket");
//		inst.startSimulator("SimpleEcho");
//		inst.startSimulator("socket");
//		inst.start();
//		try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		inst.stopSimulator("socket");
//		inst.stopSimulator("SimpleEcho");
//		inst.stopSimulator("WebSocket");
//	}
}
