/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.monitor.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.ModifyMonitor;
import com.clustercontrol.monitor.run.factory.ModifyMonitorTruthValueType;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;
import com.clustercontrol.rpa.monitor.model.RpaManagementToolServiceCheckInfo;
import com.clustercontrol.rpa.util.QueryUtil;

/**
 * RPA管理ツールサービス監視情報更新クラス<BR>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class ModifyMonitorRpaManagementToolService extends ModifyMonitorTruthValueType{
	private static Log m_log = LogFactory.getLog( ModifyMonitorRpaLogfile.class );
	/**
	 * RPA管理ツールサービス監視情報を設定します。<BR>
	 */
	/* (non-Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.AddMonitor#addCheckInfo()
	 */
	@Override
	protected boolean addCheckInfo() throws MonitorNotFound {
		// RPA管理ツールサービス監視情報を設定
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			RpaManagementToolServiceCheckInfo checkInfo = m_monitorInfo.getRpaManagementToolServiceCheckInfo();
			checkInfo.setMonitorId(m_monitorInfo.getMonitorId());

			// ログファイル監視情報を追加
			em.persist(checkInfo);
			return true;
		}
	}
	
	/* (non-Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.ModifyMonitor#modifyCheckInfo()
	 */
	@Override
	protected boolean modifyCheckInfo() throws MonitorNotFound {
		// RPA管理ツールサービス監視情報を取得
		RpaManagementToolServiceCheckInfo oldEntity = m_monitorInfo.getRpaManagementToolServiceCheckInfo();
		RpaManagementToolServiceCheckInfo entity = QueryUtil.getMonitorRpaManagementToolServiceInfoPK_NONE(m_monitorInfo.getMonitorId());
		
		
		// RPA管理ツールサービス監視情報を設定
		entity.setConnectTimeout(oldEntity.getConnectTimeout());
		entity.setRequestTimeout(oldEntity.getRequestTimeout());
		m_log.trace("modify() : entity.getConnectTimeout = " + entity.getConnectTimeout());
		m_log.trace("modify() : entity.getRequestTimeout = " + entity.getRequestTimeout());
		
		return true;
	}

	/**
	 * スケジュール実行の遅延時間を返します。
	 */
	@Override
	protected int getDelayTime() {
		return ModifyMonitor.getDelayTimeBasic(m_monitorInfo);
	}

	/**
	 * スケジュール実行種別を返します。
	 */
	@Override
	protected TriggerType getTriggerType() {
		return TriggerType.SIMPLE;
	}
	
	/* (non-Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.DeleteMonitor#deleteCheckInfo()
	 */
	@Override
	protected boolean deleteCheckInfo() {
		return true;
	}
}