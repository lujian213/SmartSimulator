package org.jingle.simulator.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.VelocityContext;

public class BeanRepository {
	private static BeanRepository repository = new BeanRepository();
	private Map<String, Object> beanMap = new HashMap<>();
	
	public static BeanRepository getInstance() {
		return repository;
	}
	
	public Object addBean(Object obj) {
		if (obj == null) {
			throw new RuntimeException("null can not add to BeanRepository");
		}
		return doAddBean(obj.getClass().getName(), obj);
	}

	public Object addBean(Class<?> clazz) {
		Object obj;
		synchronized (beanMap) {
			obj = beanMap.get(clazz.getName());
			if (obj == null) {
				try {
					obj = clazz.newInstance();
					beanMap.put(clazz.getName(), obj);
				} catch (InstantiationException | IllegalAccessException e) {
					throw new RuntimeException("error to create instance of class [" + clazz + "]", e);
				}
			}
		}
		return obj;
	}

	public Object addBean(String className) {
		try {
			return addBean(Class.forName(className));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("error to create instance of class [" + className + "]", e);
		}
	}

	public Object addBean(String className, Object obj) {
		if (obj == null) {
			throw new RuntimeException("null can not add to BeanRepository");
		}
		Class<?> clazz;
		try {
			clazz = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		if (obj.getClass().isAssignableFrom(clazz)) {
			return doAddBean(clazz.getName(), obj);
		} else {
			throw new RuntimeException("class name [" + className + "] is not compatible with object [" + obj + "]");
		}
	}
	
	protected Object doAddBean(String name, Object obj) {
		if (obj == null) {
			throw new RuntimeException("null can not add to BeanRepository");
		}
		synchronized (beanMap) {
			Object inst = beanMap.get(name);
			if (inst == null) {
				beanMap.put(name, obj);
				inst = obj;
			}
			return inst;
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
	
	protected Object invoke(Object obj, Method m, VelocityContext vc) {
		Parameter[] parameters = m.getParameters();
		Object[] paramValues = new Object[parameters.length];
		for (int i = 1; i <= parameters.length; i++) {
			String paramName = getParameterName(parameters[i - 1]);
			paramValues[i - 1] = vc.get(paramName);
		}
		try {
			return m.invoke(obj, paramValues);
		} catch (IllegalAccessException|IllegalArgumentException e) {
			throw new RuntimeException("invoke error", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getCause().getMessage(), e.getCause());
		}
	}
	
	public Object invoke(String className, String methodName, VelocityContext vc) {
		Object obj = addBean(className);
		Method targetMethod = findMethod(obj, methodName);
		if (targetMethod != null) {
			return invoke(obj, targetMethod, vc);
		} else {
			throw new RuntimeException("no such method [" + methodName + "] for bean [" + className + "]");
		}
	}
}
