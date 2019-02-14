package io.github.lujian213.simulator.webbit;

import static org.junit.Assert.*;

import org.junit.Test;

import io.github.lujian213.simulator.webbit.WebbitSimulator;

public class WebbitSimulatorTest {

	@Test
	public void test() {
		WebbitSimulator inst = new WebbitSimulator();
		assertEquals("abc/1", inst.reformatChannelName("abc.1"));;
	}

}
