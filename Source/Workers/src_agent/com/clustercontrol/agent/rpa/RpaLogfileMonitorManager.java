/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.rpa;

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

/**
 * RPAログ転送スレッドを管理するクラス<BR>
 * 
 * 転送対象ログファイル情報を受け取り、ログ転送スレッドを制御します。
 * 
 */
public class RpaLogfileMonitorManager extends AbstractFileMonitorManager<RpaMonitorInfoWrapper> {
	private static RpaLogfileMonitorManager instance = new RpaLogfileMonitorManager(RpaLogfileMonitorConfig.getInstance());

	public static RpaLogfileMonitorManager getInstance() {
		return instance;
	}
	protected RpaLogfileMonitorManager(FileMonitorConfig fileMonitorConfig) {
		super(fileMonitorConfig);
	}

	@Override
	protected void sendMessageInner(String filePath, int priority, String app, String msg, String msgOrg, String monitorId, AgtRunInstructionInfoRequest runInstructionInfo) {
		// ログ出力情報
		MessageSendableObject sendme = new MessageSendableObject();
		sendme.body = new AgtOutputBasicInfoRequest();
		sendme.body.setPluginId(HinemosModuleConstant.MONITOR_RPA_LOGFILE);
		sendme.body.setPriority(priority);
		sendme.body.setApplication(app);
		sendme.body.setMessage(msg);
		sendme.body.setMessageOrg(msgOrg);

		sendme.body.setGenerationDate(HinemosTime.getDateInstance().getTime());
		sendme.body.setMonitorId(monitorId);
		sendme.body.setFacilityId(""); // マネージャがセットする。
		sendme.body.setScopeText(""); // マネージャがセットする。

		sendQueue.put(sendme);
	}

	@Override
	public String getReadingStatusStorePath() {
		String home = Agent.getAgentHome();
		String storepath = new File(new File(home), "readingstatus").getAbsolutePath();
		return storepath;
	}

	@Override
	protected void sendMessageInner(String filePath, int priority, String app, String msg, String msgOrg, RpaMonitorInfoWrapper fileMonitorInfoWrapper) {
		sendMessageInner(filePath, priority, app, msg, msgOrg, fileMonitorInfoWrapper.getId(), null);		
	}

	@Override
	public AbstractFileMonitor<RpaMonitorInfoWrapper> createFileMonitor(RpaMonitorInfoWrapper monitorInfo, AbstractReadingStatus<RpaMonitorInfoWrapper> status, FileMonitorConfig fileMonitorConfig) {
		return new RpaLogfileMonitor(this, monitorInfo, status, fileMonitorConfig);
	}

	@Override
	public AbstractReadingStatusRoot<RpaMonitorInfoWrapper> createReadingStatusRoot(List<RpaMonitorInfoWrapper> miList, String baseDirectory, FileMonitorConfig fileMonitorConfig) {
		return new ReadingStatusRoot(this, "rstatus.json", "rs_", "monitor_type", "rpa_logfile", miList, baseDirectory,
				fileMonitorConfig);
	}
}
