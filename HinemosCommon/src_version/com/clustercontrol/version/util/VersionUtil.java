/*
 * Copyright (c) 2023 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.version.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Hinemosのバージョンに関するUtilクラス
 *
 */
public class VersionUtil {
	
	private final static String VERSION;
	private final static String VERSION_SHORT;
	private final static String VERSION_MAJOR;
	private final static String VERSION_TOOL;

	private static Log m_log = LogFactory.getLog(VersionUtil.class);
	
	static InputStream input = VersionUtil.class.getResourceAsStream("/hinemos_version.properties");
	static Properties properties = new Properties();
	
	static {
		try {
			properties.load(input);
		} catch (IOException e) {
			m_log.error("hinemos_version.properties is not exist.");
		}
		
		try {
			input.close();
		} catch (IOException e) {
			m_log.error("hinemos_version.properties InputStream close error.");
		}
		
		VERSION = properties.getProperty("VERSION");
		VERSION_SHORT = properties.getProperty("VERSION.SHORT");
		VERSION_MAJOR = properties.getProperty("VERSION.MAJOR");
		VERSION_TOOL = properties.getProperty("VERSION.TOOL");
	}
	
	/**
	 * このHinemosのバージョンを返すメソッド
	 * @return string このHinemosのバージョン表記
	 */
	public static String getVersion(){
		return VERSION;
	}
	
	/**
	 * このHinemosのバージョン(short)を返すメソッド
	 * @return string このHinemosのバージョン(short)表記
	 */
	public static String getVersionShort(){
		return VERSION_SHORT;
	}
	
	/**
	 * このHinemosのバージョン(major)を返すメソッド
	 * @return string このHinemosのバージョン(major)表記
	 */
	public static String getVersionMajor(){
		return VERSION_MAJOR;
	}
	
	/**
	 * このHinemosのバージョン(tool)を返すメソッド
	 * @return string 使用されているツールのバージョン表記
	 */
	public static String getVersionTool(){
		return VERSION_TOOL;
	}
	
	/**
	 * 指定された設定インポートエクスポート対応機能のスキーマ表記を返すメソッド
	 * @return string 指定された設定インポートエクスポート対応機能のスキーマ表記
	 */
	public static String getSchemaProperty(String key){
		return properties.getProperty(key);
	}
}
