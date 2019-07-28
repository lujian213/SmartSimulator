package io.github.lujian213.simulator.mem;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import io.github.lujian213.simulator.SimScript;

public class MemSimulatorTest {
	private static MemSimulator simulator;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		SimScript script = new SimScript(new File("scripts"));
		simulator = new MemSimulator(new SimScript(script, new File("scripts/mem")));
	}

}
