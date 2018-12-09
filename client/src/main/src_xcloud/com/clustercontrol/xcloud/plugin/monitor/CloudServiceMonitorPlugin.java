/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.plugin.monitor;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.monitor.action.DeleteInterface;
import com.clustercontrol.monitor.plugin.IMonitorPlugin;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.xcloud.common.CloudStringConstants;

public class CloudServiceMonitorPlugin implements IMonitorPlugin, CloudStringConstants {
	public static final String option = "xcloud";
	public static final String monitorPluginId = "MON_CLOUD_SERVICE_CONDITION";
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

	@Override
	public DeleteInterface getDeleteMonitorClassObject() {
		return new DeleteMonitor();
	}

	public static class DeleteMonitor implements DeleteInterface {
		@Override
		public boolean delete(String managerName, List<String> monitorIdList) throws Exception {
			boolean result = false;
			try {
				result = MonitorSettingEndpointWrapper.getWrapper(managerName).deleteMonitor(monitorIdList);
			} catch (MonitorNotFound_Exception e) {
				String[] msgArgs = { monitorIdList.toString() };
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.monitor.notfound", msgArgs));
			}
			return result;
		}
	}
}
