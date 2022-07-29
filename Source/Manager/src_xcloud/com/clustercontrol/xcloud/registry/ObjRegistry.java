/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.clustercontrol.xcloud.InternalManagerError;
import com.clustercontrol.xcloud.persistence.PersistenceUtil;
import com.clustercontrol.xcloud.persistence.Transactional;


public class ObjRegistry {
	public static class Entry<T> {
		private Object key;
		private T implementor;
		public Entry(Object key, T implementor) {
			this.key = key;
			this.implementor = implementor;
		}
		public Object getKey() {
			return key;
		}
		public T getImplementor() {
			return implementor;
		}
	}

	public interface IProvider<T> {
		T provide(Object key);
	}

	public interface IRegistry {
		<T> void put(Class<T> interfaceClass, Object key, Class<? extends T> implementor);
		<T> void put(Class<T> interfaceClass, Class<? extends T> implementor);
		<T> void put(Class<T> interfaceClass, IProvider<? extends T> provider);
		<T> void put(Class<T> interfaceClass, Object key, IProvider<? extends T> provider);
		<T> void put(Class<T> interfaceClass, Object key, T implementor);
		<T> void put(Class<T> interfaceClass, T implementor);
		
		<T> void remove(Class<T> interfaceClass, Object key);
		<T> void remove(Class<T> interfaceClass);

		<T> T get(Class<T> interfaceClass, Object key);
		<T> T get(Class<T> interfaceClass);
		<T> List<Entry<T>> getAll(Class<T> interfaceClass);
	}
	
	private static class RegistryImpl implements IRegistry {
		public static final Object nullKey = new Object();
		private Map<Class<?>, Map<Object, Object>> objectMap = Collections.synchronizedMap(new HashMap<Class<?>, Map<Object, Object>>());
		
		@Override
		public <T> void put(Class<T> interfaceClass, Object key, Class<? extends T> implementor) {
			internalPut(interfaceClass, key, implementor);
		}

		@Override
		public <T> void put(Class<T> interfaceClass, Class<? extends T> implementor) {
			internalPut(interfaceClass, nullKey, implementor);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T get(Class<T> interfaceClass, Object key) {
			Map<Object, Object> map = objectMap.get(interfaceClass);
			if (map == null) {
				return null;
			}
			Object obj = map.get(key);
			if (obj == null) {
				return null;
			}

			Object implementer = object(interfaceClass, key, obj);
			
			Transactional t = implementer.getClass().getAnnotation(Transactional.class);
			if (t != null) {
				return PersistenceUtil.decorateTransactional(interfaceClass, (T)implementer);
			}
			else {
				return interfaceClass.cast(implementer);
			}
		}

		@Override
		public <T> T get(Class<T> interfaceClass) {
			return get(interfaceClass, nullKey);
		}
		
		private <T> T object(Class<T> interfaceClass, Object key, Object obj) {
			if (obj instanceof IProvider) {
				return interfaceClass.cast(((IProvider<?>)obj).provide(key));
			}
			else if (obj instanceof Class) {
				try {
					return interfaceClass.cast(((Class<?>)obj).newInstance());
				}
				catch (InstantiationException | IllegalAccessException e) {
					throw new InternalManagerError(e);
				}
			}
			else {
				return interfaceClass.cast(obj);
			}
		}

		@Override
		public <T> void put(Class<T> interfaceClass, IProvider<? extends T> provider) {
			internalPut(interfaceClass, nullKey, provider);
		}

		@Override
		public <T> void put(Class<T> interfaceClass, Object key, IProvider<? extends T> provider) {
			internalPut(interfaceClass, key, provider);
		}

		@Override
		public <T> List<Entry<T>> getAll(Class<T> interfaceClass) {
			Map<Object, Object> map = objectMap.get(interfaceClass);
			if (map == null) {
				return null;
			}
			List<Entry<T>> list = new ArrayList<>();
			synchronized (map) {
				for (Map.Entry<Object, Object> entry: map.entrySet()) {
					list.add(new Entry<T>(entry.getKey(), object(interfaceClass, entry.getKey(), entry.getValue())));
				}
			}
			return list;
		}

		@Override
		public <T> void put(Class<T> interfaceClass, Object key, T implementor) {
			internalPut(interfaceClass, key, implementor);
		}

		@Override
		public <T> void put(Class<T> interfaceClass, T implementor) {
			internalPut(interfaceClass, nullKey, implementor);
		}

		private void internalPut(Class<?> interfaceClass, Object key, Object implementor) {
			Map<Object, Object> map = objectMap.get(interfaceClass);
			if (map == null) {
				map = new HashMap<>();
				objectMap.put(interfaceClass, Collections.synchronizedMap(map));
			}
			map.put(key, implementor);
		}

		@Override
		public <T> void remove(Class<T> interfaceClass, Object key) {
			Map<Object, Object> map = objectMap.get(interfaceClass);
			if (map == null)
				return;
			map.remove(key);
			
			if (map.isEmpty())
				objectMap.remove(interfaceClass);
		}

		@Override
		public <T> void remove(Class<T> interfaceClass) {
			remove(interfaceClass, nullKey);
		}
	}
	
	private static volatile IRegistry singleton;
	
	public static IRegistry reg() {
		if (singleton == null) {
			synchronized (ObjRegistry.class) {
				if (singleton == null) {
					singleton = new RegistryImpl();
				}
			}
		}
		return singleton;
	}
}
