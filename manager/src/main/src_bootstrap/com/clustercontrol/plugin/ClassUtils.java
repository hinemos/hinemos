/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
