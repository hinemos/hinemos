/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.snmptrap.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.jobmanagement.bean.JobLinkMessageId;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.jobmanagement.util.MonitorJobWorker;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.bean.NotifyTriggerType;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.snmptrap.bean.SnmpTrap;
import com.clustercontrol.snmptrap.bean.TrapId;

/**
 * snmptrap監視の通知実装のインタフェース(シングル版)
 */
public class SnmpTrapNotifier {
	private static final Log log = LogFactory.getLog(SnmpTrapNotifier.class);
	
	public static String replaceUnsafeCharacter(String str) {

		String charList = HinemosPropertyCommon.notify_replace_before.getStringValue();
		char newChar = HinemosPropertyCommon.notify_replace_after.getStringValue().charAt(0);

		for (String oldCharByte : charList.split(",")) {
			int oldCharInt = Integer.parseInt(oldCharByte);
			Character oldChar = (char)oldCharInt;
			if (str != null) {
				str = str.replace(oldChar, newChar);
			}
		}

		return str;
	}

	public List<OutputBasicInfo> createOutputBasicInfoList(List<SnmpTrap> receivedTrapList,
			List<MonitorInfo> monitorList,
			List<Integer> priorityList, 
			List<String> facilityIdList,
			List<String[]> msgsList,
			List<RunInstructionInfo> runInstructionList) {

		List<OutputBasicInfo> rtn = new ArrayList<>();

		Map<String, String> facilityPathMap = new HashMap<String, String>();
		for (int i = 0; i < receivedTrapList.size(); i++) {
			SnmpTrap receivedTrap = receivedTrapList.get(i);
			MonitorInfo monitor = monitorList.get(i);
			int priority = priorityList.get(i);
			String facilityId = facilityIdList.get(i);
			String[] msgs = msgsList.get(i);

			String facilityPath = facilityPathMap.get(facilityId);
			if (facilityPath == null) {
				facilityPath = getFacilityPath(facilityId, receivedTrap);
				if (facilityPath == null) {
					continue;
				}
				facilityPathMap.put(facilityId, facilityPath);
			}
			if (runInstructionList == null) {
				// 監視ジョブ以外
				OutputBasicInfo output = new OutputBasicInfo();

				output.setNotifyGroupId(monitor.getNotifyGroupId());
				output.setJoblinkMessageId(JobLinkMessageId.getId(NotifyTriggerType.MONITOR,
						HinemosModuleConstant.MONITOR_SNMPTRAP, monitor.getMonitorId()));
				output.setMonitorId(monitor.getMonitorId());
				output.setPluginId(HinemosModuleConstant.MONITOR_SNMPTRAP);

				TrapId trapV1 = receivedTrap.getTrapId().asTrapV1Id();
				output.setSubKey(trapV1.getEnterpriseId() + "_" + trapV1.getGenericId() + "_" + trapV1.getSpecificId());

				output.setPriority(priority);
				output.setApplication(monitor.getApplication());
				output.setFacilityId(facilityId);
				output.setScopeText(facilityPath);
				output.setGenerationDate(receivedTrap.getReceivedTime());

				String msg = replaceUnsafeCharacter(msgs[0]);
				String msgOrig = replaceUnsafeCharacter(msgs[1]);
				
				int messageMaxLength = HinemosPropertyCommon.monitor_snmptrap_line_max_length.getIntegerValue();

				if (messageMaxLength >= 0 && msg.length() > messageMaxLength) {
					output.setMessage(msg.substring(0, messageMaxLength));
				} else {
					output.setMessage(msg);
				}

				output.setMessageOrg(msgOrig);
				rtn.add(output);
			} else {
				// 監視ジョブ
				MonitorJobWorker.endMonitorJob(
						runInstructionList.get(i),
						HinemosModuleConstant.MONITOR_SNMPTRAP,
						replaceUnsafeCharacter(msgs[1]),
						"",
						RunStatusConstant.END,
						MonitorJobWorker.getReturnValue(runInstructionList.get(i), priority));
			}
		}
		return rtn;
	}

	private String getFacilityPath(String facilityId, SnmpTrap receivedTrap) {
		if (FacilityTreeAttributeConstant.UNREGISTERED_SCOPE.equals(facilityId)) {
			return receivedTrap.getAgentAddr();
		}
		else {
			try {
				return new RepositoryControllerBean().getFacilityPath(facilityId, null);
			} catch (HinemosUnknown e) {
				log.error(e);
			}
			
			return null;
		}
	}
}