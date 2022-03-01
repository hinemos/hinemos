/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.plugin.factory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.plugin.util.PluginCheckInfoUtil;
import com.clustercontrol.monitor.run.factory.ModifyMonitor;
import com.clustercontrol.monitor.run.factory.ModifyMonitorNumericValueType;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;

public class ModifyMonitorPluginNumeric extends ModifyMonitorNumericValueType {
	@Override
	protected TriggerType getTriggerType() {
		return TriggerType.SIMPLE;
	}

	@Override
	protected int getDelayTime() {
		return ModifyMonitor.getDelayTimeBasic(m_monitorInfo);
	}

	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, HinemosUnknown,
			InvalidRole {
		m_monitorInfo.getPluginCheckInfo().setMonitorId(m_monitorInfo.getMonitorId());
		return (new PluginCheckInfoUtil()).addCheckInfo(m_monitorInfo.getPluginCheckInfo());
	}
	
	@Override
	protected boolean modifyCheckInfo() throws MonitorNotFound, InvalidRole,
			HinemosUnknown {
		m_monitorInfo.getPluginCheckInfo().setMonitorId(m_monitorInfo.getMonitorId());
		return (new PluginCheckInfoUtil()).modifyCheckInfo(m_monitorInfo.getPluginCheckInfo());
	}
}
