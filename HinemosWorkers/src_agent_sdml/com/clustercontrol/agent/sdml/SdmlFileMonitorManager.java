/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.sdml;

import java.io.File;
import java.util.List;

import org.openapitools.client.model.AgtOutputBasicInfoRequest;
import org.openapitools.client.model.AgtRunInstructionInfoRequest;
import org.openapitools.client.model.SendSdmlMessageRequest;

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitor;
import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitorManager;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatus;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatusRoot;
import com.clustercontrol.agent.util.filemonitor.FileMonitorConfig;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.util.HinemosTime;

public class SdmlFileMonitorManager extends AbstractFileMonitorManager<SdmlFileMonitorInfoWrapper> {
	private static SdmlFileMonitorManager instance = new SdmlFileMonitorManager(SdmlFileMonitorConfig.getInstance());

	public static SdmlFileMonitorManager getInstance() {
		return instance;
	}

	protected SdmlFileMonitorManager(FileMonitorConfig fileMonitorConfig) {
		super(fileMonitorConfig);
	}

	@Override
	protected void sendMessageInner(String filePath, int priority, String app, String msg, String msgOrg,
			String monitorId, AgtRunInstructionInfoRequest runInstructionInfo) {
		// ログ出力情報
		SendSdmlMessageRequest sendSdmlMessageRequest = new SendSdmlMessageRequest();
		sendSdmlMessageRequest.setAgentInfo(Agent.getAgentInfoRequest());

		AgtOutputBasicInfoRequest agtOutputBasicInfoRequest = new AgtOutputBasicInfoRequest();
		agtOutputBasicInfoRequest.setPluginId(HinemosModuleConstant.SDML_CONTROL);
		agtOutputBasicInfoRequest.setPriority(priority);
		agtOutputBasicInfoRequest.setApplication(app);
		agtOutputBasicInfoRequest.setMessage(msg);
		agtOutputBasicInfoRequest.setMessageOrg(msgOrg);
		agtOutputBasicInfoRequest.setGenerationDate(HinemosTime.getDateInstance().getTime());
		agtOutputBasicInfoRequest.setMonitorId(monitorId);
		agtOutputBasicInfoRequest.setFacilityId(""); // マネージャがセットする。
		agtOutputBasicInfoRequest.setScopeText(""); // マネージャがセットする。
		sendSdmlMessageRequest.setOutputBasicInfo(agtOutputBasicInfoRequest);

		SdmlMessageSendableObject smso = new SdmlMessageSendableObject(sendSdmlMessageRequest);

		sendQueue.put(smso);
	}

	@Override
	protected void sendMessageInner(String filePath, int priority, String app, String msg, String msgOrg,
			SdmlFileMonitorInfoWrapper fileMonitorInfoWrapper) {
		sendMessageInner(filePath, priority, app, msg, msgOrg, fileMonitorInfoWrapper.getId(), null);
	}

	@Override
	public String getReadingStatusStorePath() {
		String home = Agent.getAgentHome();
		String storepath = new File(new File(home), "sdml_readingstatus").getAbsolutePath();
		return storepath;
	}

	@Override
	public AbstractFileMonitor<SdmlFileMonitorInfoWrapper> createFileMonitor(SdmlFileMonitorInfoWrapper monitorInfo,
			AbstractReadingStatus<SdmlFileMonitorInfoWrapper> status, FileMonitorConfig fileMonitorConfig) {
		return new SdmlFileMonitor(this, monitorInfo, status, fileMonitorConfig);
	}

	@Override
	public AbstractReadingStatusRoot<SdmlFileMonitorInfoWrapper> createReadingStatusRoot(
			List<SdmlFileMonitorInfoWrapper> miList, String baseDirectory, FileMonitorConfig fileMonitorConfig) {
		return new SdmlReadingStatusRoot(this, "rstatus.json", "rs_", "monitor_type", "sdmlctl", miList, baseDirectory, fileMonitorConfig);
	}

}
