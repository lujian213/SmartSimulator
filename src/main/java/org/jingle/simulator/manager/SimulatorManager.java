package org.jingle.simulator.manager;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipFile;

import org.jingle.simulator.SimScript;
import org.jingle.simulator.SimSimulator;
import org.jingle.simulator.util.SimLogger;
import org.jingle.simulator.util.SimParam;

public class SimulatorManager {
	private Map<String, SimSimulator> simulatorMap = new HashMap<>();
	private File folder;

	public SimulatorManager(File folder) throws IOException {
		this.folder = folder;
		refresh();

	}

	protected List<SimScript> loadScripts(File folder) throws IOException {
		List<SimScript> ret = new ArrayList<>();
		SimScript script = new SimScript(folder);

		File[] files = folder.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				if (file.isDirectory() || file.getName().endsWith(SimScript.SCRIPT_ZIP)) {
					return true;
				}
				return false;
			}
		});
		for (File file : files) {
			SimScript simScript = null;
			if (file.isDirectory()) {
				simScript = new SimScript(script, file);
			} else {
				simScript = new SimScript(script, new ZipFile(file));
			}
			if (!simScript.isValid()) {
				SimLogger.getLogger().info(file.getName() + " is not a valid script folder/file, skip ...");
			} else {
				simScript.prepareLogger();
				ret.add(simScript);
			}
		}
		return ret;
	}
	
	public List<SimSimulator> getAllSimulators() {
		List<SimSimulator> ret = new ArrayList<>();

		synchronized (simulatorMap) {
			ret.addAll(simulatorMap.values());
		}
		return ret;
	}

	public List<SimulatorStatus> getAllSimulatorStatus() {
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

	public String startSimulator(@SimParam("simulatorName") String name) throws IOException {
		synchronized (simulatorMap) {
			SimSimulator sim = simulatorMap.get(name);
			if (sim == null) {
				throw new RuntimeException("no such simulator [" + name + "]");
			}
			if (!sim.isRunning()) {
				sim.start();
			}
			return "Simulator [" + name + "] is running at " + sim.getRunningURL();
		}
	}

	public String stopSimulator(@SimParam("simulatorName") String name) {
		synchronized (simulatorMap) {
			SimSimulator sim = simulatorMap.get(name);
			if (sim == null) {
				throw new RuntimeException("no such simulator [" + name + "]");
			}
			if (sim.isRunning()) {
				sim.stop();
			}
			return "Simulator [" + name + "] is stopped ";
		}
	}

	public void refresh() throws IOException {
		List<SimScript> scripts = loadScripts(folder);
		synchronized (simulatorMap) {
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

	public String restartSimulator(@SimParam("simulatorName") String name) throws IOException {
		List<SimScript> scripts = loadScripts(folder);
		synchronized (simulatorMap) {
			SimSimulator sim = simulatorMap.get(name);
			if (sim != null) {
				if (sim.isRunning()) {
					sim.stop();
				}
			}
			Optional<SimScript> opSc = scripts.stream().filter((script) -> name.equals(script.getSimulatorName())).findFirst();
			if (!opSc.isPresent()) {
				throw new RuntimeException("no such simulator [" + name + "]");
			}
			sim = SimSimulator.createSimulator(opSc.get());
			simulatorMap.put(sim.getName(), sim);
			sim.start();

			return "Simulator [" + name + "] is running at " + sim.getRunningURL();
		}
	}
}
