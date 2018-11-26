package org.jingle.simulator.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.jingle.simulator.SimScript;
import org.jingle.simulator.util.function.SimConstructor;
import org.jingle.simulator.util.function.SimParam;
import org.jingle.simulator.util.function.SimulatorListener;

public class BeanRepository {
	private static BeanRepository repository = new BeanRepository();
	
	Map<String, Map<String,Object>> beanMap = new HashMap<>();
	
	public static BeanRepository getInstance() {
		return repository;
	}
	
	protected Object createInstance(Class<?> clazz, VelocityContext vc) {
		Constructor<?>[] cons = clazz.getConstructors();
		Constructor<?> constructor = null;
		for (Constructor<?> con: cons) {
			if (con.getAnnotation(SimConstructor.class) != null) {
				constructor = con;
				break;
			} else if (con.getParameterCount() == 0 ) {
				constructor = con;
			}
		}
		if (constructor == null) {
			throw new RuntimeException("no proper constructor for [" + clazz + "]");
		} else {
			Object[] paramValues = prepareMethodParameters(constructor.getParameters(), vc);
			try {
				return constructor.newInstance(paramValues);
			} catch (Exception e) {
				throw new RuntimeException("Error when create instance of [" + clazz + "]", e);
			}
		}
	}
	
	public Object addBean(Class<?> clazz, VelocityContext vc) {
		String simulatorName = (String) vc.get(SimScript.PROP_NAME_SIMULATOR_NAME);
		Object obj = null;
		synchronized (beanMap) {
			Map<String, Object> simMap = beanMap.get(simulatorName);
			if (simMap == null) {
				simMap = new HashMap<>();
				beanMap.put(simulatorName, simMap);
			}
			obj = simMap.get(clazz.getName());
			if (obj == null) {
				obj = createInstance(clazz, vc);
				simMap.put(clazz.getName(), obj);
			}
		}
		return obj;
	}

	public Object addBean(String className, VelocityContext vc) {
		try {
			return addBean(Class.forName(className), vc);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("error to create instance of class [" + className + "]", e);
		}
	}
	
	public void removeSimulatorBeans(String simulatorName) {
		Map<String, Object> simMap = null;
		synchronized (beanMap) {
			simMap = beanMap.remove(simulatorName);
		}
		if (simMap != null) {
			for (Object bean: simMap.values()) {
				if (bean instanceof SimulatorListener) {
					((SimulatorListener)bean).onClose(simulatorName);
				}
			}
		}
	}

	protected Method findMethod(Object obj, String methodName) {
		try {
			for (Method m: obj.getClass().getMethods()) {
				if (m.getName().equals(methodName)) {
					return m;
				}
			}
			return null;
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected String getParameterName(Parameter parameter) {
		String paramName = parameter.getName();
		SimParam sp = parameter.getAnnotation(SimParam.class);
		if (sp != null) {
			paramName = sp.value();
		}
		return paramName;
	}
	
	protected Object[] prepareMethodParameters(Parameter[] parameters, VelocityContext vc) {
		Object[] paramValues = new Object[parameters.length];
		for (int i = 1; i <= parameters.length; i++) {
			String paramName = getParameterName(parameters[i - 1]);
			paramValues[i - 1] = smartValuePickup(parameters[i - 1], vc.get(paramName));
		}
		return paramValues;
	}
	
	protected Object invoke(Object obj, Method m, VelocityContext vc) {
		Object[] paramValues = prepareMethodParameters(m.getParameters(), vc);
		try {
			return m.invoke(obj, paramValues);
		} catch (IllegalAccessException|IllegalArgumentException e) {
			throw new RuntimeException("invoke error", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getCause().getMessage(), e.getCause());
		}
	}
	
	protected Object smartValuePickup(Parameter param, Object value) {
		if (!(value instanceof String) && value != null) {
			return value;
		}
		String valueStr = (String) value;
		Class<?> type = param.getType();
		try {
		if (type == Character.class) {
			if (valueStr != null && valueStr.length() == 1) {
				return valueStr.charAt(0);
			}
			if (valueStr == null) {
				return null;
			}
		}
		if (type == Integer.class) {
			if (valueStr != null) {
				return Integer.parseInt(valueStr);
			}
			return null;
		}
		if (type == Long.class) {
			if (valueStr != null) {
				return Long.parseLong(valueStr);
			}
			return null;
		}
		if (type == Byte.class) {
			if (valueStr != null) {
				return Byte.parseByte(valueStr);
			}
			return null;
		}
		if (type == Short.class) {
			if (valueStr != null) {
				return Short.parseShort(valueStr);
			}
			return null;
		}
		if (type == Boolean.class) {
			if (valueStr != null) {
				return Boolean.parseBoolean(valueStr);
			}
			return null;
		}
		if (type == Double.class) {
			if (valueStr != null) {
				return Double.parseDouble(valueStr);
			}
			return null;
		}
		if (type == Float.class) {
			if (valueStr != null) {
				return Float.parseFloat(valueStr);
			}
			return null;
		}
		if (type == String.class) {
			return valueStr;
		}
		if (type == char.class) {
			if (valueStr != null && valueStr.length() == 1) {
				return valueStr.charAt(0);
			}
		}
		if (type == int.class) {
			if (valueStr != null) {
				return Integer.parseInt(valueStr);
			}
		}
		if (type == long.class) {
			if (valueStr != null) {
				return Long.parseLong(valueStr);
			}
		}
		if (type == byte.class) {
			if (valueStr != null) {
				return Byte.parseByte(valueStr);
			}
		}
		if (type == short.class) {
			if (valueStr != null) {
				return Short.parseShort(valueStr);
			}
		}
		if (type == boolean.class) {
			if (valueStr != null) {
				return Boolean.parseBoolean(valueStr);
			}
		}
		if (type == double.class) {
			if (valueStr != null) {
				return Double.parseDouble(valueStr);
			}
		}
		if (type == float.class) {
			if (valueStr != null) {
				return Float.parseFloat(valueStr);
			}
		}
		} catch (NumberFormatException e) {
			throw new RuntimeException("Can not use [" + valueStr + "] as parameter [" + param.getName() + "]");
		}
		throw new RuntimeException("Can not use [" + valueStr + "] as parameter [" + param.getName() + "]");
	}
	
	public Object invoke(String className, String methodName, VelocityContext vc) {
		Object obj = addBean(className, vc);
		Method targetMethod = findMethod(obj, methodName);
		if (targetMethod != null) {
			return invoke(obj, targetMethod, vc);
		} else {
			throw new RuntimeException("no such method [" + methodName + "] for bean [" + className + "]");
		}
	}
}
