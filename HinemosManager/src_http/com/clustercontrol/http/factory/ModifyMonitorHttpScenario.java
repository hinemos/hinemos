/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.http.factory;

import jakarta.persistence.EntityExistsException;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.http.util.ControlHttpScenarioInfo;
import com.clustercontrol.monitor.run.factory.ModifyMonitor;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;

/**
 * HTTP監視 数値監視をマネージャで変更するクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class ModifyMonitorHttpScenario extends ModifyMonitor {

	/* (non-Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.ModifyMonitor#modifyCheckInfo()
	 */
	@Override
	protected boolean modifyCheckInfo() throws MonitorNotFound, InvalidRole {

		// HTTP監視情報を変更
		ControlHttpScenarioInfo http = new ControlHttpScenarioInfo(m_monitorInfo.getMonitorId(), m_monitorInfo.getMonitorTypeId());
		return http.modify(m_monitorInfo.getHttpScenarioCheckInfo());
	}

	/**
	 * スケジュール実行の遅延時間を返します。
	 */
	@Override
	protected int getDelayTime() {
		return  ModifyMonitor.getDelayTimeBasic(m_monitorInfo);
	}

	/**
	 * スケジュール実行種別を返します。
	 */
	@Override
	protected TriggerType getTriggerType() {
		return TriggerType.SIMPLE;
	}

	@Override
	protected boolean modifyJudgementInfo() throws MonitorNotFound, EntityExistsException, InvalidRole {
		return true;
	}

	@Override
	protected boolean addJudgementInfo() throws MonitorNotFound, InvalidRole {
		return true;
	}
	
	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, InvalidRole {

		// HTTP監視(シナリオ)情報を追加
		ControlHttpScenarioInfo http = new ControlHttpScenarioInfo(m_monitorInfo.getMonitorId(), m_monitorInfo.getMonitorTypeId());
		return http.add(m_monitorInfo.getHttpScenarioCheckInfo());
	}
}
