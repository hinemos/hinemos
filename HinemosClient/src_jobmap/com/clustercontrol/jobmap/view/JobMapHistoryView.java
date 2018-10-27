/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.IPreferenceStore;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.jobmanagement.preference.JobManagementPreferencePage;

/**
 * ノードマップビューを描画するためのクラス。
 * コントロール部分はMapViewController
 */
public class JobMapHistoryView extends JobMapView {

	// ログ
	private static Log m_log = LogFactory.getLog( JobMapHistoryView.class );

	public static final String ID = JobMapHistoryView.class.getName();

	public JobMapHistoryView(){
		m_log.debug("JobMapHistoryView constructor");
	}

	@Override
	public void applySetting() {
		// 設定情報反映
		IPreferenceStore store = ClusterControlPlugin.getDefault()
		.getPreferenceStore();

		int interval = store
		.getInt(JobManagementPreferencePage.P_HISTORY_UPDATE_CYCLE);
		this.setInterval(interval);

		boolean updateFlag = store.getBoolean(JobManagementPreferencePage.P_HISTORY_UPDATE_FLG);
		if (updateFlag) {
			this.startAutoReload();
		} else {
			this.stopAutoReload();
		}
		m_log.debug("JobMapHistoryView applySetting : " + interval + ", " + updateFlag);
		
		m_canvasComposite.applySetting();
		updateNotManagerAccess();
	}

	@Override
	protected String getViewName() {
		return this.getClass().getName();
	}
}
