package org.jingle.simulator;

import java.io.File;
import java.io.FileFilter;
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
	private SimScript script;
	private SimSimulator simulator = null;
	private SimulatorManager sm = new SimulatorManager();
	
	
	public SimulatorService(File folder) throws IOException {
		script = new SimScript(folder);
		script.prepareLogger();
		load(folder);
		simulator = SimSimulator.createSimulator(script);
		BeanRepository.getInstance().addBean(sm);
		logger.info("Simulator [" + simulator.getName() +"] loaded");
	}
	
	public void start() throws IOException {
		simulator.start();
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
			sm.addSimulator(simulator);
			logger.info("Simulator [" + simulator.getName() +"] loaded");
		}
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException {
		SimulatorService inst = new SimulatorService(new File(args[0]));
                                                                                                                                                                                                                                                               
//		inst.startSimulator("WebSocket");
//		inst.startSimulator("SimpleEcho");
//		inst.startSimulator("socket");
		inst.start();
//		try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		inst.stopSimulator("socket");
//		inst.stopSimulator("SimpleEcho");
//		inst.stopSimulator("WebSocket");
	}
}
