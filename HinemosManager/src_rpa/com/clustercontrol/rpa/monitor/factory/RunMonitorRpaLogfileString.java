/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.monitor.factory;

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
import com.clustercontrol.hub.bean.CollectStringTag;
import com.clustercontrol.hub.bean.StringSample;
import com.clustercontrol.hub.bean.StringSampleTag;
import com.clustercontrol.hub.util.CollectStringDataUtil;
import com.clustercontrol.logfile.bean.LogfileResultDTO;
import com.clustercontrol.logfile.factory.RunMonitorLogfileString;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.XMLUtil;

/**
 * @see {@link RunMonitorLogfileString}
 */
public class RunMonitorRpaLogfileString {
	
	public static final Log _log = LogFactory.getLog(RunMonitorRpaLogfileString.class);

	public List<OutputBasicInfo> run(String facilityId, LogfileResultDTO result) throws HinemosUnknown {
		List<OutputBasicInfo> rtn = new ArrayList<>();
		
		// 収集処理.
		if (result.monitorInfo.getCollectorFlg() != null && result.monitorInfo.getCollectorFlg()) {
			StringSample sample = new StringSample(new Date(result.msgInfo.getGenerationDate()), result.monitorInfo.getMonitorId());
			
			String filePath = new File(new File(result.monitorInfo.getRpaLogFileCheckInfo().getDirectory()), result.monitorInfo.getRpaLogFileCheckInfo().getFileName()).getPath();
			sample.set(facilityId, filePath, XMLUtil.ignoreInvalidString(result.message.trim()), Arrays.asList(new StringSampleTag(CollectStringTag.filename, result.monitorInfo.getRpaLogFileCheckInfo().getFileName())));
			CollectStringDataUtil.store(Arrays.asList(sample));
		}
		
		if (result.monitorStrValueInfo == null)
			return rtn;
		
		String origMessage = MessageConstant.LOGFILE_FILENAME.getMessage() + "=" + result.monitorInfo.getRpaLogFileCheckInfo().getFileName() + "\n"
				+ MessageConstant.LOGFILE_PATTERN.getMessage() + "=" + result.monitorStrValueInfo.getPattern() + "\n" 
				+ MessageConstant.LOGFILE_LINE.getMessage() + "=" + result.message.trim();
		
		OutputBasicInfo output = new OutputBasicInfo();
		output.setNotifyGroupId(NotifyGroupIdGenerator.generate(result.monitorInfo));
		output.setMonitorId(result.monitorStrValueInfo.getMonitorId());
		output.setFacilityId(facilityId);
		output.setPluginId(HinemosModuleConstant.MONITOR_RPA_LOGFILE);
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
		
		output.setPriorityChangeJudgmentType(result.monitorInfo.getPriorityChangeJudgmentType());
		output.setPriorityChangeFailureType(result.monitorInfo.getPriorityChangeFailureType());


		// RPAログファイル監視は監視ジョブに対応しない
		rtn.add(output);

		return rtn;
	}
}
