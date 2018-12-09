/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.http.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.http.factory.RunMonitorHttpScenario;
import com.clustercontrol.monitor.run.bean.MonitorRunResultInfo;

/**
 * 
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class CallableTaskHttpScenario implements Callable<ArrayList<MonitorRunResultInfo>>{
	private RunMonitorHttpScenario m_runMonitor;
	private String m_facilityId;

	/**
	 * コンストラクタ
	 * @param monitor
	 * @param facilityId
	 */
	public CallableTaskHttpScenario(RunMonitorHttpScenario monitor, String facilityId) {
		m_runMonitor = monitor;
		m_facilityId = facilityId;
	}

	/**
	 * 各監視を実行します。
	 * 
	 * @see #setMonitorInfo(String, String)
	 */
	@Override
	public ArrayList<MonitorRunResultInfo> call() throws Exception {
		JpaTransactionManager jtm = null;
		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 各監視処理を実行し、実行の可否を格納
			List<MonitorRunResultInfo> infoList = m_runMonitor.collectList(m_facilityId);

			// コミット
			jtm.commit();
			
			return new ArrayList<>(infoList);
		} catch (Exception e) {
			if (jtm != null)
				jtm.rollback();
			throw e;
		} finally {
			// 一時停止していたトランザクションを再開
			if (jtm != null)
				jtm.close();
		}
	}
}
