package io.github.lujian213.simulator.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

public interface ListenerHub<T> {
	public void addListener(T listener);
	public void removeListener(T listener);
	public static <R> ListenerHub<R> createListenerHub(Class<R> clazz) {
		return (ListenerHub<R>) Proxy.newProxyInstance(ListenerHub.class.getClassLoader(), new Class<?>[] {clazz, ListenerHub.class}, new ListenerHumInvocationHandler<R>(clazz));
	}
}

class ListenerHumInvocationHandler<T> implements InvocationHandler {
	private ListenerHubImp<T> inst = new ListenerHubImp<>();
	private Class<T> clazz;
	
	public ListenerHumInvocationHandler(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getDeclaringClass() == ListenerHub.class) {
			return method.invoke(inst, args);
		} else if (method.getDeclaringClass() == Object.class) {
			return method.invoke(inst, args);
		} else if (method.getDeclaringClass() == clazz || method.getDeclaringClass().isAssignableFrom(clazz)) {
			inst.fireEvent(method, args);
		} else {
			return method.invoke(inst, args);
		}
		return null;
	}
}

class ListenerHubImp<T> implements ListenerHub<T>{
	private Set<T> listenerSet = new HashSet<>();
	
	public ListenerHubImp() {
	}
	
	public void addListener(T listener) {
		listenerSet.add(listener);
	}
	
	public void removeListener(T listener) {
		listenerSet.remove(listener);
	}

	public void fireEvent(Method m, Object[] args) {
		for (T listener: listenerSet) {
			try {
				m.invoke(listener, args);
			} catch (Exception e) {
				SimLogger.getLogger().error("error when handle event", e);
			}
		}
	}
}
