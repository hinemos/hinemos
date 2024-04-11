/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ControlEnum;

/**
 * ジョブ操作用プロパティを作成するクラスです。
 *
 * @version 2.0.0
 * @since 1.0.0
 */
public class JobOperationProperty {

	private static Log m_log = LogFactory.getLog( JobOperationProperty.class );

	public ArrayList<Integer> getAvailableStartOperation (String sessionId, String jobunitId, String jobId, String facilityId, Locale locale) {

		int status = 0;
		int jobType = 0;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
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
						|| sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_MONITORJOB
						|| sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_JOBLINKSENDJOB
						|| sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_JOBLINKRCVJOB
						|| sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_FILECHECKJOB
						|| sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_RESOURCEJOB
						|| sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_RPAJOB){
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
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_START_FORCE_RUN, jobType, status)){
			values.add(OperationConstant.TYPE_START_FORCE_RUN);
		}

		return values;
	}
	
	public ArrayList<ControlEnum> getAvailableStartOperationSessionJob (String sessionId, String jobunitId, String jobId, Locale locale) throws JobInfoNotFound {

		int status = 0;
		int jobType = 0;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			//セッションIDとジョブIDから、セッションジョブを取得
			JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

			//実行状態を取得
			status = sessionJob.getStatus();

			//ジョブタイプを取得
			if(sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_JOB
					|| sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_APPROVALJOB
					|| sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_MONITORJOB
					|| sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_JOBLINKSENDJOB
					|| sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_JOBLINKRCVJOB
					|| sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_FILECHECKJOB
					|| sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_RESOURCEJOB
					|| sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_RPAJOB){
				jobType = JobOperationJudgment.TYPE_JOB;
			}
			else{
				jobType = JobOperationJudgment.TYPE_JOBNET;
			}
		} catch (JobInfoNotFound e) {
			// ジョブ情報が見つからない場合はスローする
			throw e;
		} catch (Exception e) {
			// 何もしない・
			m_log.warn("getAvailableStartOperationSessionJob() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}

		ArrayList<ControlEnum> values = new ArrayList<ControlEnum>();
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_START_AT_ONCE, jobType, status)){
			values.add(ControlEnum.START_AT_ONCE);
		}
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_START_SUSPEND, jobType, status)){
			values.add(ControlEnum.START_SUSPEND);
		}
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_START_WAIT, jobType, status)){
			values.add(ControlEnum.START_WAIT);
		}
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_START_SKIP, jobType, status)){
			values.add(ControlEnum.START_SKIP);
		}
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_START_FORCE_RUN, jobType, status)){
			values.add(ControlEnum.START_FORCE_RUN);
		}

		return values;
	}
	
	public ArrayList<ControlEnum> getAvailableStartOperationSessionNode (String sessionId, String jobunitId, String jobId, String facilityId, Locale locale) throws JobInfoNotFound {

		int status = 0;
		int jobType = 0;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
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
					m_log.info("getAvailableStartOperationSessionNode() : "
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
		} catch (JobInfoNotFound e) {
			// ジョブ情報が見つからない場合はスローする
			throw e;
		} catch (Exception e) {
			// 何もしない・
			m_log.warn("getAvailableStartOperationSessionNode() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}

		ArrayList<ControlEnum> values = new ArrayList<ControlEnum>();
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_START_AT_ONCE, jobType, status)){
			values.add(ControlEnum.START_AT_ONCE);
		}
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_START_SUSPEND, jobType, status)){
			values.add(ControlEnum.START_SUSPEND);
		}
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_START_WAIT, jobType, status)){
			values.add(ControlEnum.START_WAIT);
		}
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_START_SKIP, jobType, status)){
			values.add(ControlEnum.START_SKIP);
		}
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_START_FORCE_RUN, jobType, status)){
			values.add(ControlEnum.START_FORCE_RUN);
		}

		return values;
	}
	
	public ArrayList<Integer> getAvailableStopOperation(String sessionId, String jobunitId, String jobId, String facilityId, Locale locale) {

		int status = 0;
		int jobType = 0;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
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

	public ArrayList<ControlEnum> getAvailableStopOperationSessionJob(String sessionId, String jobunitId, String jobId, Locale locale) throws JobInfoNotFound {

		int status = 0;
		int jobType = 0;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			
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
			
		} catch (JobInfoNotFound e) {
			// ジョブ情報が見つからない場合はスロー
			throw e;
		} catch (Exception e) {
			// 何もしない・
			m_log.warn("getAvailableStopOperationSessionJob() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}

		ArrayList<ControlEnum> values = new ArrayList<ControlEnum>();
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_STOP_AT_ONCE, jobType, status)){
			values.add(ControlEnum.STOP_AT_ONCE);
		}
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_STOP_SUSPEND, jobType, status)){
			values.add(ControlEnum.STOP_SUSPEND);
		}
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_STOP_WAIT, jobType, status)){
			values.add(ControlEnum.STOP_WAIT);
		}
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_STOP_SKIP, jobType, status)){
			values.add(ControlEnum.STOP_SKIP);
		}
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_STOP_MAINTENANCE, jobType, status)){
			values.add(ControlEnum.STOP_MAINTENANCE);
		}
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_STOP_FORCE, jobType, status)){
			values.add(ControlEnum.STOP_FORCE);
		}

		return values;
	}
	
	public ArrayList<ControlEnum> getAvailableStopOperationSessionNode(String sessionId, String jobunitId, String jobId, String facilityId, Locale locale) throws JobInfoNotFound {

		int status = 0;
		int jobType = 0;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
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
					m_log.info("getAvailableStopOperationSessionNode() : "
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
		} catch (JobInfoNotFound e) {
			// ジョブ情報が見つからない場合はスロー
			throw e;
		} catch (Exception e) {
			// 何もしない・
			m_log.warn("getAvailableStopOperation() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}

		ArrayList<ControlEnum> values = new ArrayList<ControlEnum>();
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_STOP_AT_ONCE, jobType, status)){
			values.add(ControlEnum.STOP_AT_ONCE);
		}
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_STOP_SUSPEND, jobType, status)){
			values.add(ControlEnum.STOP_SUSPEND);
		}
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_STOP_WAIT, jobType, status)){
			values.add(ControlEnum.STOP_WAIT);
		}
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_STOP_SKIP, jobType, status)){
			values.add(ControlEnum.STOP_SKIP);
		}
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_STOP_MAINTENANCE, jobType, status)){
			values.add(ControlEnum.STOP_MAINTENANCE);
		}
		if(JobOperationJudgment.judgment(OperationConstant.TYPE_STOP_FORCE, jobType, status)){
			values.add(ControlEnum.STOP_FORCE);
		}

		return values;
	}
}
