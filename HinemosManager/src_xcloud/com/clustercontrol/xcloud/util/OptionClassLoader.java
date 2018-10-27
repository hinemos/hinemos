/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.util;

import java.net.URL;
import java.net.URLClassLoader;

public class OptionClassLoader extends URLClassLoader {
	private ClassLoader parent;
	
	public OptionClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, null);
		this.parent = parent;
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		synchronized (getClassLoadingLock(name)) {
			Class<?> clazz = findLoadedClass(name);
			if (clazz == null) {
				try {
					clazz = findClass(name);
				} catch (ClassNotFoundException e) {
					if (parent != null) {
						clazz = parent.loadClass(name);
					} else {
						throw e;
					}
				}
			}
			if (resolve) {
				resolveClass(clazz);
			}
			return clazz;
		}
	}
}
