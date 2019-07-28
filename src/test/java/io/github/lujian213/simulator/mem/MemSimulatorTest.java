package io.github.lujian213.simulator.mem;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import io.github.lujian213.simulator.SimScript;

public class MemSimulatorTest {
	@Test
	public void test1() throws Exception {
		SimScript script = new SimScript(new File("scripts"));
		MemSimulator simulator = new MemSimulator(new SimScript(script, new File("scripts/mem")));
		assertEquals("Good Morning! Adele", simulator.handleRequest("Hello Adele"));
		assertEquals("Status Code is COMP", simulator.handleRequest(FileUtils.readFileToString(new File("scripts/mem/sample.xml"), "utf-8")));
	}

}
