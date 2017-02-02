package com.clustercontrol.approval.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.clustercontrol.ClusterControlPlugin;

public class ApprovalPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();
		store.setDefault(ApprovalPreferencePage.P_APPROVAL_MESSAGE_FLG, false);
		store.setDefault(ApprovalPreferencePage.P_APPROVAL_MAX_LIST, 500);
	}
}
