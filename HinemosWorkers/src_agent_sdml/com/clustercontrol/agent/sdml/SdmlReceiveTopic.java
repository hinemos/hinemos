/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.sdml;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtSdmlControlSettingInfoResponse;
import org.openapitools.client.model.GetSdmlControlSettingForAgentResponse;
import org.openapitools.client.model.SettingUpdateInfoResponse;

import com.clustercontrol.agent.Agent;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;

public class SdmlReceiveTopic {

	// ロガー
	private static Log m_log = LogFactory.getLog(SdmlReceiveTopic.class);

	private static SdmlReceiveTopic instance = new SdmlReceiveTopic();

	private SdmlReceiveTopic() {
	}

	public static SdmlReceiveTopic getInstance() {
		return instance;
	}

	private boolean isSdmlSettingReload(SettingUpdateInfoResponse updateInfo,
			SettingUpdateInfoResponse settingLastUpdateInfo) {
		if (updateInfo == null) {
			return false;
		} else if (settingLastUpdateInfo == null) {
			return true;
		} else {
			if (settingLastUpdateInfo.getSdmlControlSettingUpdateTime()
					.equals(updateInfo.getSdmlControlSettingUpdateTime())
					&& settingLastUpdateInfo.getCalendarUpdateTime().equals(updateInfo.getCalendarUpdateTime())
					&& settingLastUpdateInfo.getRepositoryUpdateTime().equals(updateInfo.getRepositoryUpdateTime())) {
				return false;
			} else {
				return true;
			}
		}
	}

	public void reloadSdmlSetting(SettingUpdateInfoResponse updateInfo, SettingUpdateInfoResponse settingLastUpdateInfo,
			boolean force) throws RestConnectFailed {
		if (!isSdmlSettingReload(updateInfo, settingLastUpdateInfo) && !force) {
			return;
		}
		m_log.info("reloading configuration of sdml...");
		try {
			GetSdmlControlSettingForAgentResponse res = SdmlAgentRestClientWrapper
					.getSdmlControlSettingV1(Agent.getAgentInfoRequest());
			List<AgtSdmlControlSettingInfoResponse> list = res.getList();
			List<SdmlFileMonitorInfoWrapper> sdmlFileMonitorInfoList = SdmlFileMonitorInfoWrapper
					.createSdmlFileMonitorInfoList(list);
			SdmlFileMonitorManager.getInstance().pushMonitorInfoList(sdmlFileMonitorInfoList);
		} catch (InvalidSetting | HinemosUnknown e) {
			m_log.error(e, e);
		} catch (InvalidRole | InvalidUserPass | MonitorNotFound e) {
			m_log.warn("reloadSdmlSetting: " + e.getMessage());
		}
	}
}
