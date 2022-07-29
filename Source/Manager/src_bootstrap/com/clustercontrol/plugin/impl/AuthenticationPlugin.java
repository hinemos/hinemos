/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.auth.Authentication;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.util.Singletons;

public class AuthenticationPlugin implements HinemosPlugin {
	public static final Log log = LogFactory.getLog(AuthenticationPlugin.class);
	
	@Override
	public Set<String> getDependency() {
		Set<String> dependency = new HashSet<String>();
		return dependency;
	}

	@Override
	public Set<String> getRequiredKeys() {
		return null;
	}

	@Override
	public void create() {
	}

	@Override
	public void activate() {
		Singletons.get(Authentication.class).eraseInternalPasswords();
	}

	@Override
	public void deactivate() {
	}

	@Override
	public void destroy() {
	}
}
