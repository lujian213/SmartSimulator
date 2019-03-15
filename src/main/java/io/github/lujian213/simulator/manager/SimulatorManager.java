package io.github.lujian213.simulator.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

import io.github.lujian213.simulator.SimScript;
import io.github.lujian213.simulator.SimSimulator;
import io.github.lujian213.simulator.SimulatorRepository;
import io.github.lujian213.simulator.util.function.SimParam;

public class SimulatorManager {
	private SimulatorRepository rep = SimulatorRepository.getInstance();

	public List<SimSimulator> getAllSimulators() {
		return new ArrayList<> (rep.getAllSimulators());
	}

	public List<SimulatorStatus> getAllSimulatorStatus() {
		List<SimulatorStatus> ret = new ArrayList<>();

		for (SimSimulator simulator : rep.getAllSimulators()) {
			ret.add(new SimulatorStatus(simulator));
		}
		ret.sort((o1, o2)-> o1.getName().toUpperCase().compareTo(o2.getName().toUpperCase()));
		return ret;
	}

	public SimulatorDetail getSimulatorInfo(@SimParam("simulatorName") String name, @SimParam("_raw") boolean raw) {
		SimSimulator simulator = rep.getSimulator(name);
		return new SimulatorDetail(simulator, raw);
	}

	public String startSimulator(@SimParam("simulatorName") String name) throws IOException {
		SimSimulator sim = rep.startSimulator(name);
		return "Simulator [" + name + "] is running at " + sim.getRunningURL();
	}

	public String stopSimulator(@SimParam("simulatorName") String name) {
		rep.stopSimulator(name);
		return "Simulator [" + name + "] is stopped ";
	}

	public void refresh() throws IOException {
		rep.refresh();
	}

	public String restartSimulator(@SimParam("simulatorName") String name) throws IOException {
		SimSimulator sim = rep.restartSimulator(name);
		return "Simulator [" + name + "] is running at " + sim.getRunningURL();
	}

	public SimulatorFolder getSimulatorScript(@SimParam("simulatorFolder") String folder) throws IOException {
		SimScript simScript = rep.getSimulatorScript(folder);
		if (simScript.getMyself().isDirectory()) {
			return new SimulatorFolder(simScript);
		} else {
			return new SimulatorFolder(simScript, new ZipFile(simScript.getMyself()));
		}
	}

	public void createSimulatorScript(@SimParam("simulatorFolder") SimulatorFolder folder) throws IOException {
		System.out.println(folder);
//		SimScript simScript = rep.getSimulatorScript(folder);
//		if (simScript.getMyself().isDirectory()) {
//			return new SimulatorFolder(simScript);
//		} else {
//			return new SimulatorFolder(simScript, new ZipFile(simScript.getMyself()));
//		}
	}
}
