package io.github.lujian213.simulator.manager;

import io.github.lujian213.simulator.SimScript;
import io.github.lujian213.simulator.SimSimulator;

public class SimulatorDetail {
	private String name;
	private String type = "UNKNOWN";
	private ScriptInfo scriptInfo;
	
	public SimulatorDetail(SimSimulator simulator) {
		this(simulator, false);
	}

	public SimulatorDetail(SimSimulator simulator, boolean raw) {
		this(simulator.getScript(), raw);
		this.type = simulator.getType();
	}

	public SimulatorDetail(SimScript simScript, boolean raw) {
		this.name = simScript.getSimulatorName();
		this.scriptInfo = new ScriptInfo("root", simScript, raw);
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
