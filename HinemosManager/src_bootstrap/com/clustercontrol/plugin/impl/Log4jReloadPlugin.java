/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.impl;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import com.clustercontrol.plugin.api.HinemosPlugin;

/**
 * log4j2.propertiesを定期的にリロードするプラグインサービス<br/>
 */
public class Log4jReloadPlugin implements HinemosPlugin {

	public static final Log log = LogFactory.getLog(Log4jReloadPlugin.class);

	// log4j2.propertiesのファイルパス
	public static final String _configFilePath;
	public static final String _configFilePathDefault = System.getProperty("hinemos.manager.etc.dir") + File.separator + "log4j2.properties";

	static {
		_configFilePath = System.getProperty("hinemos.log4j.file", _configFilePathDefault);
	}

	@Override
	public Set<String> getDependency() {
		return new HashSet<String>();
	}

	@Override
	public Set<String> getRequiredKeys() {
		return null;
	}

	@Override
	public void create() {
		// do nothing
	}

	@Override
	public void activate() {
		// 定期的にリロードする処理を開始する
		LoggerContext context = (LoggerContext) LogManager.getContext(false);
		context.setConfigLocation(Paths.get(_configFilePath).toUri());
	}

	@Override
	public void deactivate() {
		// do nothing
	}

	@Override
	public void destroy() {
		// do nothing
	}


}
