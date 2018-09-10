package org.jingle.simulator.manager;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jingle.simulator.SimScript;
import org.jingle.simulator.SimSimulator;
import org.jingle.simulator.util.SimLogger;
import org.jingle.simulator.util.SimParam;

public class SimulatorManager {
	private Map<String, SimSimulator> simulatorMap = new HashMap<>();
	private File folder;
	private SimScript rootScript;

	public SimulatorManager(File folder) throws IOException {
		this.folder = folder;
		refresh();

	}

	protected List<SimScript> loadScripts(File folder) throws IOException {
		List<SimScript> ret = new ArrayList<>();
		SimScript script = new SimScript(folder);
		script.prepareLogger();
		ret.add(script);

		File[] files = folder.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				if (file.isDirectory()) {
					return true;
				}
				return false;
			}
		});
		for (File file : files) {
			SimScript simScript = new SimScript(script, file);
			simScript.prepareLogger();
			ret.add(simScript);
		}
		return ret;
	}

	public String getRootSimulatorName() {
		return rootScript.getSimulatorName();
	}

	public List<SimulatorStatus> getAllSimulators() {
		List<SimulatorStatus> ret = new ArrayList<>();

		synchronized (simulatorMap) {
			for (SimSimulator simulator : simulatorMap.values()) {
				ret.add(new SimulatorStatus(simulator));
			}
		}
		return ret;
	}

	public SimulatorDetail getSimulatorInfo(@SimParam("simulatorName") String name) {
		SimSimulator simulator = null;
		synchronized (simulatorMap) {
			simulator = simulatorMap.get(name);
		}
		if (simulator == null) {
			throw new RuntimeException("no such simulator [" + name + "]");
		}
		return new SimulatorDetail(simulator);
	}

	public void startSimulator(@SimParam("simulatorName") String name) throws IOException {
		synchronized (simulatorMap) {
			SimSimulator sim = simulatorMap.get(name);
			if (sim == null) {
				throw new RuntimeException("no such simulator [" + name + "]");
			}
			if (!sim.isRunning()) {
				sim.start();
			}
		}
	}

	public void stopSimulator(@SimParam("simulatorName") String name) {
		synchronized (simulatorMap) {
			SimSimulator sim = simulatorMap.get(name);
			if (sim == null) {
				throw new RuntimeException("no such simulator [" + name + "]");
			}
			if (sim.isRunning()) {
				sim.stop();
			}
		}
	}

	public void refresh() throws IOException {
		List<SimScript> scripts = loadScripts(folder);
		synchronized (simulatorMap) {
			rootScript = scripts.get(0);
			Iterator<Map.Entry<String, SimSimulator>> it = simulatorMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, SimSimulator> entry = it.next();
				if (!entry.getValue().isRunning()) {
					SimLogger.getLogger().info("Simulator [" + entry.getKey() + "] is not running, remove ...");
					it.remove();
				}
			}
			for (SimScript script : scripts) {
				SimSimulator simulator = simulatorMap.get(script.getSimulatorName());
				if (simulator == null) {
					simulator = SimSimulator.createSimulator(script);
					simulatorMap.put(simulator.getName(), simulator);
					SimLogger.getLogger().info("Simulator [" + simulator.getName() + "] loaded");
				}
			}
		}
	}
}
