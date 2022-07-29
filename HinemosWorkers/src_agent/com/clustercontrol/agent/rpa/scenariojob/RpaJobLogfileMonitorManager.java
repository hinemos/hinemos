/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.rpa.scenariojob;

import java.io.File;
import java.util.List;

import org.openapitools.client.model.AgtOutputBasicInfoRequest;
import org.openapitools.client.model.AgtRunInstructionInfoRequest;

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.SendQueue.MessageSendableObject;
import com.clustercontrol.agent.log.MonitorInfoWrapper;
import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitorManager;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatus;
import com.clustercontrol.agent.util.filemonitor.FileMonitorConfig;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.util.HinemosTime;

/**
 * RPAシナリオジョブ用ログファイル監視スレッドを管理するクラス<BR>
 */
public class RpaJobLogfileMonitorManager extends AbstractFileMonitorManager<MonitorInfoWrapper> {
	private static RpaJobLogfileMonitorManager instance = new RpaJobLogfileMonitorManager(RpaJobLogfileMonitorConfig.getInstance());

	public static RpaJobLogfileMonitorManager getInstance() {
		return instance;
	}

	private RpaJobLogfileMonitorManager(FileMonitorConfig fileMonitorConfig) {
		super(fileMonitorConfig);
	}

	@Override
	public String getReadingStatusStorePath() {
		String home = Agent.getAgentHome();
		String storepath = new File(new File(home), "readingstatus").getAbsolutePath();
		return storepath;
	}

	@Override
	public ScenarioLogfileMonitor createFileMonitor(MonitorInfoWrapper monitorInfo,
			AbstractReadingStatus<MonitorInfoWrapper> status, FileMonitorConfig fileMonitorConfig) {
		return new ScenarioLogfileMonitor(this, monitorInfo, status, fileMonitorConfig);
	}

	@Override
	public ScenarioLogReadingStatusRoot createReadingStatusRoot(List<MonitorInfoWrapper> miList,
			String baseDirectory, FileMonitorConfig fileMonitorConfig) {
		return new ScenarioLogReadingStatusRoot(this, "rstatus.json", "rs_", "monitor_type", "logfile", miList, baseDirectory,
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
		// RPAシナリオジョブ用ログファイル監視ではマネージャへの送信は行わないため呼ばれることは無い
		//sendMessageInner(filePath, priority, app, msg, msgOrg, monitorInfoWrapper.getId(), monitorInfoWrapper.getRunInstructionInfoReq());
	}

}
