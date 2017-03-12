package com.clustercontrol.monitor.plugin;
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.monitor.action.DeleteInterface;

public interface IMonitorPlugin {
	static int MONITOR_TYPE_BOOLEAN = 0;
	static int MONITOR_TYPE_NUMERIC = 1;
	static int MONITOR_TYPE_STRING = 2;

	String getMonitorPluginId();
	int getMonitorCode();
	String getMonitorName();
	int getMonitorType();

	int create(Shell shell, String managerName, String monitorId, boolean updateFlg);
	DeleteInterface getDeleteMonitorClassObject();
}
