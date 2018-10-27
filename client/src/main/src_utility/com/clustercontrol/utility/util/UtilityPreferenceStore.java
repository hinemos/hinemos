/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.util;

import org.eclipse.jface.preference.IPreferenceStore;

import com.clustercontrol.ClusterControlPlugin;

/**
 * Wrapper class for Utility and Porting
 * 
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
public class UtilityPreferenceStore implements IUtilityPreferenceStore {

	private IPreferenceStore store;

	private UtilityPreferenceStore(){}

	public static IUtilityPreferenceStore get() {
		UtilityPreferenceStore instance = new UtilityPreferenceStore();
		instance.store = ClusterControlPlugin.getDefault().getPreferenceStore();
		return instance;
	}

	@Override
	public String getString(String name) {
		return store.getString(name);
	}

	@Override
	public void setDefault(String name, String value) {
		store.setDefault(name, value);
	}

	@Override
	public void setDefault(String name, int value) {
		store.setDefault(name, value);
	}

	@Override
	public int getInt(String name) {
		return store.getInt(name);
	}
}
