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


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((exceptionClass == null) ? 0 : exceptionClass.hashCode());
		result = prime * result + ((exceptionMsg == null) ? 0 : exceptionMsg.hashCode());
		result = prime * result + ((retVal == null) ? 0 : retVal.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FunctionResp other = (FunctionResp) obj;
		if (exceptionClass == null) {
			if (other.exceptionClass != null)
				return false;
		} else if (!exceptionClass.equals(other.exceptionClass))
			return false;
		if (exceptionMsg == null) {
			if (other.exceptionMsg != null)
				return false;
		} else if (!exceptionMsg.equals(other.exceptionMsg))
			return false;
		if (retVal == null) {
			if (other.retVal != null)
				return false;
		} else if (!retVal.equals(other.retVal))
			return false;
		return true;
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
