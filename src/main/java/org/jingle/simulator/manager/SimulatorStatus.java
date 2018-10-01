package org.jingle.simulator.manager;

import org.jingle.simulator.SimSimulator;

public class SimulatorStatus {
	private String name;
	private String type;
	private String status;
	private String runningURL;
	
	public SimulatorStatus(SimSimulator simulator) {
		this.name = simulator.getName();
		this.type = simulator.getClass().getName();
		this.status = simulator.isRunning() ? "running" : "stopped";
		this.runningURL = simulator.getRunningURL();
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getStatus() {
		return status;
	}

	public String getRunningURL() {
		return runningURL;
	}
}
