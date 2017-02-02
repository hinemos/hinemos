package com.clustercontrol.hub.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.clustercontrol.ClusterControlPlugin;


public class HubPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();
		
		store.setDefault(HubPreferencePage.P_SIZE_POS, HubPreferencePage.PAGE_MAX_DEFAULT);

	}
}
