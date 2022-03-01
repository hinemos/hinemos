/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.custom.factory;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.custom.bean.CommandResultDTO;
import com.clustercontrol.fault.CustomInvalid;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.jobmanagement.bean.JobLinkMessageId;
import com.clustercontrol.jobmanagement.bean.MonitorJobEndNode;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.bean.NotifyTriggerType;
import com.clustercontrol.notify.bean.OutputBasicInfo;

/**
 * コマンド監視の監視処理基底クラス<br/>
 * 
 * @version 6.0.0
 * @since 6.0.0
 */
public abstract class RunCustomBase {

	protected CommandResultDTO result = null;
	protected List<MonitorJobEndNode> monitorJobEndNodeList = new ArrayList<>(); 
	
	/**
	 * 閾値判定を行い、監視結果を通知する。<br/>
	 * 
	 * @return 通知情報リスト
	 * @throws HinemosUnknown
	 *             予期せぬ内部エラーが発生した場合
	 * @throws MonitorNotFound
	 *             該当する監視設定が存在しない場合
	 * @throws CustomInvalid
	 *             監視設定に不整合が存在する場合
	 */
	public abstract List<OutputBasicInfo> monitor() throws HinemosUnknown, MonitorNotFound, CustomInvalid;

	/**
	 * 通知情報を作成する。<br/>
	 * 
	 * @param priority
	 *            監視結果の重要度(PriorityConstant.INFOなど)
	 * @param monitor
	 *            コマンド監視に対応するMonitorInfo
	 * @param facilityId
	 *            監視結果に対応するファシリティID
	 * @param facilityPath
	 *            ファシリティIDに対応するパス文字列
	 * @param msg
	 *            監視結果に埋め込むメッセージ
	 * @param msgOrig
	 *            監視結果に埋め込むオリジナルメッセージ
	 * @param pluginID
	 *            PluginID
	 * @param isPredict
	 *            true:将来予測
	 * @return 通知情報
	 *      
	 * @throws HinemosUnknown
	 *             予期せぬ内部エラーが発生した場合
	 * @throws CustomInvalid
	 *             監視設定に不整合が存在する場合
	 */
	protected OutputBasicInfo createOutputBasicInfo(int priority, MonitorInfo monitor, String facilityId, String facilityPath, String subKey,
			String msg, String msgOrig, String pluginID, String applicationName, String notifyGroupId) throws HinemosUnknown, CustomInvalid {
		// Local Variable
		OutputBasicInfo notifyInfo = null;

		notifyInfo = new OutputBasicInfo();
		notifyInfo.setMonitorId(monitor.getMonitorId());
		notifyInfo.setPluginId(pluginID);
		// デバイス名単位に通知抑制されるよう、抑制用サブキーを設定する。
		notifyInfo.setSubKey(subKey == null ? "" : subKey);
		notifyInfo.setPriority(priority);
		notifyInfo.setFacilityId(facilityId);
		notifyInfo.setScopeText(facilityPath);
		notifyInfo.setGenerationDate(result.getCollectDate());
		notifyInfo.setMessage(msg);
		notifyInfo.setMessageOrg(msgOrig);

		if (notifyGroupId == null || notifyGroupId.isEmpty()) {
			notifyGroupId = NotifyGroupIdGenerator.generate(monitor);
		}
		if (applicationName == null || applicationName.isEmpty()) {
			applicationName = monitor.getApplication();
		}
		notifyInfo.setApplication(applicationName);
		notifyInfo.setNotifyGroupId(notifyGroupId);
		notifyInfo.setJoblinkMessageId(JobLinkMessageId.getId(NotifyTriggerType.MONITOR, pluginID, monitor.getMonitorId()));
		
		notifyInfo.setPriorityChangeJudgmentType(monitor.getPriorityChangeJudgmentType());
		notifyInfo.setPriorityChangeFailureType(monitor.getPriorityChangeFailureType());
		
		return notifyInfo;
	}

	/**
	 * 監視ジョブの実行結果を返す
	 * 
	 * @return 監視ジョブの実行結果
	 */
	public List<MonitorJobEndNode> getMonitorJobEndNodeList() {
		return this.monitorJobEndNodeList;
	}

}
