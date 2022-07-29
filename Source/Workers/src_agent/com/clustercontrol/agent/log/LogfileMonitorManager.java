/*
 * Copyright (c) 2021 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.log;

import java.io.File;
import java.util.List;

import org.openapitools.client.model.AgtOutputBasicInfoRequest;
import org.openapitools.client.model.AgtRunInstructionInfoRequest;

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.SendQueue.MessageSendableObject;
import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitor;
import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitorManager;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatus;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatusRoot;
import com.clustercontrol.agent.util.filemonitor.FileMonitorConfig;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.util.HinemosTime;

public class LogfileMonitorManager extends AbstractFileMonitorManager<MonitorInfoWrapper> {
	private static LogfileMonitorManager instance = new LogfileMonitorManager(LogfileMonitorConfig.getInstance());

	public static LogfileMonitorManager getInstance() {
		return instance;
	}

	private LogfileMonitorManager(FileMonitorConfig fileMonitorConfig) {
		super(fileMonitorConfig);
	}

	@Override
	public String getReadingStatusStorePath() {
		String home = Agent.getAgentHome();
		String storepath = new File(new File(home), "readingstatus").getAbsolutePath();
		return storepath;
	}

	@Override
	public AbstractFileMonitor<MonitorInfoWrapper> createFileMonitor(MonitorInfoWrapper monitorInfo,
			AbstractReadingStatus<MonitorInfoWrapper> status, FileMonitorConfig fileMonitorConfig) {
		return new LogfileMonitor(this, monitorInfo, status, fileMonitorConfig);
	}

	@Override
	public AbstractReadingStatusRoot<MonitorInfoWrapper> createReadingStatusRoot(List<MonitorInfoWrapper> miList,
			String baseDirectory, FileMonitorConfig fileMonitorConfig) {
		return new ReadingStatusRoot(this, "rstatus.json", "rs_", "monitor_type", "logfile", miList, baseDirectory,
				fileMonitorConfig);
	}

	@Override
	public void sendMessageInner(String filePath, int priority, String app, String msg, String msgOrg, String monitorId,
			AgtRunInstructionInfoRequest runInstructionInfo) {
		// ログ出力情報
		MessageSendableObject sendme = new MessageSendableObject();
		sendme.body = new AgtOutputBasicInfoRequest();
		sendme.body.setPluginId(HinemosModuleConstant.MONITOR_LOGFILE);
		sendme.body.setPriority(priority);
		sendme.body.setApplication(app);
		sendme.body.setMessage(msg);
		sendme.body.setMessageOrg(msgOrg);

		sendme.body.setGenerationDate(HinemosTime.getDateInstance().getTime());
		sendme.body.setMonitorId(monitorId);
		sendme.body.setFacilityId(""); // マネージャがセットする。
		sendme.body.setScopeText(""); // マネージャがセットする。
		sendme.body.setRunInstructionInfo(runInstructionInfo);

		sendQueue.put(sendme);
	}

	@Override
	public void sendMessageInner(String filePath, int priority, String app, String msg, String msgOrg, MonitorInfoWrapper monitorInfoWrapper) {
		sendMessageInner(filePath, priority, app, msg, msgOrg, monitorInfoWrapper.getId(), monitorInfoWrapper.getRunInstructionInfoReq());
	}

}
