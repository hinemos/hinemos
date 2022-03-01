/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.binary.session.BinaryControllerBean;
import com.clustercontrol.binary.util.BinaryManagerUtil;
import com.clustercontrol.commons.bean.SettingUpdateInfo;
import com.clustercontrol.commons.util.JpaTransactionCallback;
import com.clustercontrol.custom.factory.SelectCustom;
import com.clustercontrol.custom.util.CustomManagerUtil;
import com.clustercontrol.logfile.session.MonitorLogfileControllerBean;
import com.clustercontrol.logfile.util.LogfileManagerUtil;
import com.clustercontrol.notify.util.NotifyRelationCache;
import com.clustercontrol.systemlog.util.SystemlogCache;
import com.clustercontrol.rpa.monitor.session.MonitorRpaLogfileControllerBean;
import com.clustercontrol.rpa.util.RpaLogfileManagerUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.winevent.session.MonitorWinEventControllerBean;
import com.clustercontrol.winevent.util.WinEventManagerUtil;
import com.clustercontrol.xcloud.factory.monitors.CloudLogManagerUtil;
import com.clustercontrol.xcloud.factory.monitors.MonitorCloudLogControllerBean;

public class MonitorChangedNotificationCallback implements JpaTransactionCallback {

	private static Log m_log = LogFactory.getLog( MonitorChangedNotificationCallback.class );
	
	public final String monitorTypeId;
	public final boolean flag;
	
	public MonitorChangedNotificationCallback(String monitorTypeId ,boolean flag ) {
		this.monitorTypeId = monitorTypeId;
		this.flag = flag;
	}
	
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
			LogfileManagerUtil.broadcastConfiguredFlowControl();
			break;
		case HinemosModuleConstant.MONITOR_RPA_LOGFILE :
			MonitorRpaLogfileControllerBean.refreshCache();
			
			SettingUpdateInfo.getInstance().setRpaLogFileMonitorUpdateTime(HinemosTime.currentTimeMillis());
			
			// 接続中のHinemosAgentに対する更新通知
			RpaLogfileManagerUtil.broadcastConfiguredFlowControl();
			break;
		case HinemosModuleConstant.MONITOR_CLOUD_LOG :
			MonitorCloudLogControllerBean.refreshCache();
			
			SettingUpdateInfo.getInstance().setCloudLogMonitorUpdateTime(HinemosTime.currentTimeMillis());
			
			// 接続中のHinemosAgentに対する更新通知
			CloudLogManagerUtil.broadcastConfiguredFlowControl();
			break;
		case HinemosModuleConstant.MONITOR_BINARYFILE_BIN:
		case HinemosModuleConstant.MONITOR_PCAP_BIN:
			BinaryControllerBean.refreshCache();

			SettingUpdateInfo.getInstance().setBinaryMonitorUpdateTime(HinemosTime.currentTimeMillis());
			
			// 接続中のHinemosAgentに対する更新通知
			BinaryManagerUtil.broadcastConfiguredFlowControl();
			break;
		case HinemosModuleConstant.MONITOR_CUSTOM_N :
			SelectCustom.refreshCache();
			
			SettingUpdateInfo.getInstance().setCustomMonitorUpdateTime(HinemosTime.currentTimeMillis());
			
			// 接続中のHinemosAgentに対する更新通知
			CustomManagerUtil.broadcastConfiguredFlowControl();
			break;
		case HinemosModuleConstant.MONITOR_CUSTOM_S :
			SelectCustom.refreshCache();
			
			SettingUpdateInfo.getInstance().setCustomMonitorUpdateTime(HinemosTime.currentTimeMillis());
			
			// 接続中のHinemosAgentに対する更新通知
			CustomManagerUtil.broadcastConfiguredFlowControl();
			break;
		case HinemosModuleConstant.MONITOR_WINEVENT :
			MonitorWinEventControllerBean.refreshCache();
			
			SettingUpdateInfo.getInstance().setWinEventMonitorUpdateTime(HinemosTime.currentTimeMillis());
			
			// 接続中のHinemosAgentに対する更新通知
			WinEventManagerUtil.broadcastConfiguredFlowControl();
			break;
		case HinemosModuleConstant.MONITOR_CUSTOMTRAP_N :
		case HinemosModuleConstant.MONITOR_CUSTOMTRAP_S :
			SettingUpdateInfo.getInstance().setCustomTrapMonitorUpdateTime(HinemosTime.currentTimeMillis());
			break;
		default :
		}
		if (flag) {
			try {
				NotifyRelationCache.refresh();

				if (HinemosModuleConstant.MONITOR_SYSTEMLOG.equals(monitorTypeId)) {
					SystemlogCache.refresh();
				}
			} catch (Exception e) {
				m_log.warn("addMonitor() transaction failure. : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				throw e;
			}
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
