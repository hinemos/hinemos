/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.util;

import java.net.MalformedURLException;
import java.net.URL;

import com.clustercontrol.util.RestConnectManager;


public class ManagerAddressProvider {
	private static ManagerAddressProvider instance = null;

	private ManagerAddressProvider() { }

	protected static void setInstance(ManagerAddressProvider instance) {
		ManagerAddressProvider.instance = instance;
	}

	public static ManagerAddressProvider getInstance() {
		if (instance == null) {
			instance = new ManagerAddressProvider();
		}
		return instance;
	}

	public String getManagerAddress() {
		if (UtilityManagerUtil.getCurrentManagerName() == null) {return null;}
		try {
			URL url = new URL(RestConnectManager.get(UtilityManagerUtil.getCurrentManagerName()).getUrlListStr());
			return url.getHost();
		} catch (MalformedURLException e) {
			// TODO
		}
		return null;
	}
}
