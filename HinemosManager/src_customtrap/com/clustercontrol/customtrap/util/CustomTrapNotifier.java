/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.customtrap.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.customtrap.bean.CustomTrap;
import com.clustercontrol.jobmanagement.bean.JobLinkMessageId;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.jobmanagement.util.MonitorJobWorker;
import com.clustercontrol.monitor.bean.ConvertValueConstant;
import com.clustercontrol.monitor.run.bean.MonitorRunResultInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfo;
import com.clustercontrol.notify.bean.NotifyTriggerType;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.MessageConstant;

/**
 * カスタムトラップ監視通知<br/>
 * 
 * @version 6.0.0
 * @since 6.0.0
 */
public class CustomTrapNotifier {
	/** ログ出力のインスタンス */
	private final static Log m_log = LogFactory.getLog(CustomTrapNotifier.class);

	/**
	 * カスタムトラップ監視（文字列）を通知します。
	 * 
	 * @param customtrapList	受信データリスト
	 * @param monitorInfo		監視情報
	 * @param priorityBuffer	プライオリティリスト
	 * @param ruleList			ルールリスト
	 * @param facilityIdList	ファシリティリスト
	 * @param agentAddr			Agentアドレス
	 * @return 通知情報リスト
	 */
	public List<OutputBasicInfo> createStringOutputBasicInfoList(
			List<CustomTrap> customtrapList, MonitorInfo monitorInfo, List<Integer> priorityBuffer,
			List<MonitorStringValueInfo> monitorStringValueInfoList, List<String> facilityIdList,String agentAddr,
			RunInstructionInfo runInstructionInfo) {
		List<OutputBasicInfo> rtn = new ArrayList<>();
		for (int i = 0; i < customtrapList.size(); i++) {
			CustomTrap customTrap = customtrapList.get(i);
			MonitorStringValueInfo monitorStringValueInfo = monitorStringValueInfoList.get(i);
			String facilityId = facilityIdList.get(i);
			int priority = priorityBuffer.get(i);

			OutputBasicInfo output = new OutputBasicInfo();
			output.setNotifyGroupId(NotifyGroupIdGenerator.generate(monitorInfo));
			output.setJoblinkMessageId(JobLinkMessageId.getId(NotifyTriggerType.MONITOR,
					HinemosModuleConstant.MONITOR_CUSTOMTRAP_S, monitorStringValueInfo.getMonitorId()));
			output.setMonitorId(monitorStringValueInfo.getMonitorId());
			output.setPluginId(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S);
			output.setFacilityId(facilityId);
	
	
			// 通知抑制を監視項目の「パターンマッチ表現」単位にするため、通知抑制用サブキーを設定する。
			output.setSubKey(monitorStringValueInfo.getPattern());
	
			if (FacilityTreeAttributeConstant.UNREGISTERED_SCOPE.equals(facilityId)) {
				// 未登録ノードの場合には送信元IPを表示する。
				output.setScopeText(agentAddr);
			} else {
				// ファシリティのパスを設定する]
				try {
					String facilityPath = new RepositoryControllerBean().getFacilityPath(facilityId, null);
					output.setScopeText(facilityPath);
				} catch (Exception e) {
					m_log.warn("makeStringMessage() cannot get facility path.(facilityId = " + facilityId + ") : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				}
			}
	
			output.setApplication(monitorInfo.getApplication());
	
			// メッセージに#[LOG_LINE]が入力されている場合は、 オリジナルメッセージに置換する。
			if (monitorStringValueInfo.getMessage() != null) {
				String str = monitorStringValueInfo.getMessage().replace("#[LOG_LINE]", customTrap.getMsg());
				// DBよりオリジナルメッセージ出力文字数を取得
				int maxLen = HinemosPropertyCommon.monitor_log_line_max_length.getIntegerValue();
				m_log.info("monitor.log.line.max.length = " + maxLen);
				if (str.length() > maxLen) {
					str = str.substring(0, maxLen);
				}
				output.setMessage(str);
			}
	
			output.setMessageOrg(customTrap.getOrgMsg());
			output.setPriority(priority);
			output.setGenerationDate(customTrap.getSampledTime());

			output.setPriorityChangeJudgmentType(monitorInfo.getPriorityChangeJudgmentType());
			output.setPriorityChangeFailureType(monitorInfo.getPriorityChangeFailureType());

			if (runInstructionInfo != null) {
				// 監視ジョブ
				MonitorJobWorker.endMonitorJob(
						runInstructionInfo,
						HinemosModuleConstant.MONITOR_CUSTOMTRAP_S,
						makeJobOrgMessageString(monitorInfo, output.getMessageOrg(), output.getMessage()),
						"",
						RunStatusConstant.END,
						MonitorJobWorker.getReturnValue(runInstructionInfo, priority));
			} else {
				// 監視ジョブ以外
				rtn.add(output);
			}
		}
		return rtn;
	}

	/**
	 * カスタムトラップ監視（数値）を通知します。
	 * 
	 * @param customtrapList	受信データリスト
	 * @param monitorInfo		監視情報
	 * @param priorityBuffer	プライオリティリスト
	 * @param facilityIdList	ファシリティリスト
	 * @param agentAddr			Agentアドレス
	 * @param valueBuffer		Valueリスト
	 * @return 通知情報リスト
	 */
	public List<OutputBasicInfo> createNumOutputBasicInfoList(List<CustomTrap> customtrapList, MonitorInfo monitorInfo, List<Integer> priorityBuffer,
			List<String> facilityIdList, String agentAddr, List<Double> valueBuffer, RunInstructionInfo runInstructionInfo) {
		List<OutputBasicInfo> rtn = new ArrayList<>();
		for (int i = 0; i < customtrapList.size(); i++) {
			CustomTrap customTrap = customtrapList.get(i);
			String facilityId = facilityIdList.get(i);
			int priority = priorityBuffer.get(i);
			Double value = valueBuffer.get(i);

			OutputBasicInfo output = new OutputBasicInfo();
			output.setNotifyGroupId(NotifyGroupIdGenerator.generate(monitorInfo));
			output.setJoblinkMessageId(JobLinkMessageId.getId(NotifyTriggerType.MONITOR, HinemosModuleConstant.MONITOR_CUSTOMTRAP_N, monitorInfo.getMonitorId()));
			output.setMonitorId(monitorInfo.getMonitorId());
			output.setPluginId(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N);
			output.setSubKey(customTrap.getKey());
			output.setPriority(priority);
			output.setApplication(monitorInfo.getApplication());
			output.setFacilityId(facilityId);
			output.setPriorityChangeJudgmentType(monitorInfo.getPriorityChangeJudgmentType());
			output.setPriorityChangeFailureType(monitorInfo.getPriorityChangeFailureType());
	
			if (FacilityTreeAttributeConstant.UNREGISTERED_SCOPE.equals(facilityId)) {
				// 未登録ノードの場合には送信元IPを表示する。
				output.setScopeText(agentAddr);
			} else {
				// ファシリティのパスを設定する]
				try {
					String facilityPath = new RepositoryControllerBean().getFacilityPath(facilityId, null);
					output.setScopeText(facilityPath);
				} catch (Exception e) {
					m_log.warn("makeNumMessage() cannot get facility path.(facilityId = " + facilityId + ") : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				}
			}
	
			output.setGenerationDate(customTrap.getSampledTime());
			String msg =null;
			if (monitorInfo.getCustomTrapCheckInfo().getConvertFlg() == ConvertValueConstant.TYPE_DELTA) {
				msg = "DIFF VALUE : " + customTrap.getKey() + "=" + value;
			}else{
				msg = "VALUE : " + customTrap.getKey() + "=" + value;
			}
	
			output.setMessage(msg);
			output.setMessageOrg(customTrap.getOrgMsg());

			if (runInstructionInfo != null) {
				// 監視ジョブ
				MonitorJobWorker.endMonitorJob(
					runInstructionInfo,
					HinemosModuleConstant.MONITOR_CUSTOMTRAP_N,
					makeJobOrgMessageNum(monitorInfo, output.getMessageOrg(), output.getMessage()),
					"",
					RunStatusConstant.END,
					MonitorJobWorker.getReturnValue(runInstructionInfo, priority));
			} else {
				// 監視ジョブ以外
				rtn.add(output);
			}
		}
		return rtn;
	}

	/**
	 * カスタムトラップ監視（数値）の将来予測を通知します。
	 * 
	 * @param customtrapList		受信データリスト
	 * @param monitorInfo			監視情報
	 * @param agentAddr				Agentアドレス
	 * @param collectResultBuffer	監視結果リスト
	 * @return 通知情報リスト
	 */
	public List<OutputBasicInfo> createPredictionOutputBasicInfoList(
			List<CustomTrap> customtrapList, MonitorInfo monitorInfo, 
			String agentAddr, List<MonitorRunResultInfo> collectResultBuffer) {
		List<OutputBasicInfo> rtn = new ArrayList<>();
		for (int i = 0; i < collectResultBuffer.size(); i++) {
			MonitorRunResultInfo collectResult = collectResultBuffer.get(i);
			OutputBasicInfo output = new OutputBasicInfo();
			output.setNotifyGroupId(collectResult.getNotifyGroupId());
			output.setJoblinkMessageId(JobLinkMessageId.getId(NotifyTriggerType.MONITOR,
					HinemosModuleConstant.MONITOR_CUSTOMTRAP_N, monitorInfo.getMonitorId()));
			output.setMonitorId(monitorInfo.getMonitorId());
			output.setPluginId(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N);
			output.setSubKey(collectResult.getDisplayName());
			output.setPriority(collectResult.getPriority());
			output.setApplication(collectResult.getApplication());
			output.setFacilityId(collectResult.getFacilityId());
	
			if (FacilityTreeAttributeConstant.UNREGISTERED_SCOPE.equals(output.getFacilityId())) {
				// 未登録ノードの場合には送信元IPを表示する。
				output.setScopeText(agentAddr);
			} else {
				// ファシリティのパスを設定する]
				try {
					String facilityPath = new RepositoryControllerBean().getFacilityPath(output.getFacilityId(), null);
					output.setScopeText(facilityPath);
				} catch (Exception e) {
					m_log.warn("makeNumMessage() cannot get facility path.(facilityId = " + output.getFacilityId() + ") : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				}
			}
			output.setGenerationDate(collectResult.getNodeDate());
			output.setMessage(collectResult.getMessage());
			output.setMessageOrg(collectResult.getMessageOrg());
			rtn.add(output);
		}
		return rtn;
	}

	/**
	 * 文字列用監視ジョブメッセージ作成
	 * 
	 * @param monitorInfo 監視情報
	 * @param orgMsg オリジナルメッセージ
	 * @param msg メッセージ
	 * @return
	 */
	private static String makeJobOrgMessageNum(MonitorInfo monitorInfo, String orgMsg, String msg) {
		if (monitorInfo == null || monitorInfo.getCustomTrapCheckInfo() == null) {
			return "";
		}
		String[] args = {monitorInfo.getCustomTrapCheckInfo().getTargetKey(), ""};
		if(monitorInfo.getCustomTrapCheckInfo().getConvertFlg()
				== ConvertValueConstant.TYPE_NO){
			// 何もしない
			args[1] = MessageConstant.CONVERT_NO.getMessage();
		} else if (monitorInfo.getCustomTrapCheckInfo().getConvertFlg() 
				== ConvertValueConstant.TYPE_DELTA) {
			// 差分をとる
			args[1] = MessageConstant.DELTA.getMessage();
		}
		return MessageConstant.MESSAGE_JOB_MONITOR_ORGMSG_CUSTOM_TRP_N.getMessage(args)
				+ "\n" + orgMsg
				+ "\n" + msg;
	}


	/**
	 * 文字列用監視ジョブメッセージ作成
	 * 
	 * @param monitorInfo 監視情報
	 * @param orgMsg オリジナルメッセージ
	 * @param msg メッセージ
	 * @return
	 */
	private static String makeJobOrgMessageString(MonitorInfo monitorInfo, String orgMsg, String msg) {
		if (monitorInfo == null || monitorInfo.getCustomTrapCheckInfo() == null) {
			return "";
		}
		String[] args = {monitorInfo.getCustomTrapCheckInfo().getTargetKey()};
		return MessageConstant.MESSAGE_JOB_MONITOR_ORGMSG_CUSTOM_TRP_S.getMessage(args)
				+ "\n" + orgMsg;
	}
}