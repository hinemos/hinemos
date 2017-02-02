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
		// ping監視情報を設定
		PingCheckInfo ping = m_monitorInfo.getPingCheckInfo();
		ping.setMonitorId(m_monitorInfo.getMonitorId());

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(ping);
 
		return true;
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
