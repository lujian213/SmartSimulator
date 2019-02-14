package io.github.lujian213.simulator;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import org.junit.Test;

import io.github.lujian213.simulator.SimScript;
import io.github.lujian213.simulator.SimulatorRepository;
import io.github.lujian213.simulator.manager.SimulatorFolder;

public class SimulatorFolderTest {
	@Test
	public void test() {
		try {
			SimulatorRepository rep = new SimulatorRepository(new File("scripts"));
			SimScript script = rep.getSimulatorScript("websocket");
			SimulatorFolder folder = new SimulatorFolder(script);
			assertEquals(2, folder.getFiles().size());
			String name = folder.getFiles().get(0).getName();
			assertTrue("init.properties".equals(name) || "step1.sim".equals(name));
			assertEquals(2, folder.getSubFolders().size());
			name = folder.getSubFolders().get(0).getName();
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
			SimScript script = rep.getSimulatorScript("websocket4");
			SimulatorFolder folder = new SimulatorFolder(script, new ZipFile(script.getMyself()));
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
}
