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
