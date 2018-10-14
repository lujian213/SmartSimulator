package org.jingle.simulator;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.jingle.simulator.manager.SimulatorManager;
import org.jingle.simulator.util.BeanRepository;
import org.jingle.simulator.util.SimLogger;

public class SimulatorService {
	private static final Logger logger = Logger.getLogger(SimulatorService.class);

	static {
		SimLogger.setLogger(logger);
	}
	private SimulatorManager sm;
	
	
	public SimulatorService(File folder) throws IOException {
		sm = new SimulatorManager(folder);
		BeanRepository.getInstance().addBean(sm);
	}
	
	public void start() throws IOException {
		sm.getAllSimulators().stream().
		filter((sim) -> sim.getScript().getConfig().getBoolean(SimScript.PROP_NAME_SIMULATOR_AUTOSTART, false))
		.forEach((sim) -> {
			try {
				sm.startSimulator(sim.getScript().getSimulatorName());
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
