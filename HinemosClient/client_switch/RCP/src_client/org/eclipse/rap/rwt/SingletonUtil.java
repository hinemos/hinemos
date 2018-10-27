/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package org.eclipse.rap.rwt;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceContext;

/**
 * Singleton Utility for RCP
 * 
 * @since 5.0.0
 */
public final class SingletonUtil{
	@SuppressWarnings("unchecked")
	public static <T> T getSessionInstance( Class<T> type ) {
		ServiceContext context = ContextProvider.getContext();

		T singleton = null;
		synchronized (context) {
			Map<Class<?>, Object> singletonMap = context.getSingletonMap();
			singleton = (T) singletonMap.get(type);
			if( null == singleton ) {
				Constructor<T> constructor;
				try {
					constructor = type.getDeclaredConstructor();
					// Also allow access to inner class instance
					if( !constructor.isAccessible() ) {
						constructor.setAccessible( true );
					}
					try {
						singleton = (T) constructor.newInstance();
						singletonMap.put(type, singleton );
					} catch (InstantiationException e) {
						Logger.getLogger(SingletonUtil.class).debug(e.getMessage(), e);
					} catch (IllegalAccessException e) {
						Logger.getLogger(SingletonUtil.class).debug(e.getMessage(), e);
					} catch (IllegalArgumentException e) {
						Logger.getLogger(SingletonUtil.class).debug(e.getMessage(), e);
					} catch (InvocationTargetException e) {
						Logger.getLogger(SingletonUtil.class).debug(e.getMessage(), e);
					}
				} catch (NoSuchMethodException e) {
					Logger.getLogger(SingletonUtil.class).debug(e.getMessage(), e);
				} catch (SecurityException e) {
					Logger.getLogger(SingletonUtil.class).debug(e.getMessage(), e);
				}
			}
		}
		return singleton;
	}
}

