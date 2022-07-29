/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.plugin;
import org.eclipse.swt.widgets.Shell;

public interface IMonitorPlugin {
	static int MONITOR_TYPE_BOOLEAN = 0;
	static int MONITOR_TYPE_NUMERIC = 1;
	static int MONITOR_TYPE_STRING = 2;
	static int MONITOR_TYPE_BINARY = 3;

	String getOption();
	String getMonitorPluginId();
	int getMonitorCode();
	String getMonitorName();
	int getMonitorType();

	int create(Shell shell, String managerName, String monitorId, boolean updateFlg);
}
