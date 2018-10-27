/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ping.factory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.ModifyMonitor;
import com.clustercontrol.monitor.run.factory.ModifyMonitorNumericValueType;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.ping.model.PingCheckInfo;
import com.clustercontrol.ping.util.QueryUtil;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;

/**
 * ping監視情報を更新するクラス<BR>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class ModifyMonitorPing extends ModifyMonitorNumericValueType{
	/* (非 Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.AddMonitor#addCheckInfo()
	 */
	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, InvalidRole {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// ping監視情報を設定
			PingCheckInfo ping = m_monitorInfo.getPingCheckInfo();
			ping.setMonitorId(m_monitorInfo.getMonitorId());

			em.persist(ping);
 
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

		// ping監視情報を取得
		PingCheckInfo entity = QueryUtil.getMonitorPingInfoPK(m_monitorInfo.getMonitorId());

		// ping監視情報を設定
		PingCheckInfo ping = m_monitorInfo.getPingCheckInfo();
		entity.setRunCount(ping.getRunCount());
		entity.setRunInterval(ping.getRunInterval());
		entity.setTimeout(ping.getTimeout());
		monitorEntity.setPingCheckInfo(entity);

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
