package io.github.lujian213.simulator.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FunctionBean {
	public static class FunctionContext {
		private ObjectMapper objectMapper = new ObjectMapper();

		public ObjectMapper getObjectMapper() {
			return objectMapper;
		}

		public void setObjectMapper(ObjectMapper objectMapper) {
			this.objectMapper = objectMapper;
		}
		
	}
	private Object bean;
	private FunctionContext context = new FunctionContext();
	
	public FunctionBean(Object bean) {
		this.bean = bean;
	}

	public Object getBean() {
		return bean;
	}

	public FunctionContext getContext() {
		return context;
	}
}
