/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.util.LoginManager;

public class ClusterControlInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();

		store.setDefault(ClusterControlCorePreferencePage.URL, LoginManager.VALUE_URL);
		store.setDefault(ClusterControlCorePreferencePage.KEY_INTERVAL, LoginManager.VALUE_INTERVAL);
		store.setDefault(ClusterControlCorePreferencePage.KEY_HTTP_REQUEST_TIMEOUT, LoginManager.VALUE_HTTP_REQUEST_TIMEOUT);
		store.setDefault(ClusterControlCorePreferencePage.KEY_PROXY_ENABLE, LoginManager.VALUE_PROXY_ENABLE);
		store.setDefault(ClusterControlCorePreferencePage.KEY_PROXY_HOST, LoginManager.VALUE_PROXY_HOST);
		store.setDefault(ClusterControlCorePreferencePage.KEY_PROXY_PORT, LoginManager.VALUE_PROXY_PORT);
		store.setDefault(ClusterControlCorePreferencePage.KEY_PROXY_USER, LoginManager.VALUE_PROXY_USER);
		store.setDefault(ClusterControlCorePreferencePage.KEY_PROXY_PASSWORD, LoginManager.VALUE_PROXY_PASSWORD);
	}

}
