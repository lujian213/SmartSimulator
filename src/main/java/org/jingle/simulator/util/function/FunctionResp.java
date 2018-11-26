package org.jingle.simulator.util.function;

import java.lang.reflect.InvocationTargetException;

public class FunctionResp {
	private String exceptionClass;
	private String exceptionMsg;
	private Object retVal;
	
	public FunctionResp() {
	}
	
	public FunctionResp(Object obj) {
		if (obj instanceof Exception) {
			exceptionClass = ((Exception) obj).getClass().getName();
			exceptionMsg = ((Exception) obj).getMessage();
		} else {
			retVal = obj;
		}
	}


	public Exception toException() {
		try {
			return (Exception) Class.forName(exceptionClass).getConstructor(new Class[] {String.class}).newInstance(exceptionMsg);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			throw new RuntimeException(exceptionMsg);
		}
	}
	
	public String getExceptionClass() {
		return exceptionClass;
	}

	public void setExceptionClass(String exceptionClass) {
		this.exceptionClass = exceptionClass;
	}

	public String getExceptionMsg() {
		return exceptionMsg;
	}

	public void setExceptionMsg(String exceptionMsg) {
		this.exceptionMsg = exceptionMsg;
	}

	public Object getRetVal() {
		return retVal;
	}

	public void setRetVal(Object retVal) {
		this.retVal = retVal;
	}
	
	public boolean hasException() {
		return exceptionClass != null;
	}
}
