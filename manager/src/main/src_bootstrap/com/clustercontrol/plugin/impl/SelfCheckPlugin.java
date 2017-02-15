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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.HinemosManagerMain;
import com.clustercontrol.HinemosManagerMain.StartupMode;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.selfcheck.SelfCheckTaskSubmitter;

/**
 * セルフチェック機能の設定にしたがって、定期的に内部状態を診断するプラグイン.
 */
public class SelfCheckPlugin implements HinemosPlugin {

	private static final Log log = LogFactory.getLog(SelfCheckPlugin.class);

	private SelfCheckTaskSubmitter _selfcheck;

	@Override
	public Set<String> getDependency() {
		Set<String> dependency = new HashSet<String>();
		dependency.add(WebServiceStartHTTPSPlugin.class.getName());
		return dependency;
	}

	@Override
	public void create() {
		_selfcheck = new SelfCheckTaskSubmitter();
	}

	@Override
	public void activate() {
		if (HinemosManagerMain._startupMode == StartupMode.MAINTENANCE) {
			log.info("skipped activation (startup mode is MAINTENANCE) : SelfCheckPlugin");
			return;
		}

		_selfcheck.start();
	}

	@Override
	public void deactivate() {
		if (HinemosManagerMain._startupMode == StartupMode.MAINTENANCE) {
			log.info("skipped deactivation (startup mode is MAINTENANCE) : SelfCheckPlugin");
			return;
		}

		_selfcheck.shutdown();
	}

	@Override
	public void destroy() {
		// do nothing
	}

}
