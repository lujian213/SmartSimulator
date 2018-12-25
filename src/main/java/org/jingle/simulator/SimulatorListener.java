package org.jingle.simulator;

import java.util.List;
import java.util.Properties;

import org.jingle.simulator.util.SimContextAwareness;

public interface SimulatorListener extends SimContextAwareness {
	default public void onStart(String simulatorName) {
	}
	default public void onStop(String simulatorName) {
	}
	default public void onHandleMessage(String simulatorName, SimRequest request, List<SimResponse> responseList, boolean status) {
	}
	default public void init(Properties props) {
	}
}
