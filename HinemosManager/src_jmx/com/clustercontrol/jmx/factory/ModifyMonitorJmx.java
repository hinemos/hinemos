/*

 Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jmx.factory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.jmx.model.JmxCheckInfo;
import com.clustercontrol.jmx.util.ControlJmxInfo;
import com.clustercontrol.monitor.run.factory.ModifyMonitor;
import com.clustercontrol.monitor.run.factory.ModifyMonitorNumericValueType;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;

/**
 * JMX 監視 数値監視をマネージャで変更するクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class ModifyMonitorJmx extends ModifyMonitorNumericValueType{
	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, InvalidRole {

		// JMX 監視情報を追加
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		JmxCheckInfo checkinfo = m_monitorInfo.getJmxCheckInfo();
		checkinfo.setMonitorId(m_monitorInfo.getMonitorId());
		em.persist(checkinfo);
		return true;
	}
	
	@Override
	protected boolean modifyCheckInfo() throws MonitorNotFound, InvalidRole {

		// JMX 監視情報を変更
		return new ControlJmxInfo().modify(m_monitorInfo.getMonitorId(), m_monitorInfo.getJmxCheckInfo());
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
