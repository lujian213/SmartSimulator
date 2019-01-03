package org.jingle.simulator.manager;

import org.jingle.simulator.SimSimulator;

public class SimulatorDetail {
	private String name;
	private String type;
	private ScriptInfo scriptInfo;
	
	public SimulatorDetail(SimSimulator simulator) {
		this.name = simulator.getName();
		this.type = simulator.getType();
		this.scriptInfo = new ScriptInfo("root", simulator.getScript());
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public ScriptInfo getScriptInfo() {
		return scriptInfo;
	}
	
	
}
