/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.logfile.factory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.hinemosagent.util.AgentVersionManager;
import com.clustercontrol.hub.bean.CollectStringTag;
import com.clustercontrol.hub.bean.StringSample;
import com.clustercontrol.hub.bean.StringSampleTag;
import com.clustercontrol.hub.util.CollectStringDataUtil;
import com.clustercontrol.jobmanagement.bean.JobLinkMessageId;
import com.clustercontrol.jobmanagement.bean.MonitorJobEndNode;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.jobmanagement.util.MonitorJobWorker;
import com.clustercontrol.logfile.bean.LogfileResultDTO;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.bean.NotifyTriggerType;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.sdml.util.SdmlUtil;
import com.clustercontrol.util.MessageConstant;

public class RunMonitorLogfileString {
	
	public static final Log _log = LogFactory.getLog(RunMonitorLogfileString.class);
	private List<MonitorJobEndNode> monitorJobEndNodeList = new ArrayList<>();

	public List<OutputBasicInfo> run(String facilityId, LogfileResultDTO result) throws HinemosUnknown {
		List<OutputBasicInfo> rtn = new ArrayList<>();
		
		// 監視ジョブ以外の場合、収集処理.
		if (result.runInstructionInfo == null && result.monitorInfo.getCollectorFlg() != null && result.monitorInfo.getCollectorFlg()) {
			StringSample sample = new StringSample(new Date(result.msgInfo.getGenerationDate()), result.monitorInfo.getMonitorId());
			
			String filePath = new File(new File(result.monitorInfo.getLogfileCheckInfo().getDirectory()), result.monitorInfo.getLogfileCheckInfo().getFileName()).getPath();
			
			String messageWithoutBom = result.message;
			if (result.message.startsWith("\uFEFF")) {
				// ログにBOMが含まれる場合は除去する
				messageWithoutBom = result.message.substring(1);
			}
			
			if (SdmlUtil.isCreatedBySdml(result.monitorInfo)) {
				// SDMLで自動作成されたログファイル監視の場合、独自のタグを抽出する
				List<StringSampleTag> tagList = SdmlUtil.extractTagsFromMonitoringLog(result.monitorInfo.getMonitorId(), result.message);
				tagList.add(new StringSampleTag(CollectStringTag.filename, result.monitorInfo.getLogfileCheckInfo().getLogfile()));
				sample.set(facilityId, filePath, messageWithoutBom.trim(), tagList);
			} else {
				sample.set(facilityId, filePath, messageWithoutBom.trim(), Arrays.asList(new StringSampleTag(CollectStringTag.filename, result.monitorInfo.getLogfileCheckInfo().getLogfile())));
			}
			CollectStringDataUtil.store(Arrays.asList(sample));
		}
		
		if (result.monitorStrValueInfo == null)
			return rtn;
		
		String origMessage = MessageConstant.LOGFILE_FILENAME.getMessage() + "=" + result.monitorInfo.getLogfileCheckInfo().getLogfile() + "\n"
				+ MessageConstant.LOGFILE_PATTERN.getMessage() + "=" + result.monitorStrValueInfo.getPattern() + "\n" 
				+ MessageConstant.LOGFILE_LINE.getMessage() + "=" + result.message.trim();
		
		OutputBasicInfo output = new OutputBasicInfo();
		output.setNotifyGroupId(NotifyGroupIdGenerator.generate(result.monitorInfo));
		output.setJoblinkMessageId(JobLinkMessageId.getId(NotifyTriggerType.MONITOR, HinemosModuleConstant.MONITOR_LOGFILE,
				result.monitorStrValueInfo.getMonitorId()));
		output.setMonitorId(result.monitorStrValueInfo.getMonitorId());
		output.setFacilityId(facilityId);
		output.setPluginId(HinemosModuleConstant.MONITOR_LOGFILE);
		output.setSubKey(result.monitorStrValueInfo.getPattern());
		
		if (FacilityTreeAttributeConstant.UNREGISTERED_SCOPE.equals(facilityId)) {
			output.setScopeText(result.msgInfo.getHostName());
		} else {
			String facilityPath = new RepositoryControllerBean().getFacilityPath(facilityId, null);
			output.setScopeText(facilityPath);
		}
		
		output.setApplication(result.monitorInfo.getApplication());
		
		if (result.monitorStrValueInfo.getMessage() != null) {
			String str = result.monitorStrValueInfo.getMessage().replace("#[LOG_LINE]", result.message.trim());
			int maxLen = HinemosPropertyCommon.monitor_log_line_max_length.getIntegerValue();
			if (str.length() > maxLen) {
				str = str.substring(0, maxLen);
			}
			output.setMessage(str);
		}

		output.setMessageOrg(origMessage);
		output.setPriority(result.monitorStrValueInfo.getPriority());
		output.setGenerationDate(result.msgInfo.getGenerationDate());
		output.setRunInstructionInfo(result.runInstructionInfo);
		
		output.setMultiId(HinemosPropertyCommon.monitor_systemlog_receiverid.getStringValue());

		if (result.monitorInfo.getPriorityChangeJudgmentType() == null
				&& result.monitorInfo.getPriorityChangeFailureType() == null) {
			// 重要度変化関連の設定は、監視対象がWS接続（非REST）エージェントの場合
			// エージェント送信値から取得できないのでマネージャ側の設定値を参照する
			if (!(AgentVersionManager.isRestConnetctAgent(facilityId))) {
				try {
					if(_log.isTraceEnabled()){
						_log.trace("run() : isRestConnetctAgent(" + facilityId + ") = false");
					}
					MonitorInfo monitor = new MonitorInfo();
					monitor = new MonitorSettingControllerBean().getMonitor(result.monitorInfo.getMonitorId());
					output.setPriorityChangeJudgmentType(monitor.getPriorityChangeJudgmentType());
					output.setPriorityChangeFailureType(monitor.getPriorityChangeFailureType());
				} catch (MonitorNotFound | InvalidRole e) {
					// レコードないなら設定不要
				}
			}
		} else {
			if(_log.isTraceEnabled()){
				_log.trace("run() : PriorityChangeJudgmentType or getPriorityChangeFailureType of result is not null ");
			}
			output.setPriorityChangeJudgmentType(result.monitorInfo.getPriorityChangeJudgmentType());
			output.setPriorityChangeFailureType(result.monitorInfo.getPriorityChangeFailureType());
		}

		if (result.runInstructionInfo == null) {
			// 監視ジョブ以外
			rtn.add(output);
		} else {
			// 監視ジョブ
			this.monitorJobEndNodeList.add(new MonitorJobEndNode(
					output.getRunInstructionInfo(),
					HinemosModuleConstant.MONITOR_LOGFILE,
					makeJobOrgMessage(result.monitorInfo, output.getMessageOrg()),
					"",
					RunStatusConstant.END,
					MonitorJobWorker.getReturnValue(output.getRunInstructionInfo(), output.getPriority())));
		}
		return rtn;
	}

	/**
	 * 監視ジョブの実行結果を返す
	 * 
	 * @return 監視ジョブの実行結果
	 */
	public List<MonitorJobEndNode> getMonitorJobEndNodeList() {
		return this.monitorJobEndNodeList;
	}

	private String makeJobOrgMessage(MonitorInfo monitorInfo, String orgMsg) {
		if (monitorInfo == null || monitorInfo.getLogfileCheckInfo() == null) {
			return "";
		}
		String[] args = {
				monitorInfo.getLogfileCheckInfo().getDirectory(),
				monitorInfo.getLogfileCheckInfo().getFileName(),
				monitorInfo.getLogfileCheckInfo().getFileEncoding()};
		return MessageConstant.MESSAGE_JOB_MONITOR_ORGMSG_LOGFILE.getMessage(args)
				+ "\n" + orgMsg;
	}
}
