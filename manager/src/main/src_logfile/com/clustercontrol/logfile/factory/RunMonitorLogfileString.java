package com.clustercontrol.logfile.factory;

import java.io.File;
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
import com.clustercontrol.hub.bean.StringSampleTag;
import com.clustercontrol.hub.bean.ValueType;
import com.clustercontrol.hub.util.CollectStringDataUtil;
import com.clustercontrol.jobmanagement.bean.MonitorJobEndNode;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.util.MonitorJobWorker;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.logfile.bean.LogfileResultDTO;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.factory.FacilitySelector;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.MessageConstant;

public class RunMonitorLogfileString {
	
	public static final Log _log = LogFactory.getLog(RunMonitorLogfileString.class);
	private List<MonitorJobEndNode> monitorJobEndNodeList = new ArrayList<>();

	public void run(String facilityId, LogfileResultDTO result) throws HinemosUnknown {
		if (result.monitorInfo.getCollectorFlg() != null && result.monitorInfo.getCollectorFlg()) {
			StringSample sample = new StringSample(new Date(result.msgInfo.getGenerationDate()), result.monitorInfo.getMonitorId());
			
			String filePath = new File(new File(result.monitorInfo.getLogfileCheckInfo().getDirectory()), result.monitorInfo.getLogfileCheckInfo().getFileName()).getPath();
			sample.set(facilityId, filePath, result.message.trim());
			
			StringSampleTag tag = new StringSampleTag("filename", ValueType.string, result.monitorInfo.getLogfileCheckInfo().getLogfile());
			sample.getTagList().add(tag);
			
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
		
		String origMessage = MessageConstant.LOGFILE_FILENAME.getMessage() + "=" + result.monitorInfo.getLogfileCheckInfo().getLogfile() + "\n"
				+ MessageConstant.LOGFILE_PATTERN.getMessage() + "=" + result.monitorStrValueInfo.getPattern() + "\n" 
				+ MessageConstant.LOGFILE_LINE.getMessage() + "=" + result.message.trim();
		
		OutputBasicInfo output = new OutputBasicInfo();

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
					HinemosModuleConstant.MONITOR_LOGFILE,
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
