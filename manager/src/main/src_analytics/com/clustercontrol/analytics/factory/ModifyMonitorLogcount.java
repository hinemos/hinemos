/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.analytics.factory;

import com.clustercontrol.analytics.model.LogcountCheckInfo;
import com.clustercontrol.analytics.util.QueryUtil;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.ModifyMonitor;
import com.clustercontrol.monitor.run.factory.ModifyMonitorNumericValueType;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;

/**
 * ログ件数監視情報更新クラス
 *
 * @version 6.1.0
 */
public class ModifyMonitorLogcount extends ModifyMonitorNumericValueType{
	/* (非 Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.AddMonitor#addCheckInfo()
	 */
	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, InvalidRole {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			
			LogcountCheckInfo logCountInfo = m_monitorInfo.getLogcountCheckInfo();
			
			logCountInfo.setMonitorId(m_monitorInfo.getMonitorId());
			em.persist(logCountInfo);
			
			return true;
		}
	}
	
	/* (非 Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.ModifyMonitor#modifyCheckInfo()
	 */
	@Override
	protected boolean modifyCheckInfo() throws MonitorNotFound, InvalidRole {

		MonitorInfo monitorEntity
		= com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId());

		// ログ件数監視情報を取得
		LogcountCheckInfo entity = QueryUtil.getMonitorLogcountInfoPK(m_monitorInfo.getMonitorId());

		// ログ件数監視情報を設定
		LogcountCheckInfo logCount = m_monitorInfo.getLogcountCheckInfo();
		entity.setTargetMonitorId(logCount.getTargetMonitorId());
		entity.setKeyword(logCount.getKeyword());
		entity.setIsAnd(logCount.getIsAnd());
		if (logCount.getTag() == null || logCount.getTag().isEmpty()) {
			entity.setTag(null);
		} else {
			entity.setTag(logCount.getTag());
		}
		monitorEntity.setLogcountCheckInfo(entity);
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
}
