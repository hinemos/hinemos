/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.QueryDivergence;
import com.clustercontrol.commons.util.QueryExecutor;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.IconFileNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobKickConstant;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.jobmanagement.factory.CreateJobSession;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobInfoEntityPK;
import com.clustercontrol.jobmanagement.model.JobKickEntity;
import com.clustercontrol.jobmanagement.model.JobLinkJobExpInfoEntity;
import com.clustercontrol.jobmanagement.model.JobLinkJobkickExpInfoEntity;
import com.clustercontrol.jobmanagement.model.JobLinkMessageEntity;
import com.clustercontrol.jobmanagement.model.JobLinkSendSettingEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntityPK;
import com.clustercontrol.jobmanagement.model.JobNextJobOrderInfoEntity;
import com.clustercontrol.jobmanagement.model.JobNextJobOrderMstEntity;
import com.clustercontrol.jobmanagement.model.JobParamMstEntity;
import com.clustercontrol.jobmanagement.model.JobRpaLoginResolutionMstEntity;
import com.clustercontrol.jobmanagement.model.JobRpaRunConditionEntity;
import com.clustercontrol.jobmanagement.model.JobRpaScreenshotEntity;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntityPK;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntityPK;
import com.clustercontrol.jobmanagement.model.JobWaitGroupInfoEntity;
import com.clustercontrol.jobmanagement.model.JobWaitInfoEntity;
import com.clustercontrol.jobmanagement.model.JobmapIconImageEntity;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;

