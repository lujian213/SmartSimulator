package org.jingle.simulator;

import java.util.List;

public interface SimulatorListener {
	default public void onStart(String simulatorName) {
	}
	default public void onStop(String simulatorName) {
	}
	default public void onHandleMessage(String simulatorName, SimRequest request, List<SimResponse> responseList, boolean status) {
	}
}
