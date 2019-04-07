package io.github.lujian213.simulator.util;

import static io.github.lujian213.simulator.SimSimulatorConstants.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.VelocityContext;

import io.github.lujian213.simulator.util.function.FunctionBeanListener;
import io.github.lujian213.simulator.util.function.SimConstructor;
import io.github.lujian213.simulator.util.function.SimParam;


public class BeanRepository {
	private static BeanRepository repository = new BeanRepository();
	
	Map<String, Map<String,FunctionBean>> beanMap = new HashMap<>();
	
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
	
	public FunctionBean addBean(Class<?> clazz, VelocityContext vc) {
		String simulatorName = (String) vc.get(PROP_NAME_SIMULATOR_NAME);
		FunctionBean bean = null;
		synchronized (beanMap) {
			Map<String, FunctionBean> simMap = beanMap.get(simulatorName);
			if (simMap == null) {
				simMap = new HashMap<>();
				beanMap.put(simulatorName, simMap);
			}
			bean = simMap.get(clazz.getName());
			if (bean == null) {
				Object obj = createInstance(clazz, vc);
				if (obj instanceof SimContextAwareness) {
					SimContextAwareness.class.cast(obj).init(simulatorName, SimUtils.context2Properties(vc));
				}
				bean = new FunctionBean(obj);
				if (obj instanceof FunctionBeanListener) {
					((FunctionBeanListener)obj).onCreate(simulatorName, bean.getContext());
				}
				simMap.put(clazz.getName(), bean);
			}
		}
		return bean;
	}

	public FunctionBean addBean(String className, VelocityContext vc) {
		try {
			return addBean(Class.forName(className, true, Thread.currentThread().getContextClassLoader()), vc);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("error to create instance of class [" + className + "]", e);
		}
	}
	
	public void removeSimulatorBeans(String simulatorName) {
		Map<String, FunctionBean> simMap = null;
		synchronized (beanMap) {
			simMap = beanMap.remove(simulatorName);
		}
		if (simMap != null) {
			for (FunctionBean bean: simMap.values()) {
				if (bean.getBean() instanceof FunctionBeanListener) {
					((FunctionBeanListener)bean.getBean()).onClose(simulatorName);
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
			paramValues[i - 1] = smartValuePickup(parameters[i - 1].getType(), paramName, vc.get(paramName));
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
	
	protected Object smartValuePickup(Class<?> paramType, String paramName, Object value) {
		if (!(value instanceof String) && value != null) {
			return value;
		}
		String valueStr = (String) value;
		try {
			if (paramType == Character.class) {
				if (valueStr != null && valueStr.length() == 1) {
					return valueStr.charAt(0);
				}
				if (valueStr == null) {
					return null;
				}
			}
			if (paramType == Integer.class) {
				if (valueStr != null) {
					return Integer.parseInt(valueStr);
				}
				return null;
			}
			if (paramType == Long.class) {
				if (valueStr != null) {
					return Long.parseLong(valueStr);
				}
				return null;
			}
			if (paramType == Byte.class) {
				if (valueStr != null) {
					return Byte.parseByte(valueStr);
				}
				return null;
			}
			if (paramType == Short.class) {
				if (valueStr != null) {
					return Short.parseShort(valueStr);
				}
				return null;
			}
			if (paramType == Boolean.class) {
				if (valueStr != null) {
					return Boolean.parseBoolean(valueStr);
				}
				return null;
			}
			if (paramType == Double.class) {
				if (valueStr != null) {
					return Double.parseDouble(valueStr);
				}
				return null;
			}
			if (paramType == Float.class) {
				if (valueStr != null) {
					return Float.parseFloat(valueStr);
				}
				return null;
			}
			if (paramType == String.class) {
				return valueStr;
			}
			if (paramType == char.class) {
				if (valueStr != null && valueStr.length() == 1) {
					return valueStr.charAt(0);
				}
			}
			if (paramType == int.class) {
				if (valueStr != null) {
					return Integer.parseInt(valueStr);
				}
			}
			if (paramType == long.class) {
				if (valueStr != null) {
					return Long.parseLong(valueStr);
				}
			}
			if (paramType == byte.class) {
				if (valueStr != null) {
					return Byte.parseByte(valueStr);
				}
			}
			if (paramType == short.class) {
				if (valueStr != null) {
					return Short.parseShort(valueStr);
				}
			}
			if (paramType == boolean.class) {
				if (valueStr != null) {
					return Boolean.parseBoolean(valueStr);
				}
			}
			if (paramType == double.class) {
				if (valueStr != null) {
					return Double.parseDouble(valueStr);
				}
			}
			if (paramType == float.class) {
				if (valueStr != null) {
					return Float.parseFloat(valueStr);
				}
			}
			if (paramType == Object.class) {
				return valueStr;
			}
		} catch (NumberFormatException e) {
			throw new RuntimeException("Can not use [" + valueStr + "] as parameter [" + paramName + "]");
		}
		throw new RuntimeException("Can not use [" + valueStr + "] as parameter [" + paramName + "]");
	}
	
	public Object invoke(String className, String methodName, VelocityContext vc) {
		FunctionBean bean = addBean(className, vc);
		Method targetMethod = findMethod(bean.getBean(), methodName);
		if (targetMethod != null) {
			return invoke(bean.getBean(), targetMethod, vc);
		} else {
			throw new RuntimeException("no such method [" + methodName + "] for bean [" + className + "]");
		}
	}
}
