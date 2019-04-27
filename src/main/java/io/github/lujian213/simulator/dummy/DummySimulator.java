package io.github.lujian213.simulator.dummy;

import java.io.IOException;

import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.SimScript;
import io.github.lujian213.simulator.SimSesseionLessSimulator;
import io.github.lujian213.simulator.SimSimulator;

public class DummySimulator extends SimSimulator implements SimSesseionLessSimulator {
	
	public DummySimulator(SimScript script) throws IOException {
		super(script);
	}
	
	protected DummySimulator() {
	}
	
	@Override
	protected void init() throws IOException {
		super.init();
	}

	@Override
	protected void doStart() throws IOException {
		this.runningURL = "Dummy";
	}

	@Override
	protected void doStop() {
	}

	@Override
	public String getType() {
		return "Dummy";
	}

	@Override
	public void fillResponse(SimResponse response) throws IOException {
	}
}
