package com.clustercontrol.winevent.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.hub.bean.StringSample;
import com.clustercontrol.hub.util.CollectStringDataUtil;
import com.clustercontrol.jobmanagement.bean.MonitorJobEndNode;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.util.MonitorJobWorker;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.factory.FacilitySelector;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.winevent.bean.WinEventResultDTO;

public class RunMonitorWinEventString {
	
	public static final Log _log = LogFactory.getLog(RunMonitorWinEventString.class);

	private List<MonitorJobEndNode> monitorJobEndNodeList = new ArrayList<>();

	public void run(String facilityId, WinEventResultDTO result) throws HinemosUnknown {
		
		if (result.monitorInfo.getCollectorFlg() != null && result.monitorInfo.getCollectorFlg()) {
			StringSample sample = new StringSample(new Date(result.msgInfo.getGenerationDate()), result.monitorInfo.getMonitorId());
			sample.set(facilityId, "Windows Event Log", result.message);
			CollectStringDataUtil.store(Arrays.asList(sample));
		}
		
		if (result.monitorStrValueInfo == null)
			return;
		List<String> facilityIdList = null;
		if (result.runInstructionInfo == null) {
			// 監視ジョブ以外
			facilityIdList = FacilitySelector.getFacilityIdList(result.monitorInfo.getFacilityId(), result.monitorInfo.getOwnerRoleId(), 0, false, false);
		} else {
			// 監視ジョブ
			JobSessionJobEntity jobSessionJobEntity = null;
			try {
				jobSessionJobEntity = QueryUtil.getJobSessionJobPK(
						result.runInstructionInfo.getSessionId(),
						result.runInstructionInfo.getJobunitId(),
						result.runInstructionInfo.getJobId(), ObjectPrivilegeMode.NONE);
			} catch (InvalidRole | JobInfoNotFound e) {
				// 処理対象のジョブセッションジョブを取得できない場合は処理終了
				return;
			}
			facilityIdList = FacilitySelector.getFacilityIdList(
					result.runInstructionInfo.getFacilityId(),
					jobSessionJobEntity.getOwnerRoleId(), 0, false, false);
		}

		if (_log.isDebugEnabled()) {
			_log.debug(result.monitorInfo.getFacilityId() + " contains : " + facilityIdList);
		}
		if (! facilityIdList.contains(facilityId)) {
			_log.debug("facilityId is not contained " + facilityId + " in " + facilityIdList);
			return;
		}
		
		String origMessage = "pattern=" + result.monitorStrValueInfo.getPattern() + "\nlog.line=" + result.message;
		
		OutputBasicInfo output = new OutputBasicInfo();

		output.setMonitorId(result.monitorStrValueInfo.getMonitorId());
		output.setFacilityId(facilityId);
		output.setPluginId(HinemosModuleConstant.MONITOR_WINEVENT);
		output.setSubKey(result.monitorStrValueInfo.getPattern());
		
		if (FacilityTreeAttributeConstant.UNREGISTERED_SCOPE.equals(facilityId)) {
			output.setScopeText(result.msgInfo.getHostName());
		} else {
			String facilityPath = new RepositoryControllerBean().getFacilityPath(facilityId, null);
			output.setScopeText(facilityPath);
		}
		
		output.setApplication(result.monitorInfo.getApplication());
		
		if (result.monitorStrValueInfo.getMessage() != null) {
			String str = result.monitorStrValueInfo.getMessage().replace("#[LOG_LINE]", result.message);
			int maxLen = HinemosPropertyUtil.getHinemosPropertyNum("monitor.log.line.max.length", Long.valueOf(256)).intValue();
			if (str.length() > maxLen) {
				str = str.substring(0, maxLen);
			}
			output.setMessage(str);
		}

		output.setMessageOrg(origMessage);
		output.setPriority(result.monitorStrValueInfo.getPriority());
		output.setGenerationDate(result.msgInfo.getGenerationDate());
		output.setRunInstructionInfo(result.runInstructionInfo);
		
		output.setMultiId(HinemosPropertyUtil.getHinemosPropertyStr("monitor.systemlog.receiverid", System.getProperty("hinemos.manager.nodename")));
		
		if (result.runInstructionInfo == null) {
			// 監視ジョブ以外
			new NotifyControllerBean().notify(output, NotifyGroupIdGenerator.generate(result.monitorInfo));
		} else {
			// 監視ジョブ
			this.monitorJobEndNodeList.add(new MonitorJobEndNode(
					output.getRunInstructionInfo(),
					HinemosModuleConstant.MONITOR_WINEVENT,
					makeJobOrgMessage(result.monitorInfo, output.getMessageOrg()),
					"",
					RunStatusConstant.END,
					MonitorJobWorker.getReturnValue(output.getRunInstructionInfo(), output.getPriority())));
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
