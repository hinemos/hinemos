/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.clustercontrol.ClusterControlPlugin;

/**
 * 設定画面を初期化するクラス。
 * このクラスはプラグインロード時に1回だけ呼ばれる。
 */
public class RpaPreferenceInitializer extends AbstractPreferenceInitializer {
	
	public static final int DISPLAY_MAX_SCENARIO_DEFAULT = 15;	//シナリオ表示数
	public static final int DISPLAY_MAX_NODE_DEFAULT = 15;		//ノード表示数
	
	/**
	 * プレファレンスの初期化
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();
		
		store.setDefault(RpaPreferencePage.P_MAX_DISPLAY_SCENARIOS, DISPLAY_MAX_SCENARIO_DEFAULT);
		store.setDefault(RpaPreferencePage.P_MAX_DISPLAY_NODES, DISPLAY_MAX_NODE_DEFAULT);
	}
}
