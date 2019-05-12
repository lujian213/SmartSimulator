package io.github.lujian213.simulator.monitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.lujian213.simulator.SimRequest;
import io.github.lujian213.simulator.SimResponse;
import io.github.lujian213.simulator.SimulatorListener;

public class SimulatorListenerProxy implements SimulatorListener {
	
	private static SimulatorListenerProxy instance = new SimulatorListenerProxy();
	
	private Set<SimulatorListener> listeners = new HashSet<>();
	
	SimulatorListenerProxy() {
		
	}
	
	public static SimulatorListenerProxy getInstance() {
		return instance;
	}
	
	@Override
	public void onStart(String simulatorName) {
		List<SimulatorListener> listenerList = null;
		synchronized (listeners) {
			listenerList = new ArrayList<>(listeners);
		}
		for (SimulatorListener listener: listenerList) {
			listener.onStart(simulatorName);
		}
	}

	@Override
	public void onStop(String simulatorName) {
		List<SimulatorListener> listenerList = null;
		synchronized (listeners) {
			listenerList = new ArrayList<>(listeners);
		}
		for (SimulatorListener listener: listenerList) {
			listener.onStop(simulatorName);
		}
	}

	@Override
	public void onHandleMessage(String simulatorName, SimRequest request, List<SimResponse> responseList,
			boolean status) {
		List<SimulatorListener> listenerList = null;
		synchronized (listeners) {
			listenerList = new ArrayList<>(listeners);
		}
		for (SimulatorListener listener: listenerList) {
			listener.onHandleMessage(simulatorName, request, responseList, status);
		}
	}
	
	public void addListener(SimulatorListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	public void removeListener(SimulatorListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
}
