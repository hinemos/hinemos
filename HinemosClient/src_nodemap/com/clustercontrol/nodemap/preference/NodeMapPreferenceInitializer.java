/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.clustercontrol.ClusterControlPlugin;

/**
 * 設定画面を初期化するクラス。
 * このクラスはプラグインロード時に1回だけ呼ばれる。
 * @since 1.0.0
 */
public class NodeMapPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * 設定画面のデフォルト値をここで設定。
	 *  (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();

		store.setDefault(NodeMapPreferencePage.P_HISTORY_UPDATE_FLG, true);
		store.setDefault(NodeMapPreferencePage.P_HISTORY_UPDATE_CYCLE, 10);
		store.setDefault(NodeMapPreferencePage.P_GRID_SNAP_FLG, true);
		store.setDefault(NodeMapPreferencePage.P_GRID_WIDTH, 10);
		store.setDefault(NodeMapPreferencePage.P_ICON_BG_STATUS_FLG, true);
		store.setDefault(NodeMapPreferencePage.P_ICON_BG_EVENT_FLG, false);
	}
}
