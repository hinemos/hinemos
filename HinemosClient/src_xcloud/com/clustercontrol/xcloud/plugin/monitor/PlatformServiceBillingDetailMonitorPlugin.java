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

public class PlatformServiceBillingDetailMonitorPlugin implements IMonitorPlugin, CloudStringConstants {
	public static final String option = "xcloud";
	public static final String monitorPluginId = HinemosModuleConstant.MONITOR_CLOUD_SERVICE_BILLING_DETAIL;
	private static final int monitorCode = 253;
	private static final int monitorType = IMonitorPlugin.MONITOR_TYPE_NUMERIC;

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
		return strMonitorCloudServiceBillingDetail;
	}

	@Override
	public int getMonitorType() {
		return monitorType;
	}

	@Override
	public int create(Shell shell, String managerName, String monitorId, boolean updateFlg) {
		CreateBillingDetailMonitorDialog dialog = new CreateBillingDetailMonitorDialog(shell, managerName, monitorId, updateFlg);
		return dialog.open();
	}
}
