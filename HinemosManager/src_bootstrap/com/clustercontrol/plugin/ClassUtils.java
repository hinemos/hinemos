/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.plugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * クラスをCLASSPATHに追加するクラスローダクラス<br/>
 */
public class ClassUtils {

	private static final Log log = LogFactory.getLog(ClassUtils.class);

	/**
	 * 指定したディレクトリ内のファイルをCLASSPATHに追加するメソッド<br/>
	 * @param directory 指定するディレクトリ
	 * @throws IOException
	 */
	public static void addDirToClasspath(File directory) throws IOException {
		if (directory.exists()) {
			File[] files = directory.listFiles();
			if (files != null) {
				for (File file : files) {
					addURL(file.toURI().toURL());
				}
			}
		}
	}

	/**
	 * 指定したURLをCLASSPATHに追加するメソッド<br/>
	 * @param 指定するURL
	 * @throws IOException
	 */
	public static void addURL(URL u) throws IOException {
		URLClassLoader sysLoader = (URLClassLoader)ClassLoader.getSystemClassLoader();

		for (URL url : sysLoader.getURLs()) {
			if (url.toString().equals(u.toString())) {
				log.info("URL " + u + " is already in CLASSPATH");
				return;
			}
		}

		Class<URLClassLoader> sysClass = URLClassLoader.class;
		try {
			Method method = sysClass.getDeclaredMethod("addURL", new Class[]{ URL.class });
			method.setAccessible(true);
			method.invoke(sysLoader, new Object[] { u });
		} catch (Throwable t) {
			log.warn(t.getMessage(), t);
			throw new IOException("could not add URL " + u + " to CLASSPATH");
		}
	}

}
