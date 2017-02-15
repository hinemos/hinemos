/*

 Copyright (C) 2016 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.customtrap.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.customtrap.bean.CustomTrap;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.jobmanagement.util.MonitorJobWorker;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.monitor.bean.ConvertValueConstant;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfo;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
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
	 */
	public void putString(List<CustomTrap> customtrapList, MonitorInfo monitorInfo, List<Integer> priorityBuffer,
			List<MonitorStringValueInfo> ruleList, List<String> facilityIdList,String agentAddr,
			RunInstructionInfo runInstructionInfo) {
		ArrayList<OutputBasicInfo> logOutputList = new ArrayList<OutputBasicInfo>();
		for (int i = 0; i < customtrapList.size(); i++) {
			CustomTrap customTrap = customtrapList.get(i);
			MonitorStringValueInfo rule = ruleList.get(i);
			String logFacilityId = facilityIdList.get(i);
			int priority = priorityBuffer.get(i);

			OutputBasicInfo logOutput = makeStringMessage(customTrap, monitorInfo, rule, logFacilityId, agentAddr, priority);
			logOutputList.add(logOutput);

			if (runInstructionInfo != null) {
				// 監視ジョブ
				MonitorJobWorker.endMonitorJob(
						runInstructionInfo,
						HinemosModuleConstant.MONITOR_CUSTOMTRAP_S,
						makeJobOrgMessageString(monitorInfo, logOutput.getMessageOrg(), logOutput.getMessage()),
						"",
						RunStatusConstant.END,
						MonitorJobWorker.getReturnValue(runInstructionInfo, priority));
			}
		}

		// メッセージ送信処理
		if (runInstructionInfo == null) {
			new NotifyControllerBean().notify(logOutputList, NotifyGroupIdGenerator.generate(monitorInfo));
		}
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
	 */
	public void putNum(List<CustomTrap> customtrapList, MonitorInfo monitorInfo, List<Integer> priorityBuffer,
			List<String> facilityIdList, String agentAddr, List<Double> valueBuffer, RunInstructionInfo runInstructionInfo) {
		ArrayList<OutputBasicInfo> logOutputList = new ArrayList<OutputBasicInfo>();
		for (int i = 0; i < customtrapList.size(); i++) {
			CustomTrap customTrap = customtrapList.get(i);
			String logFacilityId = facilityIdList.get(i);
			int priority = priorityBuffer.get(i);
			Double value = valueBuffer.get(i);

			OutputBasicInfo logOutput = makeNumMessage(customTrap, monitorInfo, logFacilityId, agentAddr, priority, value);
			logOutputList.add(logOutput);
			if (runInstructionInfo != null) {
				// 監視ジョブ
				MonitorJobWorker.endMonitorJob(
					runInstructionInfo,
					HinemosModuleConstant.MONITOR_CUSTOMTRAP_N,
					makeJobOrgMessageNum(monitorInfo, logOutput.getMessageOrg(), logOutput.getMessage()),
					"",
					RunStatusConstant.END,
					MonitorJobWorker.getReturnValue(runInstructionInfo, priority));
			}
		}

		// メッセージ送信処理
		if (runInstructionInfo == null) {
			new NotifyControllerBean().notify(logOutputList, NotifyGroupIdGenerator.generate(monitorInfo));
		}
	}

	/**
	 * 引数で指定された情報より、カスタムトラップ（文字列）メッセージを生成し返します。
	 * 
	 * @param customTrap	受信したカスタムトラップ情報
	 * @param monInfo		モニタ情報
	 * @param monitorStringValueInfo	文字列監視情報
	 * @param facilityId	ファシリティID
	 * @param agentAddr		Agentアドレス
	 * @param priority		プライオリティ
	 * @return				成したメッセージ
	 */
	private static OutputBasicInfo makeStringMessage(CustomTrap customTrap, MonitorInfo monInfo,
			MonitorStringValueInfo monitorStringValueInfo, String facilityId, String agentAddr,int priority) {

		OutputBasicInfo output = new OutputBasicInfo();
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

		output.setApplication(monInfo.getApplication());

		// メッセージに#[LOG_LINE]が入力されている場合は、 オリジナルメッセージに置換する。
		if (monitorStringValueInfo.getMessage() != null) {
			String str = monitorStringValueInfo.getMessage().replace("#[LOG_LINE]", customTrap.getMsg());
			// DBよりオリジナルメッセージ出力文字数を取得
			int maxLen = HinemosPropertyUtil.getHinemosPropertyNum("monitor.log.line.max.length", Long.valueOf(256)).intValue();
			m_log.info("monitor.log.line.max.length = " + maxLen);
			if (str.length() > maxLen) {
				str = str.substring(0, maxLen);
			}
			output.setMessage(str);
		}

		output.setMessageOrg(customTrap.getOrgMsg());
		output.setPriority(priority);
		output.setGenerationDate(customTrap.getSampledTime());

		return output;
	}

	/**
	 * 引数で指定された情報より、カスタムトラップ（文字列）メッセージを生成し返します。
	 * 
	 * @param customTrap	受信したカスタムトラップ情報
	 * @param monInfo		モニタ情報
	 * @param facilityId	ファシリティID
	 * @param priority		プライオリティ
	 * @param agentAddr		Agentアドレス
	 * @param value			受信した値
	 * @return				作成したメッセージ
	 */
	private static OutputBasicInfo makeNumMessage(CustomTrap customTrap, MonitorInfo monInfo, String facilityId,String agentAddr,
			int priority, Double value) {
		// Local Variable
		OutputBasicInfo notifyInfo = null;
		
		notifyInfo = new OutputBasicInfo();
		notifyInfo.setMonitorId(monInfo.getMonitorId());
		notifyInfo.setPluginId(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N);
		notifyInfo.setSubKey(customTrap.getKey());
		notifyInfo.setPriority(priority);
		notifyInfo.setApplication(monInfo.getApplication());
		notifyInfo.setFacilityId(facilityId);

		if (FacilityTreeAttributeConstant.UNREGISTERED_SCOPE.equals(facilityId)) {
			// 未登録ノードの場合には送信元IPを表示する。
			notifyInfo.setScopeText(agentAddr);
		} else {
			// ファシリティのパスを設定する]
			try {
				String facilityPath = new RepositoryControllerBean().getFacilityPath(facilityId, null);
				notifyInfo.setScopeText(facilityPath);
			} catch (Exception e) {
				m_log.warn("makeNumMessage() cannot get facility path.(facilityId = " + facilityId + ") : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}
		}

		notifyInfo.setGenerationDate(customTrap.getSampledTime());
		String msg =null;
		if (monInfo.getCustomTrapCheckInfo().getConvertFlg() == ConvertValueConstant.TYPE_DELTA) {
			msg = "DIFF VALUE : " + customTrap.getKey() + "=" + value;
		}else{
			msg = "VALUE : " + customTrap.getKey() + "=" + value;
		}

		notifyInfo.setMessage(msg);
		notifyInfo.setMessageOrg(customTrap.getOrgMsg());
		return notifyInfo;
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