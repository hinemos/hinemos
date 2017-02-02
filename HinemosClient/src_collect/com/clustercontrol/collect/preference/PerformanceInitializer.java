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

package com.clustercontrol.collect.preference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.clustercontrol.ClusterControlPlugin;

/**
 * 性能管理機能のプリファレンスの初期化<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class PerformanceInitializer extends AbstractPreferenceInitializer {

	private static final Log log = LogFactory.getLog( PerformanceInitializer.class );

	public static final int GRAPH_MAX_DEFAULT = 100; // グラフ表示の最大数(デフォルト)
	public static final int DL_MAX_WAIT_DEFAULT = 10; // ダウンロード待ち時間最大値(分)

	/**
	 * プリファレンスの初期化
	 */
	@Override
	public void initializeDefaultPreferences() {
		log.debug("initializeDefaultPreferences()");

		IPreferenceStore store = ClusterControlPlugin.getDefault()
				.getPreferenceStore();

		log.debug("initializeDefaultPreferences() " + PerformancePreferencePage.P_GRAPH_MAX + " = " + GRAPH_MAX_DEFAULT);
		store.setDefault(PerformancePreferencePage.P_GRAPH_MAX,GRAPH_MAX_DEFAULT);

		log.debug("initializeDefaultPreferences() " + PerformancePreferencePage.P_DL_MAX_WAIT + " = " + DL_MAX_WAIT_DEFAULT);
		store.setDefault(PerformancePreferencePage.P_DL_MAX_WAIT,DL_MAX_WAIT_DEFAULT);
	}

}
