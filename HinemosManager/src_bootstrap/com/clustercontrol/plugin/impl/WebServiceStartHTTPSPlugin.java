/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.impl;

import java.util.HashSet;
import java.util.Set;

import com.clustercontrol.plugin.api.HinemosPlugin;

public class WebServiceStartHTTPSPlugin extends WebServicePlugin implements
HinemosPlugin {

	@Override
	public Set<String> getDependency() {
		Set<String> dependency = new HashSet<String>();
		dependency.add(WebServiceAgentPlugin.class.getName());
		dependency.add(WebServiceCorePlugin.class.getName());
		return dependency;
	}

	@Override
	public void create() {
	}

	@Override
	public void activate() {
		super.startHTTPS();
	}
}
