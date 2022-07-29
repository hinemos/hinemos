/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.xcloud.factory.monitors;

import com.clustercontrol.commons.scheduler.TriggerSchedulerException;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorIdInvalid;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.plugin.factory.ModifyMonitorPluginString;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;

import jakarta.persistence.EntityExistsException;

public class ModifyMonitorCloudLog extends ModifyMonitorPluginString {

	@Override
	protected TriggerType getTriggerType() {

		return TriggerType.NONE;
	}

	@Override
	public boolean addMonitorInfo(String user) throws MonitorIdInvalid, MonitorNotFound, EntityExistsException,
			InvalidRole, TriggerSchedulerException, HinemosUnknown {
		boolean result = super.addMonitorInfo(user);

		if(result){
			// 追加に成功した場合、最終実行日時を作成
			new MonitorCloudLogControllerBean().addRunStatusEntityIfNotExists(m_monitorInfo.getMonitorId());
		}
		
		return result;
	}

	@Override
	public boolean modifyMonitorInfo(String user)
			throws MonitorNotFound, TriggerSchedulerException, HinemosUnknown, InvalidRole {
		boolean result = super.modifyMonitorInfo(user);

		if (result) {
			if (m_isModifyDisableFlg) {
				// 変更に成功、かつ監視・収集がどちらも無効に変更されていた場合、
				// 最終実行日時を削除
				new MonitorCloudLogControllerBean().deleteRunStatusEntity(m_monitorInfo.getMonitorId());
			} else if (m_isModifyEnableFlg) {
				// 変更に成功、かつ監視・収集のどちらかが有効に変更されていた場合、
				// 最終実行日時を作成
				new MonitorCloudLogControllerBean().addRunStatusEntityIfNotExists(m_monitorInfo.getMonitorId());
			}
		}

		return result;
	}

}
