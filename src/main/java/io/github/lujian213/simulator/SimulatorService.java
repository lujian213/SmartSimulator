package io.github.lujian213.simulator;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import io.github.lujian213.simulator.util.SimLogger;

public class SimulatorService {
	private static final Logger logger = Logger.getLogger(SimulatorService.class);

	static {
		SimLogger.setLogger(logger);
	}
	private SimulatorRepository sr;
	
	
	public SimulatorService(File folder) throws IOException {
		sr = new SimulatorRepository(folder);
	}
	
	public void start() throws IOException {
		sr.getAllSimulators().stream().
		filter((sim) -> sim.getScript().getConfig().getBoolean(SimScript.PROP_NAME_SIMULATOR_AUTOSTART, false))
		.forEach((sim) -> {
			try {
				sr.startSimulator(sim.getScript().getSimulatorName());
			} catch (Exception e) {
				logger.error("start simultor [" + sim.getName() + "] error", e);
			}
		});
	}
	

	public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException {
		SimulatorService inst = new SimulatorService(new File(args[0]));
		inst.start();
	}
}
