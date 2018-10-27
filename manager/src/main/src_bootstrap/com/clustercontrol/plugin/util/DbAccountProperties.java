/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * DBアカウントプロパティ情報を取得するクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class DbAccountProperties {
	private static final Log log = LogFactory.getLog(DbAccountProperties.class);

	private static final String PROPERTY_FILE_PATH =  System.getProperty("hinemos.manager.etc.dir") + File.separator + "db_account.properties";
	private static final String PROP_HINEMOS_QUARTZ_USER = "hinemos_quartz_user";
	private static final String PROP_HINEMOS_QUARTZ_PASS = "hinemos_quartz_pass";

	private static Properties properties = new Properties();

	static {
		FileInputStream input = null;
		try {
			input = new FileInputStream(PROPERTY_FILE_PATH);
			properties.load(input);
			log.info(String.format("The property file %s was loaded.", PROPERTY_FILE_PATH));
		} catch (IOException e) {
			log.warn(String.format("Failed to load properties from file %s", PROPERTY_FILE_PATH), e);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					log.warn(String.format("Failed to close file %s", PROPERTY_FILE_PATH), e);
				}
			}
		}
	}

	/**
	 * hinemos_quartz DBのユーザ名を取得
	 * @return hinemos_quartz DBのユーザ名
	 */
	public static String getHinemosQuartzUser() {
		return properties.getProperty(PROP_HINEMOS_QUARTZ_USER);
	}

	/**
	 * hinemos_quartz DBのユーザパスワードを取得
	 * @return hinemos_quartz DBのユーザパスワード
	 */
	public static String getHinemosQuartzPass() {
		return properties.getProperty(PROP_HINEMOS_QUARTZ_PASS);
	}
}