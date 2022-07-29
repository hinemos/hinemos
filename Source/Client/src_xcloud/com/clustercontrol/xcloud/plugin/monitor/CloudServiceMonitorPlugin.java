/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
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

public class CloudServiceMonitorPlugin implements IMonitorPlugin, CloudStringConstants {
	public static final String option = "xcloud";
	public static final String monitorPluginId = HinemosModuleConstant.MONITOR_CLOUD_SERVICE_CONDITION;
	private static final int monitorCode = 252;
	private static final int monitorType = IMonitorPlugin.MONITOR_TYPE_BOOLEAN;

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
		return strMonitorCloudServiceCondition;
	}

	@Override
	public int getMonitorType() {
		return monitorType;
	}

	@Override
	public int create(Shell shell, String managerName, String monitorId, boolean updateFlg) {
		CloudServiceCreateDialog dialog = new CloudServiceCreateDialog(shell, managerName, monitorId, updateFlg);
		return dialog.open();
	}
}
