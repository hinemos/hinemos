/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.http.factory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.http.util.ControlHttpInfo;
import com.clustercontrol.monitor.run.factory.ModifyMonitor;
import com.clustercontrol.monitor.run.factory.ModifyMonitorNumericValueType;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;

/**
 * HTTP監視 数値監視をマネージャで変更するクラス<BR>
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class ModifyMonitorHttp extends ModifyMonitorNumericValueType{

	/* (non-Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.ModifyMonitor#modifyCheckInfo()
	 */
	@Override
	protected boolean modifyCheckInfo() throws MonitorNotFound, InvalidRole {

		// HTTP監視情報を変更
		return new ControlHttpInfo().modify(m_monitorInfo.getMonitorId(), m_monitorInfo.getHttpCheckInfo());
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

	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, InvalidRole {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// HTTP監視情報を追加
			em.persist(m_monitorInfo.getHttpCheckInfo());
			return true;
		}
	}
}
