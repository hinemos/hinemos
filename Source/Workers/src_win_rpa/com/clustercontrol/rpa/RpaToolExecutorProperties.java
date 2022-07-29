/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * RPAツールエグゼキューターの設定値を保持するクラス
 */
public class RpaToolExecutorProperties {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( RpaToolExecutorProperties.class );
	/** プロパティ */
	private static final Properties properties = new Properties();

	/**
	 * コンストラクタ
	 */
	private RpaToolExecutorProperties(){
	}

	/**
	 * Robo.propertiesファイル読み込みと初期化
	 * @param propFileName
	 */
	public static void init(String propFileName){
		m_log.debug("init() : propFileName = " + propFileName);
		try(FileInputStream inputStream = new FileInputStream(propFileName)){
			// プロパティファイルからキーと値のリストを読み込みます
			properties.load(inputStream);
		} catch (FileNotFoundException e) {
			m_log.error(e.getMessage(), e);
		} catch (IOException e) {
			m_log.error(e.getMessage(), e);
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
}
