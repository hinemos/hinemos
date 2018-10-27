/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.infra.model.InfraManagementInfo;
import com.clustercontrol.infra.util.InfraConstants;
import com.clustercontrol.jobmanagement.bean.JobInfo;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobInfoEntityPK;
import com.clustercontrol.monitor.run.model.MonitorInfo;


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
}