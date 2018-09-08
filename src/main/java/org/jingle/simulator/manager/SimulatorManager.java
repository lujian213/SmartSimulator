package org.jingle.simulator.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Produces;

import org.jingle.simulator.SimSimulator;
import org.jingle.simulator.util.SimParam;

public class SimulatorManager {
	private Map<String, SimSimulator> simulatorMap = new HashMap<>();

	public SimulatorManager() {
		
	}
	
	public void addSimulator(SimSimulator simulator) {
		synchronized(simulatorMap) {
			simulatorMap.put(simulator.getName(), simulator);
		}
	}
	
	public List<SimulatorStatus> getAllSimulators() {
		List<SimulatorStatus> ret = new ArrayList<>();
				
		synchronized(simulatorMap) {
			for (SimSimulator simulator: simulatorMap.values()) {
				ret.add(new SimulatorStatus(simulator));
			}
		}
		return ret;
	}
	
	public SimulatorDetail getSimulatorInfo(@SimParam("simulatorName") String name) {
		SimSimulator simulator = null;
		synchronized(simulatorMap) {
			simulator = simulatorMap.get(name);
		}
		if (simulator == null) {
			throw new RuntimeException("no such simulator [" + name +"]");
		}
		return new SimulatorDetail(simulator);
	}
	
	public void startSimulator(@SimParam("simulatorName") String name) throws IOException {
		SimSimulator sim = simulatorMap.get(name);
		if (sim == null) {
			throw new RuntimeException("no such simulator [" + name + "]");
		} 
		if (!sim.isRunning()) {
			sim.start();
		}
	}
	
	public void stopSimulator(@SimParam("simulatorName") String name) {
		SimSimulator sim = simulatorMap.get(name);
		if (sim == null) {
			throw new RuntimeException("no such simulator [" + name + "]");
		} 
		if (sim.isRunning()) {
			sim.stop();
		}
	}
	

}
