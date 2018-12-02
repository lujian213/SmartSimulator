package org.jingle.simulator.util.function;

import org.jingle.simulator.util.FunctionBean.FunctionContext;

public interface FunctionBeanListener {
	default public void onCreate(String simulatorName, FunctionContext context) {
		
	}
	default public void onClose(String simulatorName) {
		
	}
}
