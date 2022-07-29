/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import java.util.UUID;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.infra.model.InfraManagementInfo;
import com.clustercontrol.infra.util.InfraConstants;
import com.clustercontrol.jobmanagement.bean.JobInfo;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobInfoEntityPK;
import com.clustercontrol.maintenance.model.MaintenanceInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.reporting.bean.ReportingInfo;
import com.clustercontrol.repository.model.NodeConfigSettingInfo;
import com.clustercontrol.rpa.scenario.model.RpaScenarioOperationResultCreateSetting;
import com.clustercontrol.rpa.scenario.model.UpdateRpaScenarioOperationResultInfo;
import com.clustercontrol.sdml.model.SdmlControlSettingInfo;
import com.clustercontrol.sdml.model.SdmlMonitorNotifyRelation;


public class NotifyGroupIdGenerator {
	private static String generateForJob(String jobunitId, String jobId, Integer noticeType){
		String ret = HinemosModuleConstant.JOB_MST
				+ "-"+jobunitId
				+"-"+jobId
				+"-"+noticeType;

		return ret;
	}

	private static String generateForJobSession(String sessionId, String jobunitId, String jobId, Integer noticeType){
		String ret = HinemosModuleConstant.JOB_SESSION
				+"-"+sessionId
				+"-"+jobunitId
				+"-"+jobId
				+"-"+noticeType;

		return ret;
	}

	private static String generateForMonitor(String monitorTypeId, String monitorID){
		return  generateForMonitor(monitorTypeId, monitorID, 0);
	}

	private static  String generateForMonitor(String monitorTypeId, String monitorID, Integer orderNo){
		/* monitorTypeが存在するかチェック */
		if(!com.clustercontrol.bean.HinemosModuleConstant.isExist(monitorTypeId)){
			return null;
		}

		return monitorTypeId + "-" + monitorID + "-" + orderNo;
	}
	
	private static String generateForInfra(String managementId) {
		return InfraConstants.notifyGroupIdPrefix + managementId;
	}
	
	private static String generateForNodeConfigSetting(String settingId) {
		return HinemosModuleConstant.NODE_CONFIG_SETTING + "-" + settingId;
	}
	
	private static String generateForReporting(String reportingId) {
		return HinemosModuleConstant.REPORTING + "-" + reportingId + "-0";
	}

	private static String generateForMaintenance(String maintenanceId){
		return HinemosModuleConstant.SYSYTEM_MAINTENANCE + "-" + maintenanceId + "-0";
	}
	
	private static String generateForRpaScenarioOperationResultCreateSetting(String settingId) {
		return HinemosModuleConstant.RPA_SCENARIO_CREATE + "-" + settingId;
	}

	private static String generateForRpaScenarioOperationResultCorrection() {
		// IDは自動採番するため、通知グループIDはランダム生成
		// シナリオ実績更新実行後に通知グループは削除される。
		return HinemosModuleConstant.RPA_SCENARIO_CORRECT + "-" + UUID.randomUUID();
	}

	private static String generateForSdmlControl(String id, boolean isMonitorCommon) {
		if (isMonitorCommon) {
			return HinemosModuleConstant.SDML_CONTROL + "-" + id + "-COMMON";
		} else {
			return HinemosModuleConstant.SDML_CONTROL + "-" + id;
		}
	}

	private static String generateForSdmlMonitorNotify(String id, String sdmlMonitorTypeId) {
		return HinemosModuleConstant.SDML_CONTROL + "-" + id + "-" + sdmlMonitorTypeId;
	}

	/**
	 * 監視関連の通知グループIDを生成します。
	 * 
	 * @return 通知グループID
	 */
	public static  String generate(MonitorInfo info){
		return generateForMonitor(info.getMonitorTypeId(), info.getMonitorId());
	}

	/**
	 * ジョブ関連の通知グループIDを生成します。
	 * 
	 * @return 通知グループID
	 */
	public static String generate(JobInfo info) {
		return generateForJob(info.getJobunitId(), info.getId(), 0);
	}

	/**
	 * ジョブセッション関連の通知グループIDを生成します。
	 * 
	 * @return 通知グループID
	 */
	public static String generate(JobInfoEntity entity) {
		JobInfoEntityPK pk = entity.getId();
		return generateForJobSession(pk.getSessionId(), pk.getJobunitId(), pk.getJobId(), 0);
	}
	
	public static String generate(InfraManagementInfo info) {
		return generateForInfra(info.getManagementId());
	}

	/**
	 * 対象構成情報の通知グループIDを生成します。
	 * 
	 * @return 通知グループID
	 */
	public static  String generate(NodeConfigSettingInfo info){
		return generateForNodeConfigSetting(info.getSettingId());
	}

	/**
	 * 対象メンテナンス情報の通知グループIDを生成します。
	 * 
	 * @return 通知グループID
	 */
	public static  String generate(MaintenanceInfo info){
		return generateForMaintenance(info.getMaintenanceId());
	}
	
	/**
	 * レポーティング関連の通知グループIDを生成します。
	 * 
	 * @return 通知グループID
	 */
	public static  String generate(ReportingInfo info){
		return generateForReporting(info.getReportScheduleId());
	}

	/**
	 * RPAシナリオ実績作成設定関連の通知グループIDを生成します。
	 * 
	 * @return 通知グループID
	 */
	public static  String generate(RpaScenarioOperationResultCreateSetting info){
		return generateForRpaScenarioOperationResultCreateSetting(info.getScenarioOperationResultCreateSettingId());
	}

	/**
	 * RPAシナリオ実績更新の通知グループIDを生成します。
	 * 
	 * @return 通知グループID
	 */
	public static  String generate(UpdateRpaScenarioOperationResultInfo info){
		return generateForRpaScenarioOperationResultCorrection();
	}

	/**
	 * SDML制御設定の通知グループIDを生成します。
	 * 
	 * @param info
	 * @return
	 */
	public static String generate(SdmlControlSettingInfo info) {
		return generate(info, false);
	}

	/**
	 * SDML制御設定の通知グループIDを生成します。<br>
	 * 自動作成監視用の種別共通通知の場合はisMonitorCommonを指定します。
	 * 
	 * @param info
	 * @param isMonitorCommon
	 * @return
	 */
	public static String generate(SdmlControlSettingInfo info, boolean isMonitorCommon) {
		return generateForSdmlControl(info.getApplicationId(), isMonitorCommon);
	}

	/**
	 * SDML制御設定の個別通知設定の通知グループIDを生成します。
	 * 
	 * @param info
	 * @return
	 */
	public static String generate(SdmlMonitorNotifyRelation info) {
		return generateForSdmlMonitorNotify(info.getApplicationId(), info.getSdmlMonitorTypeId());
	}
}