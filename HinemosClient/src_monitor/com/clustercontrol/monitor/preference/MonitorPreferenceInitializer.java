/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.clustercontrol.ClusterControlPlugin;

public class MonitorPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();

		store.setDefault(MonitorPreferencePage.P_SCOPE_UPDATE_FLG, true);
		store.setDefault(MonitorPreferencePage.P_SCOPE_UPDATE_CYCLE, 10);
		store.setDefault(MonitorPreferencePage.P_STATUS_UPDATE_FLG, true);
		store.setDefault(MonitorPreferencePage.P_STATUS_UPDATE_CYCLE, 10);
		store.setDefault(MonitorPreferencePage.P_STATUS_NEW_STATE_FLG, true);
		store.setDefault(MonitorPreferencePage.P_EVENT_UPDATE_FLG, true);
		store.setDefault(MonitorPreferencePage.P_EVENT_UPDATE_CYCLE, 10);
		store.setDefault(MonitorPreferencePage.P_EVENT_MESSAGE_FLG, false);
		store.setDefault(MonitorPreferencePage.P_EVENT_MAX, 100);
		store.setDefault(MonitorPreferencePage.P_EVENT_NEW_EVENT_FLG, true);
		store.setDefault(MonitorPreferencePage.P_MAX_TRAP_OID, 100);
	}
}
