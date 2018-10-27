/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * クラスタ構成においてJava VM間で共有されないため、Java VM起動時などで必ず格納される実装とすること。
 * また、同様の理由によりImmutableなクラスのみを本クラスで管理すること。（メンバ変数などのステート変更はクラスタ間で共有されないため）
 */
public class ObjectSharingService {
	public interface IObjectRegistry {
		<T> void put(Class<T> interfaceClass, Object key, Class<? extends T> implementor);
		<T> void put(Class<T> interfaceClass, Class<? extends T> implementor);

		<T> T get(Class<T> clazz, Object key) throws InstantiationException, IllegalAccessException;
		<T> T get(Class<T> clazz) throws InstantiationException, IllegalAccessException;

		Map<Object, Class<?>> getMap(Class<?> interfaceClass);
	}

	private static class ObjectRegistry implements IObjectRegistry {
		public static final Object nullKey = new Object();
		private Map<Class<?>, Map<Object, Class<?>>> objectMap = new ConcurrentHashMap<>();

		@Override
		public synchronized <T> void put(Class<T> interfaceClass, Object key, Class<? extends T> implementor) {
			Map<Object, Class<?>> map = objectMap.get(interfaceClass);
			if (map == null) {
				map = new ConcurrentHashMap<>();
				objectMap.put(interfaceClass, map);
			}
			map.put(key, implementor);
		}

		@Override
		public synchronized <T> void put(Class<T> interfaceClass, Class<? extends T> implementor) {
			Map<Object, Class<?>> map = objectMap.get(interfaceClass);
			if (map == null) {
				map = new ConcurrentHashMap<>();
				objectMap.put(interfaceClass, map);
			}
			map.put(nullKey, implementor);
		}

		@Override
		public synchronized <T> T get(Class<T> interfaceClass, Object key) throws InstantiationException, IllegalAccessException {
			Map<Object, Class<?>> map = objectMap.get(interfaceClass);
			if (map == null) {
				return null;
			}
			Class<?> clazz = map.get(key);
			if (clazz == null) {
				return null;
			}
			T obj = interfaceClass.cast(clazz.newInstance());
			return obj;
		}

		@Override
		public synchronized <T> T get(Class<T> interfaceClass) throws InstantiationException, IllegalAccessException {
			Map<Object, Class<?>> map = objectMap.get(interfaceClass);
			if (map == null) {
				return null;
			}
			Class<?> clazz = map.get(nullKey);
			if (clazz == null) {
				return null;
			}
			T obj = interfaceClass.cast(clazz.newInstance());
			return obj;
		}

		@Override
		public synchronized Map<Object, Class<?>> getMap(Class<?> interfaceClass) {
			Map<Object, Class<?>> map = objectMap.get(interfaceClass);
			if (map == null) {
				return Collections.emptyMap();
			}
			return new ConcurrentHashMap<>(map);
		}
	}

	private static final IObjectRegistry singleton = new ObjectRegistry();;

	public static IObjectRegistry objectRegistry() {
		return singleton;
	}
}