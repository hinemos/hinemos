/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.reporting.bean.ReportingConstant;

/**
 * template.propertiesファイルに設定されているプロパティ値を
 * 取得するためのユーティリティクラス
 */
public class ReportingProperties {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( ReportingProperties.class );
	private Properties m_properties = new Properties();
	
	public ReportingProperties(String templateId){
		FileInputStream inputStream = null;
		m_properties = new Properties();
		try {

			String dirName = templateId;
			int index = dirName.lastIndexOf("_");
			
			// templateId末尾に言語コードが含まれている場合はディレクトリ名から取り除く
			if(index >= 0){
				if(Arrays.asList(Locale.getISOLanguages()).contains(dirName.substring(index + 1))){
					dirName = dirName.substring(0, index);
				}
			}
						
			String etcdir = System.getProperty("hinemos.manager.etc.dir", "/opt/hinemos/etc");
			String templateDir = etcdir + File.separator + "reporting" + File.separator + "template" + File.separator + dirName;
			
			String propertyFile = templateDir + File.separator + templateId + ".properties";
			// jrxmlファイルは多言語で共通のためdirNameと同じファイル名を取得
			String jrxmlFile = templateDir + File.separator + dirName + ".jrxml";
			
			// プロパティファイルの存在確認
			if (!new File(propertyFile).exists()) {
				m_log.error(propertyFile + "dose not exists.");
				System.exit(1);
			}
			// jrxmlファイルの存在確認
			if (!new File(jrxmlFile).exists()) {
				m_log.error(jrxmlFile + "dose not exists.");
				System.exit(1);
			}
			
			// プロパティファイルからキーと値のリストを読み込む
			inputStream = new FileInputStream(propertyFile);
			m_properties.load(inputStream);
			
			// jrxmlファイルのパスをプロパティとして格納する
			m_properties.put(ReportingConstant.JRXML_PATH_KEY_VALUE, jrxmlFile);
			
			// プロパティの内容確認
			if(m_log.isDebugEnabled()) {
				for (Object key : m_properties.keySet()) {
					m_log.debug("templateId = " + templateId + " property : key = " + key.toString() + ", value = " + m_properties.getProperty(key.toString()));
				}
			}
		} catch (FileNotFoundException e) {
			m_log.warn(e.getMessage(), e);
		} catch (IOException e) {
			m_log.warn(e.getMessage(), e);
		} finally {
			if(inputStream != null){
				try {
					inputStream.close();
				} catch (IOException e) {
					m_log.warn(e.getMessage(), e);
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
	public String getProperty(String key){
		m_log.debug(key + " = " + m_properties.getProperty(key));
		return m_properties.getProperty(key);
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
	public String getProperty(String key, String defaultValue){
		m_log.debug(key + " = " + m_properties.getProperty(key) + ", defaultValue = " + defaultValue);
		return m_properties.getProperty(key, defaultValue);
	}
	
	/**
	 * 全プロパティリストの情報をメモリ上で保持します。
	 * 
	 * @return key value のマップ（HashMap）
	 */
	public HashMap<String, String> getAllProperties() {
		
		HashMap<String, String> retMap = new HashMap<String, String>();
		
		for(Map.Entry<Object, Object> entry : m_properties.entrySet()) {
			retMap.put(entry.getKey().toString(), entry.getValue().toString());
		}
		
		return retMap;
	}
	
}