/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.clustercontrol.ClusterControlPlugin;

/**
 * 設定画面を初期化するクラス。
 * このクラスはプラグインロード時に1回だけ呼ばれる。
 * @since 1.0.0
 */
public class ReportingPreferenceInitializer extends AbstractPreferenceInitializer {
	
	public static final int DL_MAX_WAIT_DEFAULT = 30;  // レポートダウンロード待ち時間最大値(分)
	public static final int DL_CHECK_INTERVAL_DEFAULT = 1;     // レポート確認間隔(分)
	
	/**
	 * プレファレンスの初期化
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();
		
		store.setDefault(ReportingPreferencePage.P_DL_MAX_WAIT, DL_MAX_WAIT_DEFAULT);
		store.setDefault(ReportingPreferencePage.P_DL_CHECK_INTREVAL, DL_CHECK_INTERVAL_DEFAULT);
	}
}
