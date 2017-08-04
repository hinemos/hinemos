/*

Copyright (C) 2011 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.winsyslog;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * syslog.confファイルに設定されているプロパティ値を
 * 取得するためのユーティリティクラス
 */
public class WinSyslogConfig {
	private static Log log = LogFactory.getLog(WinSyslogConfig.class);

	private static final Properties properties = new Properties();

	private WinSyslogConfig(){
	}

	public static void init(String confFile){
		log.debug("init() : propFileName = " + confFile);
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(confFile);
			properties.load(inputStream);
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			if(inputStream != null){
				try {
					inputStream.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}

	public static String getProperty(String key){
		log.debug(key + " = " + properties.getProperty(key));
		return properties.getProperty(key);
	}

	public static String getProperty(String key, String defaultValue){
		log.debug(key + " = " + properties.getProperty(key) + ", defaultValue = " + defaultValue);
		return properties.getProperty(key, defaultValue);
	}
	
	public static boolean getBooleanProperty(String key){
		log.debug(key + " = " + properties.getProperty(key));
		return Boolean.parseBoolean(properties.getProperty(key));
	}
	
	public static boolean getBooleanProperty(String key, boolean defaultValue){
		log.debug(key + " = " + properties.getProperty(key));
		return Boolean.parseBoolean(properties.getProperty(key, String.valueOf(defaultValue)));
	}
	
	public static int getIntegerProperty(String key){
		log.debug(key + " = " + properties.getProperty(key));
		return Integer.parseInt(properties.getProperty(key));
	}
	
	public static int getIntegerProperty(String key, int defaultValue){
		log.debug(key + " = " + properties.getProperty(key));
		return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
	}

	public static Properties getProperties(){
		log.debug("getProperties() : call");
		return properties;
	}
}
