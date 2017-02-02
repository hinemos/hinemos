/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
