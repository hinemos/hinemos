/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.plugin.monitor;


import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.monitor.plugin.IMonitorPlugin;
import com.clustercontrol.xcloud.common.CloudStringConstants;

public class CloudLogMonitorPlugin implements IMonitorPlugin, CloudStringConstants {
	public static final String option = "xcloud";
	public static final String monitorPluginId = HinemosModuleConstant.MONITOR_CLOUD_LOG;
	private static final int monitorCode = 254;
	private static final int monitorType = IMonitorPlugin.MONITOR_TYPE_STRING;

	@Override
	public String getOption() {
		return option;
	}

	@Override
	public String getMonitorPluginId() {
		return monitorPluginId;
	}

	@Override
	public int getMonitorCode() {
		return monitorCode;
	}

	@Override
	public String getMonitorName() {
		return strMonitorCloudServiceLog;
	}

	@Override
	public int getMonitorType() {
		return monitorType;
	}

	@Override
	public int create(Shell shell, String managerName, String monitorId, boolean updateFlg) {
		CloudLogMonitorCreateDialog dialog = new CloudLogMonitorCreateDialog(shell, managerName, monitorId, updateFlg);
		return dialog.open();
	}


}
