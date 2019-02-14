package io.github.lujian213.simulator.util.function;

import io.github.lujian213.simulator.util.FunctionBean.FunctionContext;

public interface FunctionBeanListener {
	default public void onCreate(String simulatorName, FunctionContext context) {
		
	}
	default public void onClose(String simulatorName) {
		
	}
}
