package org.jingle.simulator;

import java.io.File;
import java.io.IOException;

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
		sm.startSimulator(sm.getRootSimulatorName());
	}
	

	public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException {
		SimulatorService inst = new SimulatorService(new File(args[0]));
		inst.start();
	}
}
