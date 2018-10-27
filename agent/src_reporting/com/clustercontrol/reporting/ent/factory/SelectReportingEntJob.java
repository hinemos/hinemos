/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.ent.factory;

import java.util.List;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;

public class SelectReportingEntJob {

	/**
	 * JobInfoEntityを取得します。<BR>
	 * 
	 * @param sessionId
	 * @param jobunitId
	 * @param jobId
	 * @return
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 */
	public JobInfoEntity getJobInfoEntityPK(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound {
		return com.clustercontrol.reporting.ent.util.QueryUtil.getJobInfoEntityPK(sessionId, jobunitId, jobId, ObjectPrivilegeMode.NONE);
	}
	
	/**
	 * JobSessionJobのサマリーを取得します。<BR>
	 * 
	 * @param fromTime
	 * @param toTime
	 * @param jobunitId
	 * @param jobId
	 * @param excJobId
	 * @param jobOrderKey
	 * @param ownerRoleId
	 * @param orderNum
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getSummaryJobSessionJob(Long fromTime, Long toTime, String jobunitId, String jobId,
			String excJobId, String jobOrderKey, String ownerRoleId, int orderNum) throws HinemosDbTimeout {
		return com.clustercontrol.reporting.ent.util.QueryUtil.getSummaryJobSessionJob(fromTime, toTime, jobunitId, jobId, excJobId, jobOrderKey, ownerRoleId, orderNum);
	}
	
	/**
	 * 最大実行時間のJobSessionJobEntityを取得します。<BR>
	 * 
	 * @param maxTime
	 * @param jobunitId
	 * @param jobId
	 * @return
	 */
	public List<JobSessionJobEntity> getJobSessionJobEntityByMaxTime(Long maxTime, String jobunitId, String jobId) {
		return com.clustercontrol.reporting.ent.util.QueryUtil.getJobSessionJobEntityByMaxTime(maxTime, jobunitId, jobId);
	}
	
	/**
	 * jobunitIdとjobIdを条件にJobSessionJobEntityを取得します。<BR>
	 * 
	 * @param fromTime
	 * @param toTime
	 * @param jobunitId
	 * @param jobId
	 * @param ownerRoleId
	 * @return
	 */
	public List<JobSessionJobEntity> getJobSessionJobByJobunitIdAndJobId(Long fromTime, Long toTime, String jobunitId, String jobId, String ownerRoleId){
		return com.clustercontrol.reporting.ent.util.QueryUtil.getJobSessionJobByJobunitIdAndJobId(fromTime, toTime, jobunitId, jobId, ownerRoleId);
	}
}
