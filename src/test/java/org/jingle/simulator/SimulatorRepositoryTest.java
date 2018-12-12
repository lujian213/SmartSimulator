package org.jingle.simulator;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class SimulatorRepositoryTest {

	@Test
	public void test() {
		try {
			SimulatorRepository rep = new SimulatorRepository(new File("scripts"));
			rep.getSimulator("dummy");
			fail("RuntimeException expected");
		} catch (IOException e) {
			fail("unexpected exception:" + e);
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
			fail("RuntimeException expected");
		} catch (IOException e) {
			fail("unexpected exception:" + e);
		} catch (RuntimeException e) {
		}
	}

	@Test
	public void test3() {
		try {
			SimulatorRepository rep = new SimulatorRepository(new File("scripts"));
			rep.getSimulator("WebSocket3");
			fail("RuntimeException expected");
		} catch (IOException e) {
			fail("unexpected exception:" + e);
		} catch (RuntimeException e) {
		}
	}

	@Test
	public void test4() {
		try {
			SimulatorRepository rep = new SimulatorRepository(new File("scripts"));
			SimSimulator simulator = rep.getSimulator("WebSocket4");
			assertEquals(1, simulator.getScript().getSubScripts().get("control").getSubScripts().size());
			assertNotNull(simulator.getScript().getSubScripts().get("control").getSubScripts().get("control1"));
			fail("RuntimeException expected");
		} catch (IOException e) {
			fail("unexpected exception:" + e);
		} catch (RuntimeException e) {
		}
	}
}
