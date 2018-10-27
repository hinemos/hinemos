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

public class PlatformServiceBillingMonitorPlugin implements IMonitorPlugin, CloudStringConstants {
	public static final String option = "xcloud";
	public static final String monitorPluginId = "MON_CLOUD_SERVICE_BILLING";
	private static final int monitorCode = 251;
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
		return strMonitorCloudServiceBilling;
	}

	@Override
	public int getMonitorType() {
		return monitorType;
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
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.monitor.notfound", monitorIdList.toArray()));
			}
			return result;
		}
	}

	@Override
	public int create(Shell shell, String managerName, String monitorId, boolean updateFlg) {
		CreateBillingMonitorDialog dialog = new CreateBillingMonitorDialog(shell, managerName, monitorId, updateFlg);
		return dialog.open();
	}
}
