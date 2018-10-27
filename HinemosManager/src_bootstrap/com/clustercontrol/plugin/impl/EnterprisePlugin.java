/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.impl;

import java.util.Collections;
import java.util.Set;

import com.clustercontrol.plugin.api.HinemosPlugin;

/**
 * An blank plug-in reserved for enterprise use
 *
 */
public class EnterprisePlugin extends WebServicePlugin implements HinemosPlugin {
	@Override
	public Set<String> getDependency() {
		return Collections.emptySet();
	}

	@Override
	public Set<String> getRequiredKeys() {
		return null;
	}

	@Override
	public void create() {}

	@Override
	public void activate() {}
}
