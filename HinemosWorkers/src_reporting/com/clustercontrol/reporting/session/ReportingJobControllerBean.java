/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.session;

import java.util.List;

import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.reporting.factory.SelectReportingJob;


/**
*
* <!-- begin-user-doc --> ジョブ情報の制御を行うsession bean <!-- end-user-doc --> *
*
*/
public class ReportingJobControllerBean {

	/**
	 * JobInfoEntityを取得します。<BR>
	 * 
	 * @param sessionId
	 * @param jobunitId
	 * @param jobId
	 * @return
	 */
	public JobInfoEntity getJobInfo(String sessionId, String jobunitId, String jobId) {
		SelectReportingJob select = new SelectReportingJob();
		JobInfoEntity entity = select.getJobInfo(sessionId, jobunitId, jobId);
		return entity;
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
		SelectReportingJob select = new SelectReportingJob();
		List<JobSessionEntity> list = select.getReportingJobSessionList(jobunitId, jobId, excJobId, parentJobunitId, ownerRoleId, fromTime, toTime);
		return list;
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
		SelectReportingJob select = new SelectReportingJob();
		List<JobSessionNodeEntity> list = select.getReportingJobSessionNodeList(facilityId, jobunitId, jobId, excJobId, ownerRoleId, fromTime, toTime);
		return list;
	}
	
	/**
	 * JobSessionEntityの一覧を取得します。<BR>
	 * @param sessionId
	 * @param jobunitId
	 * @param jobId
	 * @param excJobId
	 * @param ownerRoleId
	 * 
	 * @return
	 */
	public List<JobSessionJobEntity> getReportingJobDetailList(String sessionId, String jobunitId, String jobId,
			String excJobId, String ownerRoleId) {
		SelectReportingJob select = new SelectReportingJob();
		List<JobSessionJobEntity> list = select.getReportingJobDetailList(sessionId, jobunitId, jobId, excJobId, ownerRoleId);
		return list;
	}
	
	/**
	 * parentJobunitIdを条件に、JobSessionEntityを取得します。<BR>
	 * @param parentJobunitId
	 * @param fromTime
	 * @param toTime
	 * 
	 * @return
	 */
	public List<JobSessionJobEntity> getRootJobSessionJobByParentJobunitId(String parentJobunitId, Long fromTime, Long toTime){
		SelectReportingJob select = new SelectReportingJob();
		List<JobSessionJobEntity> list = select.getRootJobSessionJobByParentJobunitId(parentJobunitId, fromTime, toTime);
		return list;
	}
}
