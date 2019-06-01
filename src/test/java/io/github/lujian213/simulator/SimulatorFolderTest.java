package io.github.lujian213.simulator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import io.github.lujian213.simulator.manager.SimulatorFolder;
import io.github.lujian213.simulator.util.SimLogger;

public class SimulatorFolderTest {
	@Test
	public void test() {
		try {
			SimulatorRepository rep = new SimulatorRepository(new File("scripts"));
			SimScript script = rep.getSimulatorScript("WebSocket");
			SimulatorFolder folder = new SimulatorFolder(script);
			assertEquals(2, folder.getFiles().size());
			String name = folder.getFiles().get(0).getName();
			assertTrue("init.properties".equals(name) || "step1.sim".equals(name));
			assertEquals(3, folder.getSubFolders().size());
			name = folder.getSubFolders().get(0).getName();
            SimLogger.getLogger().info("name = " + name);
			assertTrue("hellowebsocket.1".equals(name) || "control".equals(name));
			assertEquals(3, folder.getSubFolders().get(0).getFiles().size());
		} catch (IOException e) {
			fail("unexpected exception:" + e);
		}
	}

	@Test
	public void test2() {
		try {
			SimulatorRepository rep = new SimulatorRepository(new File("scripts"));
			SimScript script = rep.getSimulatorScript("WebSocket4");
			SimulatorFolder folder = new SimulatorFolder(script);
			assertEquals(2, folder.getFiles().size());
			String name = folder.getFiles().get(0).getName();
			assertTrue("init.properties".equals(name) || "step1.sim".equals(name));
			assertEquals(2, folder.getSubFolders().size());
			name = folder.getSubFolders().get(0).getName();
			assertTrue("hellowebsocket.1".equals(name) || "control".equals(name));
		} catch (IOException e) {
			fail("unexpected exception:" + e);
		}
	}

	@Test
	public void test3() {
		try {
			SimulatorRepository rep = new SimulatorRepository(new File("scripts"));
			SimScript script = rep.getSimulatorScript(null);
			SimulatorFolder folder = new SimulatorFolder(script);
			assertEquals(2, folder.getFiles().size());
			String name = folder.getFiles().get(0).getName();
			assertTrue("init.properties".equals(name) || "base.sim".equals(name));
			assertEquals(0, folder.getSubFolders().size());
		} catch (IOException e) {
			fail("unexpected exception:" + e);
		}
	}

	@Test
	public void test4() {
		try {
			SimulatorFolder folder = new SimulatorFolder(new File("scripts/websocket"));
			assertEquals(2, folder.getFiles().size());
			String name = folder.getFiles().get(0).getName();
			assertTrue("init.properties".equals(name) || "step1.sim".equals(name));
			assertEquals(3, folder.getSubFolders().size());
			name = folder.getSubFolders().get(0).getName();
            SimLogger.getLogger().info("name = " + name);
			assertTrue("hellowebsocket.1".equals(name) || "control".equals(name));
			assertEquals(3, folder.getSubFolders().get(0).getFiles().size());
		} catch (IOException e) {
			fail("Unexpected exception:" + e);
		}
	}

	@Test
	public void test5() {
		try {
			SimulatorFolder folder = new SimulatorFolder(new File("scripts/websocket4.zip"));
			assertEquals(2, folder.getFiles().size());
			String name = folder.getFiles().get(0).getName();
			assertTrue("init.properties".equals(name) || "step1.sim".equals(name));
			assertEquals(2, folder.getSubFolders().size());
			name = folder.getSubFolders().get(0).getName();
			assertTrue("hellowebsocket.1".equals(name) || "control".equals(name));
		} catch (IOException e) {
			fail("Unexpected exception:" + e);
		}
	}
}
