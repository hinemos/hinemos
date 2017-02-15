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

package com.clustercontrol.http.factory;

import javax.persistence.EntityExistsException;

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
