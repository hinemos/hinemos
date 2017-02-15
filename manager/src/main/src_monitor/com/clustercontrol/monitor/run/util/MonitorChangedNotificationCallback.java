/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.monitor.run.util;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.bean.SettingUpdateInfo;
import com.clustercontrol.commons.util.JpaTransactionCallback;
import com.clustercontrol.custom.factory.SelectCustom;
import com.clustercontrol.custom.util.CustomManagerUtil;
import com.clustercontrol.logfile.session.MonitorLogfileControllerBean;
import com.clustercontrol.logfile.util.LogfileManagerUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.winevent.session.MonitorWinEventControllerBean;
import com.clustercontrol.winevent.util.WinEventManagerUtil;

public class MonitorChangedNotificationCallback implements JpaTransactionCallback {
	
	public final String monitorTypeId;
	
	public MonitorChangedNotificationCallback(String monitorTypeId) {
		this.monitorTypeId = monitorTypeId;
	}
	
	@Override
	public void preBegin() { }

	@Override
	public void postBegin() { }

	@Override
	public void preFlush() { }

	@Override
	public void postFlush() { }

	@Override
	public void preCommit() { }

	@Override
	public void postCommit() {
		
		switch (monitorTypeId) {
		case HinemosModuleConstant.MONITOR_SYSTEMLOG :
			SettingUpdateInfo.getInstance().setSystemLogMonitorUpdateTime(HinemosTime.currentTimeMillis());
			break;
		case HinemosModuleConstant.MONITOR_SNMPTRAP :
			SettingUpdateInfo.getInstance().setSnmptrapMonitorUpdateTime(HinemosTime.currentTimeMillis());
			break;
		case HinemosModuleConstant.MONITOR_LOGFILE :
			MonitorLogfileControllerBean.refreshCache();
			
			SettingUpdateInfo.getInstance().setLogFileMonitorUpdateTime(HinemosTime.currentTimeMillis());
			
			// 接続中のHinemosAgentに対する更新通知
			LogfileManagerUtil.broadcastConfigured();
			break;
		case HinemosModuleConstant.MONITOR_CUSTOM_N :
			SelectCustom.refreshCache();
			
			SettingUpdateInfo.getInstance().setCustomMonitorUpdateTime(HinemosTime.currentTimeMillis());
			
			// 接続中のHinemosAgentに対する更新通知
			CustomManagerUtil.broadcastConfigured();
			break;
		case HinemosModuleConstant.MONITOR_CUSTOM_S :
			SelectCustom.refreshCache();
			
			SettingUpdateInfo.getInstance().setCustomMonitorUpdateTime(HinemosTime.currentTimeMillis());
			
			// 接続中のHinemosAgentに対する更新通知
			CustomManagerUtil.broadcastConfigured();
			break;
		case HinemosModuleConstant.MONITOR_WINEVENT :
			MonitorWinEventControllerBean.refreshCache();
			
			SettingUpdateInfo.getInstance().setWinEventMonitorUpdateTime(HinemosTime.currentTimeMillis());
			
			// 接続中のHinemosAgentに対する更新通知
			WinEventManagerUtil.broadcastConfigured();
			break;
		case HinemosModuleConstant.MONITOR_CUSTOMTRAP_N :
		case HinemosModuleConstant.MONITOR_CUSTOMTRAP_S :
			SettingUpdateInfo.getInstance().setCustomTrapMonitorUpdateTime(HinemosTime.currentTimeMillis());
			break;
		default :
		}
	}

	@Override
	public void preRollback() { }

	@Override
	public void postRollback() { }

	@Override
	public void preClose() { }

	@Override
	public void postClose() {}
	
	@Override
	public int hashCode() {
		int h = 1;
		h = h * 31 + (monitorTypeId == null ? 0 : monitorTypeId.hashCode());
		return h;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof MonitorChangedNotificationCallback) {
			MonitorChangedNotificationCallback cast = (MonitorChangedNotificationCallback)obj;
			if (monitorTypeId != null && monitorTypeId.equals(cast.monitorTypeId)) {
				return true;
			}
		}
		return false;
	}
	
}
