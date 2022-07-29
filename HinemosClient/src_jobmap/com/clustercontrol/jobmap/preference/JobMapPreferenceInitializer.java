/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.clustercontrol.ClusterControlPlugin;

/**
 * 設定画面を初期化するクラス。
 * このクラスはプラグインロード時に1回だけ呼ばれる。
 * @since 1.0.0
 */
public class JobMapPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * 設定画面のデフォルト値をここで設定。
	 *  (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();

		store.setDefault(JobMapPreferencePage.P_MAX_DISPLAY_DEPTH, 10);
		store.setDefault(JobMapPreferencePage.P_MAX_DISPLAY_JOB, 500);
		store.setDefault(JobMapPreferencePage.P_AUTO_EXPAND, false);
		store.setDefault(JobMapPreferencePage.P_ARROW_WHITE, false);
		store.setDefault(JobMapPreferencePage.P_DETOUR_CONNECTION, true);
		store.setDefault(JobMapPreferencePage.P_COMPACT_CONNECTION, false);
		store.setDefault(JobMapPreferencePage.P_TURN_CONNECTION, true);
		store.setDefault(JobMapPreferencePage.P_TURN_WIDTH, 600);
		store.setDefault(JobMapPreferencePage.P_LABELING_ID, true);
		store.setDefault(JobMapPreferencePage.P_DRAGDROP_ID, true);
	}
}
