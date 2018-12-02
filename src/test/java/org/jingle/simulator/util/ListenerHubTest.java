package org.jingle.simulator.util;

import static org.junit.jupiter.api.Assertions.*;

import org.jingle.simulator.SimulatorListener;
import org.junit.jupiter.api.Test;

class ListenerHubTest {

	@Test
	void test1() {
		ListenerHub<SimulatorListener> inst = ListenerHub.createListenerHub(SimulatorListener.class);
		assertTrue(inst instanceof SimulatorListener);
		System.out.println(inst.getClass());
		assertNotNull(inst.getClass());
		System.out.println(inst.hashCode());
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
