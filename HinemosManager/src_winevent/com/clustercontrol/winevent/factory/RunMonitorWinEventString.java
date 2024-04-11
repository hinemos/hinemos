/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.winevent.factory;

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
import com.clustercontrol.hub.bean.StringSample;
import com.clustercontrol.hub.util.CollectStringDataUtil;
import com.clustercontrol.jobmanagement.bean.JobLinkMessageId;
import com.clustercontrol.jobmanagement.bean.MonitorJobEndNode;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.jobmanagement.util.MonitorJobWorker;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.bean.NotifyTriggerType;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.winevent.bean.WinEventResultDTO;

public class RunMonitorWinEventString {
	
	public static final Log _log = LogFactory.getLog(RunMonitorWinEventString.class);

	private List<MonitorJobEndNode> monitorJobEndNodeList = new ArrayList<>();

	public OutputBasicInfo run(String facilityId, WinEventResultDTO result) throws HinemosUnknown {

		OutputBasicInfo rtn = null;

		if (result.runInstructionInfo == null && result.monitorInfo.getCollectorFlg() != null && result.monitorInfo.getCollectorFlg()) {
			StringSample sample = new StringSample(new Date(result.msgInfo.getGenerationDate()), result.monitorInfo.getMonitorId());
			sample.set(facilityId, "Windows Event Log", result.message);
			CollectStringDataUtil.store(Arrays.asList(sample));
		}
		
		if (result.monitorStrValueInfo == null)
			return rtn;
		
		String origMessage = "pattern=" + result.monitorStrValueInfo.getPattern() + "\nlog.line=" + result.message;
		
		rtn = new OutputBasicInfo();

		rtn.setNotifyGroupId(NotifyGroupIdGenerator.generate(result.monitorInfo));
		rtn.setJoblinkMessageId(JobLinkMessageId.getId(NotifyTriggerType.MONITOR, HinemosModuleConstant.MONITOR_WINEVENT,
				result.monitorStrValueInfo.getMonitorId()));
		rtn.setMonitorId(result.monitorStrValueInfo.getMonitorId());
		rtn.setFacilityId(facilityId);
		rtn.setPluginId(HinemosModuleConstant.MONITOR_WINEVENT);
		rtn.setSubKey(result.monitorStrValueInfo.getPattern());
		
		if (FacilityTreeAttributeConstant.UNREGISTERED_SCOPE.equals(facilityId)) {
			rtn.setScopeText(result.msgInfo.getHostName());
		} else {
			String facilityPath = new RepositoryControllerBean().getFacilityPath(facilityId, null);
			rtn.setScopeText(facilityPath);
		}
		
		rtn.setApplication(result.monitorInfo.getApplication());
		
		if (result.monitorStrValueInfo.getMessage() != null) {
			String str = result.monitorStrValueInfo.getMessage().replace("#[LOG_LINE]", result.message);
			int maxLen = HinemosPropertyCommon.monitor_log_line_max_length.getIntegerValue();
			if (str.length() > maxLen) {
				str = str.substring(0, maxLen);
			}
			rtn.setMessage(str);
		}

		rtn.setMessageOrg(origMessage);
		rtn.setPriority(result.monitorStrValueInfo.getPriority());
		rtn.setGenerationDate(result.msgInfo.getGenerationDate());
		rtn.setRunInstructionInfo(result.runInstructionInfo);
		
		rtn.setMultiId(HinemosPropertyCommon.monitor_systemlog_receiverid.getStringValue());
		
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
					rtn.setPriorityChangeJudgmentType(monitor.getPriorityChangeJudgmentType());
					rtn.setPriorityChangeFailureType(monitor.getPriorityChangeFailureType());
				} catch (MonitorNotFound | InvalidRole e) {
					// レコードないなら設定不要
				}
			}
		} else {
			if(_log.isTraceEnabled()){
				_log.trace("run() : PriorityChangeJudgmentType or getPriorityChangeFailureType of result is not null ");
			}
			rtn.setPriorityChangeJudgmentType(result.monitorInfo.getPriorityChangeJudgmentType());
			rtn.setPriorityChangeFailureType(result.monitorInfo.getPriorityChangeFailureType());
		}

		if (result.runInstructionInfo == null) {
			// 監視ジョブ以外
			return rtn;
		} else {
			// 監視ジョブ
			this.monitorJobEndNodeList.add(new MonitorJobEndNode(
					rtn.getRunInstructionInfo(),
					HinemosModuleConstant.MONITOR_WINEVENT,
					makeJobOrgMessage(result.monitorInfo, rtn.getMessageOrg()),
					"",
					RunStatusConstant.END,
					MonitorJobWorker.getReturnValue(rtn.getRunInstructionInfo(), rtn.getPriority())));
			return null;
		}
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
		if (monitorInfo == null || monitorInfo.getWinEventCheckInfo() == null) {
			return "";
		}
		List<String> labelList = new ArrayList<>();
		if (monitorInfo.getWinEventCheckInfo().isLevelCritical())
			labelList.add(MessageConstant.MONITORJOB_CRITICAL.getMessage());
		if (monitorInfo.getWinEventCheckInfo().isLevelWarning())
			labelList.add(MessageConstant.MONITORJOB_WARNING.getMessage());
		if (monitorInfo.getWinEventCheckInfo().isLevelVerbose())
			labelList.add(MessageConstant.MONITORJOB_VERBOSE.getMessage());
		if (monitorInfo.getWinEventCheckInfo().isLevelError())
			labelList.add(MessageConstant.MONITORJOB_ERROR.getMessage());
		if (monitorInfo.getWinEventCheckInfo().isLevelInformational())
			labelList.add(MessageConstant.MONITORJOB_INFORMATION.getMessage());
		String[] args = {
			getListString(labelList),
			getListString(monitorInfo.getWinEventCheckInfo().getLogName()),
			getListString(monitorInfo.getWinEventCheckInfo().getSource()),
			getListString(monitorInfo.getWinEventCheckInfo().getEventId()),
			getListString(monitorInfo.getWinEventCheckInfo().getCategory()),
			getListString(monitorInfo.getWinEventCheckInfo().getKeywords())};
		return MessageConstant.MESSAGE_JOB_MONITOR_ORGMSG_WINEVENT.getMessage(args)
				+ "\n" + orgMsg;
	}

	private String getListString(List<?> list) {
		return list != null && list.size() > 0 ? list.toString(): "";
	}
}
