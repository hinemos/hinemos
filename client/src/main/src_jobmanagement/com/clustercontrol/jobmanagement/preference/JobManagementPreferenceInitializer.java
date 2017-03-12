package com.clustercontrol.jobmanagement.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.clustercontrol.ClusterControlPlugin;

public class JobManagementPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();

		store.setDefault(JobManagementPreferencePage.P_HISTORY_UPDATE_FLG, true);
		store.setDefault(JobManagementPreferencePage.P_HISTORY_UPDATE_CYCLE, 10);
		store.setDefault(JobManagementPreferencePage.P_HISTORY_MESSAGE_FLG, false);
		store.setDefault(JobManagementPreferencePage.P_HISTORY_MAX_HISTORIES, 500);
		store.setDefault(JobManagementPreferencePage.P_PLAN_MAX_SCHEDULE, 100);
	}
}
