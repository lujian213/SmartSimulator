package io.github.lujian213.simulator;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import io.github.lujian213.simulator.SimSimulator;
import io.github.lujian213.simulator.SimulatorRepository;

public class SimulatorRepositoryTest {
	@Test
	public void test() {
		SimulatorRepository rep = null;
		try {
			rep = new SimulatorRepository(new File("scripts"));
		} catch (IOException e1) {
			fail("unexpected exception:" + e1);
		}
		try {
			rep.getSimulator("dummy");
			fail("RuntimeException expected");
		} catch (RuntimeException e) {
		}
	}

	@Test
	public void test2() {
		try {
			SimulatorRepository rep = new SimulatorRepository(new File("scripts"));
			SimSimulator simulator = rep.getSimulator("foo");
			assertEquals(1, simulator.getScript().getSubScripts().size());
			assertNotNull(simulator.getScript().getSubScripts().get("foo2"));
		} catch (IOException e) {
			fail("unexpected exception:" + e);
		}
	}

	@Test
	public void test3() {
		SimulatorRepository rep = null;
		try {
			rep = new SimulatorRepository(new File("scripts"));
		} catch (IOException e1) {
			fail("Unexpected exception:" + e1);
		}
		try {
			rep.getSimulator("WebSocket3");
			fail("RuntimeException expected");
		} catch (RuntimeException e) {
		}
	}

	@Test
	public void test4() {
		try {
			SimulatorRepository rep = new SimulatorRepository(new File("scripts"));
			SimSimulator simulator = rep.getSimulator("WebSocket4");
			assertEquals(1, simulator.getScript().getSubScripts().get("control").getSubScripts().size());
			assertNotNull(simulator.getScript().getSubScripts().get("control").getSubScripts().get("control2"));
		} catch (IOException e) {
			fail("unexpected exception:" + e);
		}
	}
}
