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

package com.clustercontrol.agent.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Agent.propertiesファイルに設定されているプロパティ値を
 * 取得するためのユーティリティクラス
 */
public class AgentProperties {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( AgentProperties.class );

	private static final Properties properties = new Properties();

	private AgentProperties(){
	}

	/**
	 * Agent.propertiesファイル読み込みと初期化
	 * @param propFileName
	 */
	public static void init(String propFileName){
		m_log.debug("init() : propFileName = " + propFileName);
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(propFileName);

			// プロパティファイルからキーと値のリストを読み込みます
			properties.load(inputStream);
		} catch (FileNotFoundException e) {
			m_log.error(e.getMessage(), e);
		} catch (IOException e) {
			m_log.error(e.getMessage(), e);
		} finally {
			if(inputStream != null){
				try {
					inputStream.close();
				} catch (IOException e) {
					m_log.error(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * 指定されたキーを持つプロパティを、プロパティリストから探します。
	 * そのキーがプロパティリストにないと、デフォルトのプロパティリスト、
	 * さらにそのデフォルト値が繰り返し調べられます。
	 * そのプロパティが見つからない場合は、null が返されます。
	 * @param key プロパティキー
	 * @return 指定されたキー値を持つこのプロパティリストの値
	 */
	public static String getProperty(String key){
		m_log.debug(key + " = " + properties.getProperty(key));
		return properties.getProperty(key);
	}

	/**
	 * 指定されたキーを持つプロパティを、プロパティリストから探します。
	 * そのキーがプロパティリストにないと、デフォルトのプロパティリスト、
	 * さらにそのデフォルト値が繰り返し調べられます。
	 * そのプロパティが見つからない場合は、デフォルト値の引数が返されます。
	 * @param key プロパティキー
	 * @param defaultValue デフォルト値
	 * @return 指定されたキー値を持つこのプロパティリストの値
	 */
	public static String getProperty(String key, String defaultValue){
		m_log.debug(key + " = " + properties.getProperty(key) + ", defaultValue = " + defaultValue);
		return properties.getProperty(key, defaultValue);
	}

	/**
	 * Agent.propertiesのプロパティ情報をそのまま渡す
	 * @return
	 */
	public static Properties getProperties(){
		m_log.debug("getProperties() : call");
		return properties;
	}

	/**
	 * 指定されたキーと値をプロパティリストにセットする
	 * 
	 * @param key
	 * @param value
	 */
	public static void setProperty(String key, String value) {
		if (m_log.isDebugEnabled()) {
			m_log.debug("key:" + key + ", value:" + value);
		}
		properties.setProperty(key, value);
	}
}
