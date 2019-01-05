package org.jingle.simulator.manager;

import org.jingle.simulator.SimSimulator;

public class SimulatorDetail {
	private String name;
	private String type;
	private ScriptInfo scriptInfo;
	
	public SimulatorDetail(SimSimulator simulator) {
		this(simulator, false);
	}

	public SimulatorDetail(SimSimulator simulator, boolean raw) {
		this.name = simulator.getName();
		this.type = simulator.getType();
		this.scriptInfo = new ScriptInfo("root", simulator.getScript(), raw);
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
