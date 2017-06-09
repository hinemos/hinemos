/*

Copyright (C) 2015 NTT DATA Corporation

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

import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.util.KeyCheck;
import com.clustercontrol.ws.utility.UtilityEndpoint;

/**
 * JAX-WSによるWEBサービスの初期化(publish)/停止(stop)を制御するUtilityオプション用プラグイン.
 *
 */
public class WebServiceUtilityPlugin extends WebServiceOptionPlugin implements HinemosPlugin {

	public static final Log log = LogFactory.getLog(WebServiceUtilityPlugin.class);

	@Override
	public Set<String> getDependency() {
		Set<String> dependency = new HashSet<String>();
		dependency.add(SnmpTrapPlugin.class.getName());
		dependency.add(SystemLogPlugin.class.getName());
		return dependency;
	}

	@Override
	public void create() {
	}

	@Override
	public void activate() {
		if (isUtility()) {
			final String addressPrefix = HinemosPropertyUtil.getHinemosPropertyStr("ws.client.address" , "http://0.0.0.0:8080");
			publish(addressPrefix, "/HinemosWS/UtilityEndpoint", new UtilityEndpoint());
		}
	}

	private boolean isUtility() {
		return KeyCheck.checkEnterprise();
	}

}
