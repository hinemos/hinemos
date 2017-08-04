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

package com.clustercontrol.plugin.impl;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import com.clustercontrol.plugin.api.HinemosPlugin;

/**
 * log4j.propertiesを定期的にリロードするプラグインサービス<br/>
 */
public class Log4jReloadPlugin implements HinemosPlugin {

	public static final Log log = LogFactory.getLog(Log4jReloadPlugin.class);

	// log4j.propertiesのファイルパス
	public static final String _configFilePath;
	public static final String _configFilePathDefault = System.getProperty("hinemos.manager.etc.dir") + File.separator + "log4j.properties";

	// リロードの実行インターバル
	public static final int _intervalMsec;
	public static final int _intervalMsecDefault = 60000;

	static {
		_configFilePath = System.getProperty("hinemos.log4j.file", _configFilePathDefault);

		String intervalMsecStr = System.getProperty("hinemos.log4j.interval");
		int intervalMSec = _intervalMsecDefault;
		try {
			intervalMSec = Integer.parseInt(intervalMsecStr);
		} catch (NumberFormatException e) { }
		_intervalMsec = intervalMSec;
	}

	@Override
	public Set<String> getDependency() {
		return new HashSet<String>();
	}

	@Override
	public void create() {
		// do nothing
	}

	@Override
	public void activate() {
		// 定期的にリロードする処理を開始する
		PropertyConfigurator.configureAndWatch(_configFilePath, _intervalMsec);
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
