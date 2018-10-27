/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// JMX 監視情報を追加
			JmxCheckInfo checkinfo = m_monitorInfo.getJmxCheckInfo();
			checkinfo.setMonitorId(m_monitorInfo.getMonitorId());
			em.persist(checkinfo);
			return true;
		}
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
