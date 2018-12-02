package org.jingle.simulator;

public interface SimulatorListener {
	default public void onStart(String simulatorName) {
	}
	default public void onStop(String simulatorName) {
	}
	default public void onHandleMessage(String simulatorName, SimRequest request, SimResponse response, boolean status) {
	}
}
