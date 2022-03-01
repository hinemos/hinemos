/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.util;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.monitor.run.bean.MonitorRunResultInfo;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.model.MonitorInfo;

/**
 * 監視設定について、監視対象の個々のノードに並行で監視をおこなうためのTaskクラス。
 * 本クラスに監視設定のRunMonitorと対象の（個別のノードの）FacilityIdをセットし、
 * Executorにより実行を行う。
 * 監視結果をリストで返す。
 * 
 * @version 6.1.0
 */
public class MonitorMultipleExecuteTask implements Callable<List<MonitorRunResultInfo>>{

	private RunMonitor m_runMonitor;
	private String m_facilityId;
	private MonitorInfo m_monitorInfo;

	// Logger
	private static Log m_log = LogFactory.getLog( MonitorMultipleExecuteTask.class );

	/**
	 * コンストラクタ
	 * @param monitor
	 * @param facilityId
	 */
	public MonitorMultipleExecuteTask(RunMonitor runMonitor, String facilityId) {
		m_runMonitor = runMonitor;
		m_facilityId = facilityId;
		m_monitorInfo=runMonitor.getMonitorInfo();
	}

	/**
	 * 各監視を実行します。
	 * 
	 * @see #setMonitorInfo(String, String)
	 */
	@Override
	public List<MonitorRunResultInfo> call() throws Exception {
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			// 各監視処理を実行し、実行の可否を格納
			m_runMonitor.callSetMonitorInfo(m_monitorInfo.getMonitorTypeId(), m_monitorInfo.getMonitorId());
			List<MonitorRunResultInfo> list = m_runMonitor.collectMultiple(m_facilityId);
			jtm.commit();
			return list;
		} catch (HinemosDbTimeout e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (InvalidSetting | HinemosUnknown e) {
			m_log.warn("call() : "
					+ "facilityId=" + m_facilityId + ", notifyGroupId=" + m_runMonitor.getNotifyGroupId()
					+ ", Exception=" + e.getClass().getSimpleName() + ", message=" + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (RuntimeException e) {
			m_log.warn("call() : "
					+ "facilityId=" + m_facilityId + ", notifyGroupId=" + m_runMonitor.getNotifyGroupId()
					+ ", Exception=" + e.getClass().getSimpleName() + ", message=" + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}
}
