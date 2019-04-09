package io.github.lujian213.simulator.util;

import static org.junit.Assert.*;

import org.junit.Test;

import io.github.lujian213.simulator.SimulatorListener;
import io.github.lujian213.simulator.util.ListenerHub;

public class ListenerHubTest {

	@Test
	public void test1() {
		ListenerHub<SimulatorListener> inst = ListenerHub.createListenerHub(SimulatorListener.class);
		assertTrue(inst instanceof SimulatorListener);
		assertNotNull(inst.getClass());
		assertFalse(inst.hashCode() == 0);
		
		final int[] i = new int[] {1};
		SimulatorListener l1 = new SimulatorListener() {
			@Override
			public void onStart(String simulatorName) {
				i[0] = i[0] + 1;
			}
			
			
		};
		SimulatorListener l2 = new SimulatorListener() {
			@Override
			public void onStart(String simulatorName) {
				i[0] = i[0] + 10;
			}
		};
		
		inst.addListener(l1);
		inst.addListener(l2);

		SimulatorListener.class.cast(inst).onStart("aa");
		assertEquals(12, i[0]);
		
	}

}
