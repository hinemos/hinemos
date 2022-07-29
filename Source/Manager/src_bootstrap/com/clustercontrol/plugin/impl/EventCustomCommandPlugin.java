/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.clustercontrol.monitor.session.EventCustomCommandBean;
import com.clustercontrol.plugin.api.HinemosPlugin;

public class EventCustomCommandPlugin implements HinemosPlugin {

	// Logger
	static Logger logger = Logger.getLogger( EventCustomCommandPlugin.class );
	
	@Override
	public Set<String> getDependency() {
		Set<String> dependency = new HashSet<String>();
		//依存プラグインなし
		return dependency;
	}

	@Override
	public Set<String> getRequiredKeys() {
		return null;
	}

	@Override
	public void activate() {
		EventCustomCommandBean.init();
	}

	@Override
	public void create() {
	}

	@Override
	public void deactivate() {
		EventCustomCommandBean.terminate();
	}

	@Override
	public void destroy() {
	}
}
