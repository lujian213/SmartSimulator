package org.jingle.simulator.webbit;

import static org.junit.Assert.*;

import org.jingle.simulator.webbit.WebbitSimulator;
import org.junit.Test;

public class WebbitSimulatorTest {

	@Test
	public void test() {
		WebbitSimulator inst = new WebbitSimulator();
		assertEquals("abc/1", inst.reformatChannelName("abc.1"));;
	}

}