import jakarta.persistence.TypedQuery;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static JobMstEntity getJobMstPK(JobMstEntityPK pk, ObjectPrivilegeMode mode) throws JobMasterNotFound, InvalidRole {
		JobMstEntity jobMst = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			jobMst = em.find(JobMstEntity.class, pk, mode);
			if (jobMst == null) {
				JobMasterNotFound je = new JobMasterNotFound("JobMstEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.info("getJobMstPK() : "
						+ je.getClass().getSimpleName() + ", " + je.getMessage());
				je.setJobunitId(pk.getJobunitId());
				je.setJobId(pk.getJobId());
				throw je;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getJobMstPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return jobMst;
	}

	public static JobMstEntity getJobMstPK_NONE(JobMstEntityPK pk) throws JobMasterNotFound {
		JobMstEntity jobMst = null;
		try {
			jobMst = getJobMstPK(pk, ObjectPrivilegeMode.NONE);
		} catch (InvalidRole e) {
			// NONE（オブジェクト権限チェックなし）のため、ここは通らない。
		}
		return jobMst;
	}

	public static JobMstEntity getJobMstPK(String jobunitId, String jobId) throws JobMasterNotFound, InvalidRole {
		return getJobMstPK(new JobMstEntityPK(jobunitId, jobId), ObjectPrivilegeMode.READ);
	}

	public static JobMstEntity getJobMstPK_OR(String jobunitId, String jobId, String ownerRoleId) throws JobMasterNotFound, InvalidRole {
		JobMstEntity jobMst = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			JobMstEntityPK pk = new JobMstEntityPK(jobunitId, jobId);
			jobMst = em.find_OR(JobMstEntity.class, pk, ObjectPrivilegeMode.READ, ownerRoleId);
			if (jobMst == null) {
				JobMasterNotFound je = new JobMasterNotFound("JobMstEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.info("getJobMstPK_OR() : "
						+ je.getClass().getSimpleName() + ", " + je.getMessage());
				je.setJobunitId(pk.getJobunitId());
				je.setJobId(pk.getJobId());
				throw je;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getJobMstPK_OR() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return jobMst;
	}

	public static List<JobMstEntity> getJobMstEnityFindByJobunitId(
			String jobunitId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("JobMstEntity.findByJobunitId", JobMstEntity.class)
						.setParameter("jobunitId", jobunitId).getResultList();
		}
	}

	public static List<JobMstEntity> getJobMstEnityFindByIconId(
			String iconId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("JobMstEntity.findByIconId", JobMstEntity.class)
					.setParameter("iconId", iconId).getResultList();
		}
	}

	public static List<JobMstEntity> getJobMstEnityFindByParentId(String parentJobunitId, String parentJobId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("JobMstEntity.findByParentJobunitIdAndJobId", JobMstEntity.class)
						.setParameter("parentJobunitId", parentJobunitId)
						.setParameter("parentJobId", parentJobId)
						.getResultList();
		}
	}

	public static List<JobInfoEntity> getJobInfoEnityFindByIconId(
			String iconId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("JobInfoEntity.findByIconId", JobInfoEntity.class)
						.setParameter("iconId", iconId).getResultList();
		}
	}

	public static JobInfoEntity getJobInfoEntityPK(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, InvalidRole {

		//セッションIDとジョブIDから、セッションジョブを取得
		JobInfoEntityPK pk = new JobInfoEntityPK(sessionId, jobunitId, jobId);
		JobInfoEntity jobInfo = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			jobInfo = em.find(JobInfoEntity.class, pk, ObjectPrivilegeMode.READ);
			if (jobInfo == null) {
				JobInfoNotFound je = new JobInfoNotFound("JobInfoEntity.findByPrimaryKey"
						+ ", " + pk.toString());
				m_log.info("getJobInfoEntityPK() : "
						+ je.getClass().getSimpleName() + ", " + je.getMessage());
				je.setSessionId(sessionId);
				je.setJobunitId(jobunitId);
				je.setJobId(jobId);
				throw je;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getJobSessionJobPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return jobInfo;
	}
	
	public static JobSessionEntity getJobSessionPK(String sessionId) throws JobInfoNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			JobSessionEntity session = em.find(JobSessionEntity.class, sessionId, ObjectPrivilegeMode.READ);
			if (session == null) {
				JobInfoNotFound je = new JobInfoNotFound("JobSessionEntity.findByPrimaryKey"
						+ ", sessionId = " + sessionId);
				m_log.info("getJobSessionPK() : "
						+ je.getClass().getSimpleName() + ", " + je.getMessage());
				je.setSessionId(sessionId);
				throw je;
			}
			return session;
		}
	}

	public static JobSessionJobEntity getJobSessionJobPK(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, InvalidRole {
		return getJobSessionJobPK(sessionId, jobunitId, jobId, ObjectPrivilegeMode.READ);
	}

	public static JobSessionJobEntity getJobSessionJobPK(String sessionId, String jobunitId, String jobId, ObjectPrivilegeMode mode)
			throws JobInfoNotFound, InvalidRole {

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntityPK sessionJobPk = new JobSessionJobEntityPK(sessionId, jobunitId, jobId);
		JobSessionJobEntity sessionJob;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			sessionJob = em.find(JobSessionJobEntity.class, sessionJobPk, mode);
			if (sessionJob == null) {
				JobInfoNotFound je = new JobInfoNotFound("JobSessionJobEntity.findByPrimaryKey"
						+ ", " + sessionJobPk.toString());
				m_log.info("getJobSessionJobPK() : "
						+ je.getClass().getSimpleName() + ", " + je.getMessage());
				je.setSessionId(sessionId);
				je.setJobunitId(jobunitId);
				je.setJobId(jobId);
				throw je;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getJobSessionJobPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return sessionJob;
	}

	public static JobSessionNodeEntity getJobSessionNodePK(String sessionId, String jobunitId, String jobId, String facilityId) throws JobInfoNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			//セッションノード取得
			JobSessionNodeEntityPK sessionNodePk
			= new JobSessionNodeEntityPK(sessionId, jobunitId, jobId, facilityId);
			JobSessionNodeEntity sessionNode = em.find(JobSessionNodeEntity.class, sessionNodePk, ObjectPrivilegeMode.READ);
			if (sessionNode == null) {
				JobInfoNotFound je = new JobInfoNotFound("JobSessionNodeEntity.findByPrimaryKey"
						+ ", " + sessionNodePk.toString());
				m_log.info("endNodeSetStatus() : "
						+ je.getClass().getSimpleName() + ", " + je.getMessage());
				je.setSessionId(sessionId);
				je.setJobunitId(jobunitId);
				je.setJobId(jobId);
				je.setFacilityId(facilityId);
				throw je;
			}
			return sessionNode;
		}
	}

	public static List<JobMstEntity> getJobMstEntityFindByCalendarId(String calendarId){
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			//ジョブ
			List<JobMstEntity> jobMstList
			= em.createNamedQuery("JobMstEntity.findByCalendarId", JobMstEntity.class)
			.setParameter("calendarId", calendarId).getResultList();
			return jobMstList;
		}
	}

	public static List<JobMstEntity> getJobMstEntityFindByMonitorId_NONE(String monitorId){
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			//ジョブ
			List<JobMstEntity> jobMstList
			= em.createNamedQuery("JobMstEntity.findByMonitorId", JobMstEntity.class, ObjectPrivilegeMode.NONE)
			.setParameter("monitorId", monitorId).getResultList();
			return jobMstList;
		}
	}

	public static List<JobMstEntity> getJobMstEntityFindByOwnerRoleId_NONE(String roleId){
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			//ジョブ
			List<JobMstEntity> jobMstList
			= em.createNamedQuery("JobMstEntity.findByOwnerRoleId", JobMstEntity.class, ObjectPrivilegeMode.NONE)
			.setParameter("ownerRoleId", roleId).getResultList();
			return jobMstList;
		}
	}

	public static List<JobParamMstEntity> getJobParamMstEntityFindByJobunitIdParamId(String jobunitId, String paramId){
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// ジョブユニットIDを条件としてジョブ変数を取得
			List<JobParamMstEntity> jobParamMstEntities
				= em.createNamedQuery("JobParamMstEntity.findByJobunitIdParamId", JobParamMstEntity.class)
				.setParameter("jobunitId", jobunitId)
				.setParameter("paramId", paramId)
				.getResultList();
			return jobParamMstEntities;
		}
	}

	public static List<JobSessionJobEntity> getAllChildJobSessionJob(String sessionId, String jobunitId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			//ジョブ
			List<JobSessionJobEntity> jobSessionJobList
			= em.createNamedQuery("JobSessionJobEntity.findAllChild", JobSessionJobEntity.class)
			.setParameter("sessionId", sessionId)
			.setParameter("jobunitId", jobunitId)
			.getResultList();
			return jobSessionJobList;
		}
	}

	public static List<JobSessionJobEntity> getChildJobSessionJob(String sessionId, String parentJobunitId, String parentJobId){
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			//ジョブ
			List<JobSessionJobEntity> jobSessionJobList
			= em.createNamedQuery("JobSessionJobEntity.findChild", JobSessionJobEntity.class)
			.setParameter("sessionId", sessionId)
			.setParameter("parentJobunitId", parentJobunitId)
			.setParameter("parentJobId", parentJobId).getResultList();
			return jobSessionJobList;
		}
	}

	public static List<JobSessionJobEntity> getJobSessionJobByParentStatus(String sessionId, String parentJobunitId, String parentJobId, int status){
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			//ジョブ
			List<JobSessionJobEntity> jobSessionJobList
			= em.createNamedQuery("JobSessionJobEntity.findByParentStatus", JobSessionJobEntity.class)
			.setParameter("sessionId", sessionId)
			.setParameter("parentJobunitId", parentJobunitId)
			.setParameter("parentJobId", parentJobId)
			.setParameter("status", status).getResultList();
			if (jobSessionJobList == null) jobSessionJobList = new ArrayList<>();
			return jobSessionJobList;
		}
	}

	public static List<JobSessionJobEntity> getJobSessionJobBySessionId(String sessionId){
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			//ジョブ
			List<JobSessionJobEntity> jobSessionJobList
			= em.createNamedQuery("JobSessionJobEntity.findByJobSessionId", JobSessionJobEntity.class)
			.setParameter("sessionId", sessionId).getResultList();
			return jobSessionJobList;
		}
	}
	
	public static List<JobSessionJobEntity> getJobSessionJobByJobunitIdJobIdEndDate(String jobunitId, String jobId, Long endDate){
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			//ジョブ
			List<JobSessionJobEntity> jobSessionJobList
			= em.createNamedQuery("JobSessionJobEntity.findByJobunitIdJobIdEndDate", JobSessionJobEntity.class)
			.setParameter("jobunitId", jobunitId)
			.setParameter("jobId", jobId)
			.setParameter("endDate", endDate)
			.getResultList();
			return jobSessionJobList;
		}
	}

	public static List<JobSessionJobEntity> getChildJobSessionJobOrderByStartDate(String sessionId, String parentJobunitId, String parentJobId){
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			//ジョブ
			List<JobSessionJobEntity> jobSessionJobList
			= em.createNamedQuery("JobSessionJobEntity.findByStartDate", JobSessionJobEntity.class)
			.setParameter("sessionId", sessionId)
			.setParameter("parentJobunitId", parentJobunitId)
			.setParameter("parentJobId", parentJobId).getResultList();
			return jobSessionJobList;
		}
	}

	public static List<JobSessionJobEntity> getJobSessionJobByIdsDesc(String jobunitId, String jobId, int count, List<Integer> statusList){
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			//ジョブ
			List<JobSessionJobEntity> jobSessionJobList
			= em.createNamedQuery("JobSessionJobEntity.findByIdsDesc", JobSessionJobEntity.class)
			.setParameter("jobunitId", jobunitId)
			.setParameter("jobId", jobId)
			.setParameter("statusList", statusList)
			.setMaxResults(count)
			.getResultList();
			
			return jobSessionJobList;
		}
	}

	public static JobKickEntity getJobKickPK(String jobkickId, ObjectPrivilegeMode mode) throws JobInfoNotFound, InvalidRole {
		JobKickEntity entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity  = em.find(JobKickEntity.class, jobkickId, mode);
			if (entity == null) {
				JobInfoNotFound je = new JobInfoNotFound("JobKickEntity.findByPrimaryKey"
						+ jobkickId);
				m_log.info("getJobKickPK() : "
						+ je.getClass().getSimpleName() + ", " + je.getMessage());
				throw je;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getJobKickPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static List<JobKickEntity> getJobKickEntityFindByCalendarId_NONE(String calendarId){
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			//ジョブ実行契機
			List<JobKickEntity> jobKickList
			= em.createNamedQuery("JobKickEntity.findByCalendarId", JobKickEntity.class, ObjectPrivilegeMode.NONE)
			.setParameter("calendarId", calendarId).getResultList();
			return jobKickList;
		}
	}

	public static List<JobKickEntity> getJobKickEntityFindByOwnerRoleId_NONE(String roleId){
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			//ジョブ実行契機
			List<JobKickEntity> jobScheduleList
			= em.createNamedQuery("JobKickEntity.findByOwnerRoleId", JobKickEntity.class, ObjectPrivilegeMode.NONE)
			.setParameter("ownerRoleId", roleId).getResultList();
			return jobScheduleList;
		}
	}

	public static List<JobKickEntity> getJobKickEntityFindByJobKickType(Integer jobkickType){
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			//ジョブ実行契機
			List<JobKickEntity> jobScheduleList
			= em.createNamedQuery("JobKickEntity.findByJobKickType", JobKickEntity.class)
			.setParameter("jobkickType", jobkickType).getResultList();
			return jobScheduleList;
		}
	}

	public static List<JobKickEntity> getJobKickEntityFindByFilter(
			String jobkickId,
			String jobkickName,
			Integer jobkickType,
			String jobunitId,
			String jobId,
			String calendarId,
			Boolean validFlg,
			String regUser,
			Long regFromDate,
			Long regToDate,
			String updateUser,
			Long updateFromDate,
			Long updateToDate,
			String ownerRoleId){
		
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// 「含まない」検索を行うかの判断に使う値
			String notInclude = "NOT:";

			// 検索条件の組み立て
			StringBuffer sbJpql = new StringBuffer();
			sbJpql.append("SELECT a FROM JobKickEntity a WHERE true = true");
			// jobkickId
			if(jobkickId != null && !"".equals(jobkickId)) {
				if(!jobkickId.startsWith(notInclude)) {
					sbJpql.append(" AND a.jobkickId like :jobkickId");
				}else{
					sbJpql.append(" AND a.jobkickId not like :jobkickId");
				}
			}
			// jobkickName
			if(jobkickName != null && !"".equals(jobkickName)) {
				if(!jobkickName.startsWith(notInclude)) {
					sbJpql.append(" AND a.jobkickName like :jobkickName");
				}else{
					sbJpql.append(" AND a.jobkickName not like :jobkickName");
				}
			}
			// jobkickType
			if(jobkickType != null) {
				sbJpql.append(" AND a.jobkickType = :jobkickType");
			}
			// jobunitId
			if(jobunitId != null && !"".equals(jobunitId)) {
				if(!jobunitId.startsWith(notInclude)) {
					sbJpql.append(" AND a.jobunitId like :jobunitId");
				}else{
					sbJpql.append(" AND a.jobunitId not like :jobunitId");
				}
			}
			// jobId
			if(jobId != null && !"".equals(jobId)) {
				if(!jobId.startsWith(notInclude)) {
					sbJpql.append(" AND a.jobId like :jobId");
				}else{
					sbJpql.append(" AND a.jobId not like :jobId");
				}
			}
			// calendarId
			if(calendarId != null && !"".equals(calendarId)) {
				sbJpql.append(" AND a.calendarId like :calendarId");
			}
			// validFlg
			if(validFlg != null) {
				sbJpql.append(" AND a.validFlg = :validFlg");
			}
			// regUser
			if(regUser != null && !"".equals(regUser)) {
				if(!regUser.startsWith(notInclude)) {
					sbJpql.append(" AND a.regUser like :regUser");
				}else{
					sbJpql.append(" AND a.regUser not like :regUser");
				}
			}
			// regFromDate
			if (regFromDate > 0) {
				sbJpql.append(" AND a.regDate >= :regFromDate");
			}
			// regToDate
			if (regToDate > 0){
				sbJpql.append(" AND a.regDate <= :regToDate");
			}
			// updateUser
			if(updateUser != null && !"".equals(updateUser)) {
				if(!updateUser.startsWith(notInclude)) {
					sbJpql.append(" AND a.updateUser like :updateUser");
				}else{
					sbJpql.append(" AND a.updateUser not like :updateUser");
				}
			}
			// updateFromDate
			if(updateFromDate > 0) {
				sbJpql.append(" AND a.updateDate >= :updateFromDate");
			}
			// updateToDate
			if(updateToDate > 0) {
				sbJpql.append(" AND a.updateDate <= :updateToDate");
			}
			// ownerRoleId
			if(ownerRoleId != null && !"".equals(ownerRoleId)) {
				if(!ownerRoleId.startsWith(notInclude)) {
					sbJpql.append(" AND a.ownerRoleId like :ownerRoleId");
				}else{
					sbJpql.append(" AND a.ownerRoleId not like :ownerRoleId");
				}
			}
			TypedQuery<JobKickEntity> typedQuery = em.createQuery(sbJpql.toString(), JobKickEntity.class);

			
			// jobkickId
			if(jobkickId != null && !"".equals(jobkickId)) {
				if(!jobkickId.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("jobkickId",
							QueryDivergence.escapeLikeCondition(jobkickId));
				}else{
					typedQuery = typedQuery.setParameter("jobkickId",
							QueryDivergence.escapeLikeCondition(jobkickId.substring(notInclude.length())));
				}
			}
			// jobkickName
			if(jobkickName != null && !"".equals(jobkickName)) {
				if(!jobkickName.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("jobkickName",
							QueryDivergence.escapeLikeCondition(jobkickName));
				}else{
					typedQuery = typedQuery.setParameter("jobkickName",
							QueryDivergence.escapeLikeCondition(jobkickName.substring(notInclude.length())));
				}
			}
			// jobkickType
			if(jobkickType != null) {
				typedQuery = typedQuery.setParameter("jobkickType", jobkickType);
			}
			// jobunitId
			if(jobunitId != null && !"".equals(jobunitId)) {
				if(!jobunitId.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("jobunitId",
							QueryDivergence.escapeLikeCondition(jobunitId));
				}else{
					typedQuery = typedQuery.setParameter("jobunitId",
							QueryDivergence.escapeLikeCondition(jobunitId.substring(notInclude.length())));
				}
			}
			// jobId
			if(jobId != null && !"".equals(jobId)) {
				if(!jobId.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("jobId",
							QueryDivergence.escapeLikeCondition(jobId));
				}else{
					typedQuery = typedQuery.setParameter("jobId",
							QueryDivergence.escapeLikeCondition(jobId.substring(notInclude.length())));
				}
			}
			// calendarId
			if(calendarId != null && !"".equals(calendarId)) {
				typedQuery = typedQuery.setParameter("calendarId", calendarId);
			}
			// validFlg
			if(validFlg != null) {
				typedQuery = typedQuery.setParameter("validFlg", validFlg);
			}
			// regUser
			if(regUser != null && !"".equals(regUser)) {
				if(!regUser.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("regUser",
							QueryDivergence.escapeLikeCondition(regUser));
				}else{
					typedQuery = typedQuery.setParameter("regUser",
							QueryDivergence.escapeLikeCondition(regUser.substring(notInclude.length())));
				}
			}
			// regFromDate
			if (regFromDate > 0) {
				typedQuery = typedQuery.setParameter("regFromDate", regFromDate);
			}
			// regToDate
			if (regToDate > 0){
				typedQuery = typedQuery.setParameter("regToDate", regToDate);
			}
			// updateUser
			if(updateUser != null && !"".equals(updateUser)) {
				if(!updateUser.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("updateUser",
							QueryDivergence.escapeLikeCondition(updateUser));
				}else{
					typedQuery = typedQuery.setParameter("updateUser",
							QueryDivergence.escapeLikeCondition(updateUser.substring(notInclude.length())));
				}
			}
			// updateFromDate
			if(updateFromDate > 0) {
				typedQuery = typedQuery.setParameter("updateFromDate", updateFromDate);
			}
			// updateToDate
			if(updateToDate > 0) {
				typedQuery = typedQuery.setParameter("updateToDate", updateToDate);
			}
			// ownerRoleId
			if(ownerRoleId != null && !"".equals(ownerRoleId)) {
				if(!ownerRoleId.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("ownerRoleId",
							QueryDivergence.escapeLikeCondition(ownerRoleId));
				}else{
					typedQuery = typedQuery.setParameter("ownerRoleId",
							QueryDivergence.escapeLikeCondition(ownerRoleId.substring(notInclude.length())));
				}
			}
			return typedQuery.getResultList();
		}
	}

	public static JobmapIconImageEntity getJobmapIconImagePK(String filename, ObjectPrivilegeMode mode) throws IconFileNotFound, InvalidRole {
		JobmapIconImageEntity jobmapIconImageEntity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			jobmapIconImageEntity = em.find(JobmapIconImageEntity.class, filename, mode);
			if (jobmapIconImageEntity == null) {
				IconFileNotFound e = new IconFileNotFound("JobmapIconImageEntity.findByPrimaryKey"
						+ filename);
				m_log.info("getJobmapIconImagePK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getJobmapIconImagePK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return jobmapIconImageEntity;
	}

	public static List<JobmapIconImageEntity> getJobmapIconImageEntities() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("JobmapIconImageEntity.findAll", JobmapIconImageEntity.class)
					.getResultList();
		}
	}

	public static List<String> getJobmapIconImageIdExceptDefaultList_OR(List<String> defaultIconList, String orderRoleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			StringBuffer sbJpql = new StringBuffer();
			sbJpql.append("SELECT a.iconId FROM JobmapIconImageEntity a");
			if (defaultIconList != null && defaultIconList.size() > 0) {
				sbJpql.append(" WHERE a.iconId NOT IN (" + HinemosEntityManager.getParamNameString("defaultIconId", defaultIconList.toArray()) + ")");
			}
			sbJpql.append(" ORDER BY a.iconId");
			TypedQuery<String> typedQuery = em.createQuery_OR(sbJpql.toString(), String.class, JobmapIconImageEntity.class, orderRoleId);
			if (defaultIconList != null && defaultIconList.size() > 0) {
				typedQuery = HinemosEntityManager.appendParam(typedQuery, "defaultIconId", defaultIconList.toArray());
			}
			return typedQuery.getResultList();
		}
	}
	public static List<MonitorInfo> getMonitorInfoByMonitorTypeIds_OR(List<String> monitorTypeIds, String ownerRoleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			StringBuffer sbJpql = new StringBuffer();
			sbJpql.append("SELECT a FROM MonitorInfo a");
			if (monitorTypeIds != null && monitorTypeIds.size() > 0) {
				sbJpql.append(" WHERE a.monitorTypeId IN (" + HinemosEntityManager.getParamNameString("monitorTypeId", monitorTypeIds.toArray()) + ")");
			}
			sbJpql.append(" ORDER BY a.monitorId");
			TypedQuery<MonitorInfo> typedQuery
				= em.createQuery_OR(sbJpql.toString(), MonitorInfo.class, ownerRoleId);
			if (monitorTypeIds != null && monitorTypeIds.size() > 0) {
				typedQuery = HinemosEntityManager.appendParam(typedQuery, "monitorTypeId", monitorTypeIds.toArray());
			}
			return typedQuery.getResultList();
		}
	}

	public static List<JobSessionNodeEntity> getJobSessionNodeEntityFindByJobTypeEndIsNull_NONE(int jobType){
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<JobSessionNodeEntity> jobSessionNodeEntities
				= em.createNamedQuery("JobSessionNodeEntity.findByJobTypeEndIsNull", JobSessionNodeEntity.class, ObjectPrivilegeMode.NONE)
				.setParameter("jobType", jobType)
				.getResultList();
			return jobSessionNodeEntities;
		}
	}
	
	public static List<JobNextJobOrderMstEntity> getJobNextJobOrderMstEntityFindByJobunitIdJobId(String jobunitId, String jobId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<JobNextJobOrderMstEntity> jobNextJobOrderMstEntities
				= em.createNamedQuery("JobNextJobOrderMstEntity.findByJobunitIdJobId", JobNextJobOrderMstEntity.class)
				.setParameter("jobunitId", jobunitId)
				.setParameter("jobId", jobId)
				.getResultList();
			return jobNextJobOrderMstEntities;
		}
	}

	public static List<JobNextJobOrderInfoEntity> getJobNextJobOrderInfoEntityFindBySessionIdJobunitIdJobId(String sessionId, String jobunitId, String jobId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<JobNextJobOrderInfoEntity> jobNextJobOrderInfoEntities
				= em.createNamedQuery("JobNextJobOrderInfoEntity.findBySessionIdJobunitIdJobId", JobNextJobOrderInfoEntity.class)
				.setParameter("sessionId", sessionId)
				.setParameter("jobunitId", jobunitId)
				.setParameter("jobId", jobId)
				.getResultList();
			return jobNextJobOrderInfoEntities;
		}
	}

	public static List<JobSessionEntity> getJobSessionByTriggerInfo(
			String triggerInfo) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("JobSessionEntity.findByTriggerInfo", JobSessionEntity.class)
					.setParameter("triggerInfo", triggerInfo).getResultList();
		}
	}

	/**
	 * 指定された実行契機ID、実行予定日時に一致するジョブセッション情報を取得する
	 * 
	 * @param scheduleDateTime 実行予定日時(yyyyMMddHHmmss)
	 * @param jobkickId 実行契機ID
	 * @param status ステータス
	 * @return ジョブセッション一覧
	 */
	public static List<JobSessionEntity> getJobSessionListBySessionIdJobkickIdAndStatus(
			String scheduleDateTime, String jobkickId, Integer status) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("JobSessionEntity.findBySessionIdJobkickIdAndStatus", JobSessionEntity.class)
					.setParameter("sessionGenerateJobkickId", jobkickId)
					.setParameter("sessionId", scheduleDateTime + "%")
					.setParameter("parentJobunitId", CreateJobSession.TOP_JOBUNIT_ID)
					.setParameter("parentJobId", CreateJobSession.TOP_JOB_ID)
					.setParameter("status", status)
					.getResultList();
		}
	}

	/**
	 * 指定された実行契機ID、実行予定日時に一致するジョブセッション情報を取得する
	 * 
	 * @param scheduleDateTime 実行予定日時(yyyyMMddHHmmss)
	 * @param jobkickId 実行契機ID
	 * @param status ステータス
	 */
	public static List<JobSessionEntity> getJobSessionListBySessionIdJobkickId(
			String scheduleDateTime, String jobkickId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("JobSessionEntity.findBySessionIdJobkickId", JobSessionEntity.class)
					.setParameter("sessionGenerateJobkickId", jobkickId)
					.setParameter("sessionId", scheduleDateTime + "%")
					.setParameter("parentJobunitId", CreateJobSession.TOP_JOBUNIT_ID)
					.setParameter("parentJobId", CreateJobSession.TOP_JOB_ID)
					.getResultList();
		}
	}

	/**
	 * 事前生成されているジョブセッションを取得する
	 * 
	 * @param jobkickId 実行契機ID
	 * @param status ステータス
	 * @return ジョブセッション一覧
	 */
	public static List<JobSessionEntity> getJobSessionListByJobkickIdAndStatus(String jobkickId, int status, Integer timeout)
			throws HinemosDbTimeout {
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("sessionGenerateJobkickId", jobkickId);
		parameters.put("parentJobunitId", CreateJobSession.TOP_JOBUNIT_ID);
		parameters.put("parentJobId", CreateJobSession.TOP_JOB_ID);
		parameters.put("status", status);
		return QueryExecutor.getListByQueryNameWithTimeout(
				"JobSessionEntity.findByJobkickIdAndStatus", JobSessionEntity.class, parameters, timeout, ObjectPrivilegeMode.NONE);
	}

	/**
	 * 指定された日時以前に実行予定のジョブセッションを取得する
	 * 
	 * @param targetDate 対象日時
	 * @param status ステータス
	 * @return ジョブセッション一覧
	 */
	public static List<JobSessionEntity> getJobSessionListByDateAndStatus(Long targetDate, int status) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("JobSessionEntity.findByScheduleDateAndStatus",
					JobSessionEntity.class, ObjectPrivilegeMode.NONE)
					.setParameter("scheduleDate", targetDate)
					.setParameter("parentJobunitId", CreateJobSession.TOP_JOBUNIT_ID)
					.setParameter("parentJobId", CreateJobSession.TOP_JOB_ID)
					.setParameter("status", status)
					.getResultList();
		}
	}

	/**
	 * 指定された日時以前に実行予定のジョブセッションを取得する
	 * 
	 * @param targetDate 対象日時
	 * @param status ステータス
	 * @param ownerRoleId オーナーロールID
	 * @return ジョブセッション一覧
	 */
	public static List<JobSessionEntity> getJobSessionListDateStatusAndOwnerRoleId(
			Long targetDate, int status, String ownerRoleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("JobSessionEntity.findByScheduleDateStatusAndOwnerRoleId",
					JobSessionEntity.class, ObjectPrivilegeMode.NONE)
					.setParameter("scheduleDate", targetDate)
					.setParameter("parentJobunitId", CreateJobSession.TOP_JOBUNIT_ID)
					.setParameter("parentJobId", CreateJobSession.TOP_JOB_ID)
					.setParameter("ownerRoleId", ownerRoleId)
					.setParameter("status", status)
					.getResultList();
		}
	}

	/**
	 * 指定されたジョブセッションにおいて、指定されたジョブの終了が条件(セッション横断ジョブは除く)となっている
	 * {@link JobWaitInfoEntity}のリストを返します。
	 */
	public static List<JobWaitGroupInfoEntity> getJobWaitGroupInfoByTargetJobId(String sessionId, String targetJobId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("JobWaitGroupInfoEntity.findByTargetJobId", JobWaitGroupInfoEntity.class)
					.setParameter("sessionId", sessionId)
					.setParameter("targetJobId", targetJobId)
					.setParameter("excludingTypes", Arrays.asList(
							JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS,
							JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE))
					.getResultList();
		}
	}

	/**
	 * 指定されたジョブセッション、ジョブの指定された種別の
	 * {@link JobWaitInfoEntity}のリストを返します。
	 * 
	 * @param sessionId セッションID
	 * @param targetJobunitId ジョブユニットID
	 * @param targetJobId ジョブID
	 * @param type 対象種別
	 * @return 待ち条件一覧
	 */
	public static List<JobWaitInfoEntity> getJobWaitInfoByTypeJobId(
			String sessionId, String jobunitId, String jobId, Integer type) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("JobWaitInfoEntity.findByTypeJobId", JobWaitInfoEntity.class)
					.setParameter("sessionId", sessionId)
					.setParameter("jobunitId", jobunitId)
					.setParameter("jobId", jobId)
					.setParameter("type", type)
					.getResultList();
		}
	}

	public static List<JobRpaScreenshotEntity> getJobRpaScreenshot(String sessionId, String jobunitId,
			String jobId, String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("JobRpaScreenshotEntity.findBySessionIdJobunitIdJobIdFacilityId", JobRpaScreenshotEntity.class)
					.setParameter("sessionId", sessionId)
					.setParameter("jobunitId", jobunitId)
					.setParameter("jobId", jobId)
					.setParameter("facilityId", facilityId)
					.getResultList();
		}
	}
	
	/**
	 * RPAシナリオジョブ（直接実行）のログイン解像度一覧を返します。
	 * @return ログイン解像度のリスト
	 */
	public static List<JobRpaLoginResolutionMstEntity> getJobRpaLoginResolution() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("JobRpaLoginResolutionMstEntity.findAll", JobRpaLoginResolutionMstEntity.class)
					.getResultList();
		}
	}
	
	/**
	 * RPAシナリオジョブ（直接実行）のログイン解像度を検索します。
	 * @return ログイン解像度
	 */
	public static JobRpaLoginResolutionMstEntity getJobRpaLoginResolutionFindByPK(String resolution) throws JobMasterNotFound {
		JobRpaLoginResolutionMstEntity entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find(JobRpaLoginResolutionMstEntity.class, resolution, ObjectPrivilegeMode.NONE);
			if (entity == null) {
				throw new JobMasterNotFound("JobRpaLoginResolutionMstEntity.findByPrimaryKey resolution=" + resolution);
			}
		}
		return entity;
	}
	
	/**
	 * RPAシナリオジョブの実行状態を返します。
	 * @param sessionId
	 * @param jobunitId
	 * @param jobId
	 * @param facilityId
	 * @return RPAシナリオジョブの実行状態
	 */
	public static JobRpaRunConditionEntity getJobRpaRunConditionPK(String sessionId, String jobunitId,
			String jobId, String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("JobRpaRunConditionEntity.findBySessionIdJobunitIdJobIdFacilityId", JobRpaRunConditionEntity.class)
					.setParameter("sessionId", sessionId)
					.setParameter("jobunitId", jobunitId)
					.setParameter("jobId", jobId)
					.setParameter("facilityId", facilityId)
					.getSingleResult();
		}
	}
	
	/**
	 * RPAシナリオジョブの実行状態を返します。
	 * @return RPAシナリオジョブの実行状態のリスト
	 */
	public static List<JobRpaRunConditionEntity> getJobRpaRunCondition() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("JobRpaRunConditionEntity.findAll", JobRpaRunConditionEntity.class)
					.getResultList();
		}
	}

	/**
	 * ジョブ連携送信設定の取得
	 * 
	 * @param joblinkSendSettingId ジョブ連携送信設定ID
	 * @param mode モード
	 * @return ジョブ連携送信設定
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 */
	public static JobLinkSendSettingEntity getJobLinkSendSettingPK(String joblinkSendSettingId, ObjectPrivilegeMode mode)
			throws JobMasterNotFound, InvalidRole {
		JobLinkSendSettingEntity entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find(JobLinkSendSettingEntity.class, joblinkSendSettingId, mode);
			if (entity == null) {
				JobMasterNotFound je = new JobMasterNotFound("JobLinkSendSettingEntity.findByPrimaryKey "
						+ joblinkSendSettingId);
				m_log.info("getJobLinkSendSettingPK() : "
						+ je.getClass().getSimpleName() + ", " + je.getMessage());
				throw je;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getJobLinkSendSettingPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	/**
	 * ジョブ連携送信設定の取得
	 * 
	 * @param joblinkSendSettingId ジョブ連携送信設定ID
	 * @param ownerRoleId オーナーロールID
	 * @return ジョブ連携送信設定
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 */
	public static JobLinkSendSettingEntity getJobLinkSendSettingPK_OR(String joblinkSendSettingId, String ownerRoleId)
			throws JobMasterNotFound, InvalidRole {
		JobLinkSendSettingEntity entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find_OR(JobLinkSendSettingEntity.class, joblinkSendSettingId, ownerRoleId);
			if (entity == null) {
				JobMasterNotFound je = new JobMasterNotFound("JobLinkSendSettingEntity.findByPrimaryKey "
						+ joblinkSendSettingId);
				m_log.info("getJobLinkSendSettingPK_OR() : "
						+ je.getClass().getSimpleName() + ", " + je.getMessage());
				throw je;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getJobLinkSendSettingPK_OR() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	/**
	 * ジョブ連携送信設定一覧の取得
	 * 
	 * @return ジョブ連携送信設定一覧
	 */
	public static List<JobLinkSendSettingEntity> getAllJobLinkSendSettingList() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<JobLinkSendSettingEntity> list
				= em.createNamedQuery("JobLinkSendSettingEntity.findAll", JobLinkSendSettingEntity.class).getResultList();
			return list;
		}
	}

	/**
	 * ジョブ連携送信設定一覧の取得
	 * 
	 * オーナーロールIDが参照権限を持つジョブ連携送信設定一覧を取得する
	 * 
	 * @param ownerRoleId オーナーロールID
	 * 
	 * @return ジョブ連携送信設定一覧
	 */
	public static List<JobLinkSendSettingEntity> getAllJobLinkSendSettingList_OR(String ownerRoleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<JobLinkSendSettingEntity> list
				= em.createNamedQuery_OR("JobLinkSendSettingEntity.findAll", JobLinkSendSettingEntity.class, ownerRoleId)
			.getResultList();
			return list;
		}
	}

	/**
	 * ジョブ連携メッセージ検索処理
	 * ジョブ連携受信実行契機で使用
	 * 
	 * @param jobKickEntity ジョブ実行契機情報
	 * @param acceptDateFrom 受信日時From
	 * @return ジョブ連携メッセージ
	 */
	public static JobLinkMessageEntity getJobLinkMessage(
			JobKickEntity jobKickEntity,
			Long acceptDateFrom) throws HinemosUnknown {

		// ジョブ連携受信実行契機以外は対象外
		if (jobKickEntity.getJobkickType() != JobKickConstant.TYPE_JOBLINKRCV) {
			return null;
		}
		HashMap<String, String> expMap = new HashMap<>();

		boolean infoValidFlg = false;
		boolean warnValidFlg = false;
		boolean criticalValidFlg = false;
		boolean unknownValidFlg = false;
		String application = null;
		String monitorDetailId = null;
		String message = null;

		if (jobKickEntity.getInfoValidFlg() != null) {
			infoValidFlg = jobKickEntity.getInfoValidFlg();
		}
		if (jobKickEntity.getWarnValidFlg() != null) {
			warnValidFlg = jobKickEntity.getWarnValidFlg();
		}
		if (jobKickEntity.getCriticalValidFlg() != null) {
			criticalValidFlg = jobKickEntity.getCriticalValidFlg();
		}
		if (jobKickEntity.getUnknownValidFlg() != null) {
			unknownValidFlg = jobKickEntity.getUnknownValidFlg();
		}
		if (jobKickEntity.getApplicationFlg()) {
			application = jobKickEntity.getApplication();
		}
		if (jobKickEntity.getMonitorDetailIdFlg()) {
			monitorDetailId = jobKickEntity.getMonitorDetailId();
		}
		if (jobKickEntity.getMessageFlg()) {
			message = jobKickEntity.getMessage();
		}

		if (jobKickEntity.getExpFlg() != null
			&& jobKickEntity.getExpFlg()
			&& jobKickEntity.getJobLinkJobkickExpInfoEntities() != null) {
			for (JobLinkJobkickExpInfoEntity expEntity : jobKickEntity.getJobLinkJobkickExpInfoEntities()) {
				expMap.put(expEntity.getId().getKey(), expEntity.getValue());
			}
		}
		return	getJobLinkMessage(
				jobKickEntity.getFacilityId(),
				jobKickEntity.getJoblinkMessageId(),
				infoValidFlg,
				warnValidFlg,
				criticalValidFlg,
				unknownValidFlg,
				application,
				monitorDetailId,
				message,
				expMap,
				acceptDateFrom,
				jobKickEntity.getOwnerRoleId(),
				jobKickEntity.getJoblinkRcvCheckedPosition());
	}

	/**
	 * ジョブ連携メッセージ検索処理
	 * ジョブ連携待機ジョブで使用
	 * 
	 * @param facilityId 送信元スコープ
	 * @param jobSessionJobEntity ジョブセッション情報
	 * @param acceptDateFrom 受信日時From
	 * @return ジョブ連携メッセージ
	 */
	public static JobLinkMessageEntity getJobLinkMessage(
			String facilityId,
			JobSessionJobEntity jobSessionJobEntity,
			Long acceptDateFrom) throws HinemosUnknown {

		if (jobSessionJobEntity == null) {
			return null;
		}

		JobInfoEntity jobInfoEntity = jobSessionJobEntity.getJobInfoEntity();

		if (jobInfoEntity.getJobType() != JobConstant.TYPE_JOBLINKRCVJOB) {
			return null;
		}
		HashMap<String, String> expMap = new HashMap<>();

		boolean infoValidFlg = false;
		boolean warnValidFlg = false;
		boolean criticalValidFlg = false;
		boolean unknownValidFlg = false;
		String application = null;
		String monitorDetailId = null;
		String message = null;

		if (jobInfoEntity.getInfoValidFlg() != null) {
			infoValidFlg = jobInfoEntity.getInfoValidFlg();
		}
		if (jobInfoEntity.getWarnValidFlg() != null) {
			warnValidFlg = jobInfoEntity.getWarnValidFlg();
		}
		if (jobInfoEntity.getCriticalValidFlg() != null) {
			criticalValidFlg = jobInfoEntity.getCriticalValidFlg();
		}
		if (jobInfoEntity.getUnknownValidFlg() != null) {
			unknownValidFlg = jobInfoEntity.getUnknownValidFlg();
		}
		if (jobInfoEntity.getApplicationFlg()) {
			application = jobInfoEntity.getApplication();
		}
		if (jobInfoEntity.getMonitorDetailIdFlg()) {
			monitorDetailId = jobInfoEntity.getMonitorDetailId();
		}
		if (jobInfoEntity.getMessageFlg()) {
			message = jobInfoEntity.getMessage();
		}

		if (jobInfoEntity.getExpFlg() != null
			&& jobInfoEntity.getExpFlg()
			&& jobInfoEntity.getJobLinkJobExpInfoEntities() != null) {
			for (JobLinkJobExpInfoEntity expEntity : jobInfoEntity.getJobLinkJobExpInfoEntities()) {
				expMap.put(expEntity.getId().getKey(), expEntity.getValue());
			}
		}
		return	getJobLinkMessage(
					facilityId,
					jobInfoEntity.getJoblinkMessageId(),
					infoValidFlg,
					warnValidFlg,
					criticalValidFlg,
					unknownValidFlg,
					application,
					monitorDetailId,
					message,
					expMap,
					acceptDateFrom,
					jobSessionJobEntity.getOwnerRoleId(),
					jobSessionJobEntity.getJoblinkRcvCheckedPosition());
	}

	/**
	 * ジョブ連携メッセージ検索処理
	 * ジョブ連携待機ジョブ、ジョブ連携受信実行契機で使用
	 * 
	 * 取得失敗した場合は、スコープ、受信日時、確認済みメッセージ番号のみを条件として取得しなおす。
	 * 
	 * @param facilityId 送信元スコープ
	 * @param joblinkMessageId ジョブ連携
	 * @param infoValidFlg true:重要度(情報)対象
	 * @param warnValidFlg true:重要度(警告)対象
	 * @param criticalValidFlg true:重要度(危険)対象
	 * @param unknownValidFlg true:重要度(不明)対象
	 * @param application アプリケーション
	 * @param monitorDetailId 監視詳細ID
	 * @param message メッセージ
	 * @param expMap 拡張情報
	 * @param acceptDateFrom 受信日時From
	 * @param ownerRoleId オーナーロールID
	 * @param joblinkRcvCheckedPosition 確認済みメッセージ番号
	 * @return ジョブ連携メッセージ
	 */
	private static JobLinkMessageEntity getJobLinkMessage(
			String facilityId,
			String joblinkMessageId,
			boolean infoValidFlg,
			boolean warnValidFlg,
			boolean criticalValidFlg,
			boolean unknownValidFlg,
			String application,
			String monitorDetailId,
			String message,
			HashMap<String, String> expMap,
			Long acceptDateFrom,
			String ownerRoleId,
			Long joblinkRcvCheckedPosition) throws HinemosUnknown {

		m_log.debug("getJobLinkMessage() : "
				+ "facilityId=" + facilityId
				+ ", joblinkMessageId=" + joblinkMessageId
				+ ", acceptDateFrom=" + acceptDateFrom
				+ ", joblinkRcvCheckedPosition=" + joblinkRcvCheckedPosition);

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// 再取得用JPQL
			String strScopeJpql = "";

			StringBuilder sbJpql = new StringBuilder();
			sbJpql.append("SELECT a FROM JobLinkMessageEntity a");
			if (expMap != null && expMap.size() > 0) {
				sbJpql.append(" JOIN a.jobLinkMessageExpInfoEntities b");
			}
			sbJpql.append(" WHERE true = true");

			//ノードのファシリティIDリスト取得
			List<String> facilityIdList = new RepositoryControllerBean().getExecTargetFacilityIdList(facilityId, ownerRoleId);
			if (facilityIdList == null || facilityIdList.isEmpty()) {
				return null;
			}
			// ファシリティID
			sbJpql.append(" AND a.id.facilityId IN (" + HinemosEntityManager.getParamNameString("facilityId", facilityIdList.toArray(new String[0])) + ")");

			if (joblinkRcvCheckedPosition != null) {
				// 確認済みメッセージ番号
				sbJpql.append(" AND a.position > :position");
			}
			// 受信日時
			if (acceptDateFrom != null) {
				sbJpql.append(" AND a.acceptDate > :acceptDateFrom");
			}

			// 再取得用JPQLをここでとっておく
			strScopeJpql = sbJpql.toString();

			// ジョブ連携メッセージ
			if (joblinkMessageId.endsWith("%")) {
				sbJpql.append(" AND a.id.joblinkMessageId like :joblinkMessageId");
			} else {
				sbJpql.append(" AND a.id.joblinkMessageId = :joblinkMessageId");
			}

			// 重要度
			List<String> priorityList = new ArrayList<>();
			if (infoValidFlg) {
				priorityList.add(":infoPriority");
			}
			if (warnValidFlg) {
				priorityList.add(":warnPriority");
			}
			if (criticalValidFlg) {
				priorityList.add(":criticalPriority");
			}
			if (unknownValidFlg) {
				priorityList.add(":unknownPriority");
			}
			sbJpql.append(" AND a.priority IN (" + String.join(",", priorityList) + ")");

			//アプリケーション
			if (application != null) {
				if (application.endsWith("%")) {
					sbJpql.append(" AND a.application like :application");
				} else {
					sbJpql.append(" AND a.application = :application");
				}
			}

			// 監視詳細
			if (monitorDetailId != null) {
				if (monitorDetailId.endsWith("%")) {
					sbJpql.append(" AND a.monitorDetailId like :monitorDetailId");
				} else {
					sbJpql.append(" AND a.monitorDetailId = :monitorDetailId");
				}
			}

			// メッセージ
			if (message != null) {
				if (message.endsWith("%")) {
					sbJpql.append(" AND a.message like :message");
				} else {
					sbJpql.append(" AND a.message = :message");
				}
			}

			// 拡張情報
			List<String> expList = new ArrayList<>();
			HashMap<String, String> expValueMap = new HashMap<>();
			if (expMap != null && expMap.size() > 0) {
				int idx = 0;
				for (Map.Entry<String, String> entry : expMap.entrySet()) {
					expList.add("b.id.key = :key" + idx + " AND b.value = :value" + idx);
					expValueMap.put("key" + idx, entry.getKey());
					expValueMap.put("value" + idx, entry.getValue());
					idx++;
				}
				sbJpql.append(" AND ((" + String.join(") OR (", expList) + "))");
			}

			// 検索順設定
			sbJpql.append(" ORDER BY a.acceptDate ASC");

			TypedQuery<JobLinkMessageEntity> typedQuery = em.createQuery(sbJpql.toString(), JobLinkMessageEntity.class);

			// ファシリティID
			HinemosEntityManager.appendParam(typedQuery, "facilityId", facilityIdList.toArray(new String[facilityIdList.size()]));

			if (joblinkRcvCheckedPosition != null) {
				// 確認済みメッセージ番号
				typedQuery = typedQuery.setParameter("position", joblinkRcvCheckedPosition);
			}
			// 受信日時
			if (acceptDateFrom != null) {
				typedQuery = typedQuery.setParameter("acceptDateFrom", acceptDateFrom);
			}

			// ジョブ連携メッセージ
			if (joblinkMessageId.endsWith("%")) {
				typedQuery = typedQuery.setParameter("joblinkMessageId", QueryDivergence.escapeConditionJobLinkRcv(joblinkMessageId));
			} else {
				typedQuery = typedQuery.setParameter("joblinkMessageId", joblinkMessageId);
			}

			// 重要度
			if (infoValidFlg) {
				typedQuery = typedQuery.setParameter("infoPriority", PriorityConstant.TYPE_INFO);
			}
			if (warnValidFlg) {
				typedQuery = typedQuery.setParameter("warnPriority", PriorityConstant.TYPE_WARNING);
			}
			if (criticalValidFlg) {
				typedQuery = typedQuery.setParameter("criticalPriority", PriorityConstant.TYPE_CRITICAL);
			}
			if (unknownValidFlg) {
				typedQuery = typedQuery.setParameter("unknownPriority", PriorityConstant.TYPE_UNKNOWN);
			}

			//アプリケーション
			if (application != null) {
				if (application.endsWith("%")) {
					typedQuery = typedQuery.setParameter("application", QueryDivergence.escapeConditionJobLinkRcv(application));
				} else {
					typedQuery = typedQuery.setParameter("application", application);
				}
			}

			// 監視詳細
			if (monitorDetailId != null) {
				if (monitorDetailId.endsWith("%")) {
					typedQuery = typedQuery.setParameter("monitorDetailId", QueryDivergence.escapeConditionJobLinkRcv(monitorDetailId));
				} else {
					typedQuery = typedQuery.setParameter("monitorDetailId", monitorDetailId);
				}
			}

			// メッセージ
			if (message != null) {
				if (message.endsWith("%")) {
					typedQuery = typedQuery.setParameter("message", QueryDivergence.escapeConditionJobLinkRcv(message));
				} else {
					typedQuery = typedQuery.setParameter("message", message);
				}
			}

			// 拡張情報
			if (expValueMap.size() > 0) {
				for(Map.Entry<String, String> entry : expValueMap.entrySet()) {
					typedQuery = typedQuery.setParameter(entry.getKey(), entry.getValue());
				}
			}

			// 1件のみ取得
			typedQuery = typedQuery.setMaxResults(1);

			List<JobLinkMessageEntity> list = typedQuery.getResultList();
			if (list.size() > 0) {
				JobLinkMessageEntity entity = list.get(0);
				entity.setMatch(true);
				return entity;
			}

			
			// 取得できない場合に、スコープ、受信日時、確認済みメッセージ番号を条件として再取得する
			sbJpql = new StringBuilder();

			// スコープ、受信日時、確認済みメッセージ番号のJPQL
			sbJpql.append(strScopeJpql);

			// 検索順設定
			sbJpql.append(" ORDER BY a.acceptDate ASC");

			typedQuery = em.createQuery(sbJpql.toString(), JobLinkMessageEntity.class);

			// ファシリティID
			HinemosEntityManager.appendParam(typedQuery, "facilityId", facilityIdList.toArray(new String[facilityIdList.size()]));

			if (joblinkRcvCheckedPosition != null) {
				// 確認済みメッセージ番号
				typedQuery = typedQuery.setParameter("position", joblinkRcvCheckedPosition);
			}
			// 受信日時
			if (acceptDateFrom != null) {
				typedQuery = typedQuery.setParameter("acceptDateFrom", acceptDateFrom);
			}

			// 1件のみ取得
			typedQuery = typedQuery.setMaxResults(1);
			
			list = typedQuery.getResultList();
			if (list.size() > 0) {
				JobLinkMessageEntity entity = list.get(0);
				entity.setMatch(false);
				return entity;
			} else {
				return null;
			}
		}
	}

	public static List<Date> selectTargetDateJobLinkMessageBySendDate(String[] facilityIds, Long sendDate) {
		StringBuilder sbJpql = new StringBuilder();
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			sbJpql.append("SELECT a.id.sendDate FROM JobLinkMessageEntity a");
			sbJpql.append(" WHERE a.id.sendDate < :sendDate");
			if (facilityIds != null && facilityIds.length > 0) {
				sbJpql.append(" AND a.id.facilityId IN (" + 
						HinemosEntityManager.getParamNameString("facilityId", facilityIds) + ")");
			}
			sbJpql.append(" GROUP BY a.id.sendDate ORDER BY a.id.sendDate");
			TypedQuery<Long> typedQuery = em.createQuery(sbJpql.toString(), Long.class);
			if (facilityIds != null && facilityIds.length > 0) {
					typedQuery = HinemosEntityManager.appendParam(typedQuery, "facilityId", facilityIds);
			}
			List<Long> list = typedQuery.setParameter("sendDate", sendDate).getResultList();
			return getTargetDateListByUnixTimeLsit(list);
		}
	}

	public static int deleteJobLinkMessageBySendDate(String[] facilityIds, Date targetDate) {
		StringBuilder sbJpql = new StringBuilder();
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			sbJpql.append("DELETE FROM JobLinkMessageEntity a");
			sbJpql.append(" WHERE a.id.sendDate < :sendDate");
			if (facilityIds != null && facilityIds.length > 0) {
				sbJpql.append(" AND a.id.facilityId IN (" + 
						HinemosEntityManager.getParamNameString("facilityId", facilityIds) + ")");
			}
			TypedQuery<?> typedQuery = em.createQuery(sbJpql.toString(), Integer.class);
			if (facilityIds != null && facilityIds.length > 0) {
					typedQuery = HinemosEntityManager.appendParam(typedQuery, "facilityId", facilityIds);
			}
			return typedQuery.setParameter("sendDate", parseTargetDateToTargetUnixTime(targetDate))
					.executeUpdate();
		}
	}

	/**
	 * ジョブ連携送信設定IDが設定されているジョブマスタを検索
	 * 
	 * @param joblinkSendSettingId ジョブ連携送信設定ID
	 * @param mode オブジェクト権限種別
	 * @return ジョブマスタ一覧
	 */
	public static List<JobMstEntity> getJobMstByJoblinkSendSettingId_NONE(String joblinkSendSettingId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("JobMstEntity.findByJoblinkSendSettingId", JobMstEntity.class, ObjectPrivilegeMode.NONE)
					.setParameter("joblinkSendSettingId", joblinkSendSettingId).getResultList();
		}
	}
	
	/**
	 * UnixTime(ミリ秒)のリストから、Date型の日付重複のないリストを取得する
	 * 
	 * @param unixTimeList
	 * @return 処理対象となる日付のリスト
	 */
	private static List<Date> getTargetDateListByUnixTimeLsit(List<Long> unixTimeList){
		List<Date> ret = new ArrayList<Date>();
		Calendar calendar = Calendar.getInstance();
		for(long unixTime : unixTimeList){
			calendar.setTimeInMillis(unixTime);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			Date date = new Date(calendar.getTime().getTime());
			if(!ret.contains(date)){
				ret.add(date);
			}
		}
		return ret;
	}

	/**
	 * 削除対象の日付(Date)をDBに合わせてUnixTime(ミリ秒)に変換する
	 * ※その際、削除は渡したUnixTime未満で行われるため、削除対象の日付を削除するために＋1日してからUnixTimeに変換する
	 * 
	 * @param targetDate 削除対象日付
	 * @return 削除対象日付＋1日のUnixTime
	 */
	private static long parseTargetDateToTargetUnixTime(Date targetDate){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(targetDate);
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		return calendar.getTimeInMillis();
	}
}
