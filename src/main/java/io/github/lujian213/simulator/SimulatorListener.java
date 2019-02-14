package io.github.lujian213.simulator;

import java.util.List;
import java.util.Properties;

import io.github.lujian213.simulator.util.SimContextAwareness;

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
