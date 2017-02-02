/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.factory;

import java.util.ArrayList;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.OperationConstant;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntityPK;
import com.clustercontrol.jobmanagement.util.QueryUtil;

/**
 * ジョブ操作用プロパティを作成するクラスです。
 *
 * @version 2.0.0
 * @since 1.0.0
 */
public class JobOperationProperty {

	private static Log m_log = LogFactory.getLog( JobOperationProperty.class );

	//FIXME javadoc修正、操作用プロパティにjobunitIDが必要か確認。
	public ArrayList<Integer> getAvailableStartOperation (String sessionId, String jobunitId, String jobId, String facilityId, Locale locale) {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		int status = 0;
		int jobType = 0;

		try {
			if(facilityId != null && facilityId.length() > 0){
				//セッションIDとジョブIDから、セッションジョブを取得
				JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
				//ジョブタイプを取得
				if(sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_FILEJOB){
					jobId = jobId + "_" + facilityId;
					facilityId = null;

					//セッションIDとジョブIDから、セッションジョブを取得
					JobSessionJobEntity childSessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

					//実行状態を取得
					status = childSessionJob.getStatus();

					//ジョブタイプを取得
					jobType = JobOperationJudgment.TYPE_JOBNET;
				}
				else{
					//セッションIDとジョブIDから、セッションジョブを取得
					JobSessionNodeEntityPK sessionNodePk = new JobSessionNodeEntityPK(sessionId, jobunitId, jobId, facilityId);
					JobSessionNodeEntity sessionNode = em.find(JobSessionNodeEntity.class, sessionNodePk, ObjectPrivilegeMode.READ);
					if (sessionNode == null) {
						JobInfoNotFound je = new JobInfoNotFound("JobSessionNodeEntity.findByPrimaryKey"
								+ ", " + sessionNodePk.toString());
						m_log.info("getAvailableStartOperation() : "
								+ je.getClass().getSimpleName() + ", " + je.getMessage());
						je.setSessionId(sessionId);
						je.setJobunitId(jobunitId);
						je.setJobId(jobId);
						je.setFacilityId(facilityId);
						throw je;
					}
					//実行状態を取得
					status = sessionNode.getStatus();

					//ジョブタイプを取得
					jobType = JobOperationJudgment.TYPE_NODE;
				}
			}
			else{
				//セッションIDとジョブIDから、セッションジョブを取得
				JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

				//実行状態を取得
				status = sessionJob.getStatus();

				//ジョブタイプを取得
				if(sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_JOB
						|| sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_APPROVALJOB
						|| sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_MONITORJOB){
					jobType = JobOperationJudgment.TYPE_JOB;
				}
				else{
					jobType = JobOperationJudgment.TYPE_JOBNET;
				}
			}
		} catch (JobInfoNotFound e) {
			// 何もしない・
		} catch (Exception e) {
			// 何もしない・
			m_log.warn("getAvailableStartOperation() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}

		ArrayList<Integer> values = new ArrayList<Integer>();
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_START_AT_ONCE, jobType, status)){
			values.add(OperationConstant.TYPE_START_AT_ONCE);
		}
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_START_SUSPEND, jobType, status)){
			values.add(OperationConstant.TYPE_START_SUSPEND);
		}
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_START_WAIT, jobType, status)){
			values.add(OperationConstant.TYPE_START_WAIT);
		}
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_START_SKIP, jobType, status)){
			values.add(OperationConstant.TYPE_START_SKIP);
		}

		return values;
	}

	public ArrayList<Integer> getAvailableStopOperation(String sessionId, String jobunitId, String jobId, String facilityId, Locale locale) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		int status = 0;
		int jobType = 0;

		try {
			if(facilityId != null && facilityId.length() > 0){
				//セッションIDとジョブIDから、セッションジョブを取得
				JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
				//ジョブタイプを取得
				if(sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_FILEJOB){
					jobId = jobId + "_" + facilityId;
					facilityId = null;

					//セッションIDとジョブIDから、セッションジョブを取得
					JobSessionJobEntity childSessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

					//実行状態を取得
					status = childSessionJob.getStatus();

					//ジョブタイプを取得
					jobType = JobOperationJudgment.TYPE_JOBNET;
				}
				else{
					//セッションIDとジョブIDから、セッションジョブを取得
					JobSessionNodeEntityPK sessionNodePk = new JobSessionNodeEntityPK(sessionId, jobunitId, jobId, facilityId);
					JobSessionNodeEntity sessionNode = em.find(JobSessionNodeEntity.class, sessionNodePk, ObjectPrivilegeMode.READ);
					if (sessionNode == null) {
						JobInfoNotFound je = new JobInfoNotFound("JobSessionNodeEntity.findByPrimaryKey"
								+ ", " + sessionNodePk);
						m_log.info("getAvailableStopOperation() : "
								+ je.getClass().getSimpleName() + ", " + je.getMessage());
						je.setSessionId(sessionId);
						je.setJobunitId(jobunitId);
						je.setJobId(jobId);
						je.setFacilityId(facilityId);
						throw je;
					}

					//実行状態を取得
					status = sessionNode.getStatus();

					//ジョブタイプを取得
					jobType = JobOperationJudgment.TYPE_NODE;
				}
			}
			else{
				//セッションIDとジョブIDから、セッションジョブを取得
				JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

				//実行状態を取得
				status = sessionJob.getStatus();

				//ジョブタイプを取得
				if(sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_JOB){
					jobType = JobOperationJudgment.TYPE_JOB;
				}
				else{
					jobType = JobOperationJudgment.TYPE_JOBNET;
				}
			}
		} catch (JobInfoNotFound e) {
			// 何もしない・
		} catch (Exception e) {
			// 何もしない・
			m_log.warn("getAvailableStopOperation() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}

		ArrayList<Integer> values = new ArrayList<Integer>();
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_STOP_AT_ONCE, jobType, status)){
			values.add(OperationConstant.TYPE_STOP_AT_ONCE);
		}
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_STOP_SUSPEND, jobType, status)){
			values.add(OperationConstant.TYPE_STOP_SUSPEND);
		}
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_STOP_WAIT, jobType, status)){
			values.add(OperationConstant.TYPE_STOP_WAIT);
		}
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_STOP_SKIP, jobType, status)){
			values.add(OperationConstant.TYPE_STOP_SKIP);
		}
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_STOP_MAINTENANCE, jobType, status)){
			values.add(OperationConstant.TYPE_STOP_MAINTENANCE);
		}
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_STOP_FORCE, jobType, status)){
			values.add(OperationConstant.TYPE_STOP_FORCE);
		}

		return values;
	}

}
