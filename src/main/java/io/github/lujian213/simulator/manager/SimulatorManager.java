package io.github.lujian213.simulator.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

	public SimulatorDetail evaluateSimulatorStructure(@SimParam("simulatorFolder") SimulatorFolder folder, @SimParam("_raw") boolean raw) throws IOException {
		SimScript simScript = new SimScript(rep.getSimulatorScript(null), folder);
		return new SimulatorDetail(simScript, raw);
	}
	
	public SimulatorFolder getSimulatorStructure(@SimParam("simulatorName") String name) throws IOException {
		SimScript simScript = rep.getSimulatorScript(name);
		return new SimulatorFolder(simScript);
	}

//	public void uploadSimulatorStructure(@SimParam("simulatorFolder") SimulatorFolder folder) throws IOException {
//		SimScript simScript = new SimScript(rep.getSimulatorScript(null), folder);
//		rep.
//	}
}
