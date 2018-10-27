/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.factory;

import java.util.List;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;

/**
 * ジョブ情報を検索するクラス<BR>
 * <p>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class SelectReportingJob {

	/**
	 * JobInfoEntityを取得します。<BR>
	 * 
	 * @param sessionId
	 * @param jobunitId
	 * @param jobId
	 * @return
	 */
	public JobInfoEntity getJobInfo(String sessionId, String jobunitId, String jobId) {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getJobInfoEntityPK(sessionId, jobunitId, jobId, ObjectPrivilegeMode.NONE);
	}

	/**
	 * JobSessionEntityの一覧を取得します。<BR>
	 * 
	 * @param jobunitId
	 * @param jobId
	 * @param excJobId
	 * @param parentJobunitId
	 * @param ownerRoleId
	 * @param fromTime
	 * @param toTime
	 * @return
	 */
	public List<JobSessionEntity> getReportingJobSessionList(String jobunitId, String jobId, String excJobId,
			String parentJobunitId, String ownerRoleId, Long fromTime, Long toTime) {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getReportingJobSessionList(jobunitId, jobId, excJobId,
				parentJobunitId, ownerRoleId, fromTime, toTime);

	}
	
	/**
	 * JobSessionNodeEntityの一覧を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param jobunitId
	 * @param jobId
	 * @param excJobId
	 * @param ownerRoleId
	 * @param fromTime
	 * @param toTime
	 * @return
	 */
	public List<JobSessionNodeEntity> getReportingJobSessionNodeList(String facilityId, String jobunitId,
			String jobId, String excJobId, String ownerRoleId, Long fromTime, Long toTime) {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getReportingJobSessionNodeList(facilityId, jobunitId, jobId, excJobId, ownerRoleId, fromTime, toTime);
	}
	
	/**
	 * JobSessionJobEntityの一覧を取得します。<BR>
	 * @param sessionId TODO
	 * @param jobunitId
	 * @param jobId
	 * @param excJobId
	 * @param ownerRoleId
	 * 
	 * @return
	 */
	public List<JobSessionJobEntity> getReportingJobDetailList(String sessionId, String jobunitId, String jobId,
			String excJobId, String ownerRoleId) {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getReportingJobDetailList(sessionId, jobunitId, jobId, excJobId, ownerRoleId);
	}
	
	/**
	 * parentJobunitIdを条件に、ReportingJobSessionEntityを取得します。<BR>
	 * @param parentJobunitId
	 * @param fromTime
	 * @param toTime
	 * 
	 * @return
	 */
	public List<JobSessionJobEntity> getRootJobSessionJobByParentJobunitId(String parentJobunitId, Long fromTime, Long toTime){
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getRootJobSessionJobByParentJobunitId(parentJobunitId, fromTime, toTime);
	}
}