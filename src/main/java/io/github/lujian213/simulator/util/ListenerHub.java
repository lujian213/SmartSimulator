package io.github.lujian213.simulator.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

public interface ListenerHub<T> {
	public void addListener(T listener);
	public void removeListener(T listener);
	public void addFixedListener(T listener);
	public void removeAllListeners();

	public static <R> ListenerHub<R> createListenerHub(Class<R> clazz) {
		return (ListenerHub<R>) Proxy.newProxyInstance(ListenerHub.class.getClassLoader(), new Class<?>[] {clazz, ListenerHub.class}, new ListenerHubInvocationHandler<R>(clazz));
	}
}

class ListenerHubInvocationHandler<T> implements InvocationHandler {
	private ListenerHubImp<T> inst = new ListenerHubImp<>();
	private Class<T> clazz;
	
	public ListenerHubInvocationHandler(Class<T> clazz) {
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
	private Set<T> fixedListenerSet = new HashSet<>();
	private Set<T> floatListenerSet = new HashSet<>();
	
	public ListenerHubImp() {
	}
	
	public void addFixedListener(T listener) {
		synchronized(fixedListenerSet) {
			fixedListenerSet.add(listener);
		}
	}
	
	public void addListener(T listener) {
		synchronized(floatListenerSet) {
			floatListenerSet.add(listener);
		}
	}

	public void removeListener(T listener) {
		synchronized(floatListenerSet) {
			floatListenerSet.remove(listener);
		}
	}
	
	public void removeAllListeners() {
		synchronized(floatListenerSet) {
			floatListenerSet.clear();
		}
	}

	public void fireEvent(Method m, Object[] args) {
		synchronized(fixedListenerSet) {
			for (T listener: fixedListenerSet) {
				try {
					m.invoke(listener, args);
				} catch (Exception e) {
					SimLogger.getLogger().error("error when handle event", e);
				}
			}
		}
		synchronized(floatListenerSet) {
			for (T listener: floatListenerSet) {
				try {
					m.invoke(listener, args);
				} catch (Exception e) {
					SimLogger.getLogger().error("error when handle event", e);
				}
			}
		}
	}
}
