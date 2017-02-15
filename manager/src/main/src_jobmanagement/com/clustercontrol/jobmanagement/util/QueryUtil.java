/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.util;

import java.util.List;

import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.IconFileNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobInfoEntityPK;
import com.clustercontrol.jobmanagement.model.JobKickEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntityPK;
import com.clustercontrol.jobmanagement.model.JobParamMstEntity;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntityPK;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntityPK;
import com.clustercontrol.jobmanagement.model.JobmapIconImageEntity;
import com.clustercontrol.monitor.run.model.MonitorInfo;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static JobMstEntity getJobMstPK(JobMstEntityPK pk, ObjectPrivilegeMode mode) throws JobMasterNotFound, InvalidRole {
		JpaTransactionManager jtm = null;
		JobMstEntity jobMst = null;
		try {
			jtm = new JpaTransactionManager();
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
		} finally {
			if (jtm != null){
				jtm.close();
			}
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
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		JobMstEntity jobMst = null;
		try {
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
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		return em.createNamedQuery("JobMstEntity.findByJobunitId", JobMstEntity.class)
					.setParameter("jobunitId", jobunitId).getResultList();
	}

	public static List<JobMstEntity> getJobMstEnityFindByIconId(
			String iconId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		return em.createNamedQuery("JobMstEntity.findByIconId", JobMstEntity.class)
					.setParameter("iconId", iconId).getResultList();
	}

	public static List<JobInfoEntity> getJobInfoEnityFindByIconId(
			String iconId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		return em.createNamedQuery("JobInfoEntity.findByIconId", JobInfoEntity.class)
					.setParameter("iconId", iconId).getResultList();
	}

	public static JobInfoEntity getJobInfoEntityPK(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		//セッションIDとジョブIDから、セッションジョブを取得
		JobInfoEntityPK pk = new JobInfoEntityPK(sessionId, jobunitId, jobId);
		JobInfoEntity jobInfo = null;
		try {
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
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
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

	public static JobSessionJobEntity getJobSessionJobPK(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, InvalidRole {
		return getJobSessionJobPK(sessionId, jobunitId, jobId, ObjectPrivilegeMode.READ);
	}

	public static JobSessionJobEntity getJobSessionJobPK(String sessionId, String jobunitId, String jobId, ObjectPrivilegeMode mode)
			throws JobInfoNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		//セッションIDとジョブIDから、セッションジョブを取得
		JobSessionJobEntityPK sessionJobPk = new JobSessionJobEntityPK(sessionId, jobunitId, jobId);
		JobSessionJobEntity sessionJob = null;
		try {
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
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

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

	public static List<JobMstEntity> getJobMstEntityFindByCalendarId(String calendarId){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		//ジョブ
		List<JobMstEntity> jobMstList
		= em.createNamedQuery("JobMstEntity.findByCalendarId", JobMstEntity.class)
		.setParameter("calendarId", calendarId).getResultList();
		return jobMstList;
	}

	public static List<JobMstEntity> getJobMstEntityFindByMonitorId_NONE(String monitorId){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		//ジョブ
		List<JobMstEntity> jobMstList
		= em.createNamedQuery("JobMstEntity.findByMonitorId", JobMstEntity.class, ObjectPrivilegeMode.NONE)
		.setParameter("monitorId", monitorId).getResultList();
		return jobMstList;
	}

	public static List<JobMstEntity> getJobMstEntityFindByOwnerRoleId_NONE(String roleId){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		//ジョブ
		List<JobMstEntity> jobMstList
		= em.createNamedQuery("JobMstEntity.findByOwnerRoleId", JobMstEntity.class, ObjectPrivilegeMode.NONE)
		.setParameter("ownerRoleId", roleId).getResultList();
		return jobMstList;
	}

	public static List<JobParamMstEntity> getJobParamMstEntityFindByJobunitIdParamId(String jobunitId, String paramId){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		// ジョブユニットIDを条件としてジョブ変数を取得
		List<JobParamMstEntity> jobParamMstEntities
			= em.createNamedQuery("JobParamMstEntity.findByJobunitIdParamId", JobParamMstEntity.class)
			.setParameter("jobunitId", jobunitId)
			.setParameter("paramId", paramId)
			.getResultList();
		return jobParamMstEntities;
	}

	public static List<JobSessionJobEntity> getChildJobSessionJob(String sessionId, String parentJobunitId, String parentJobId){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		//ジョブ
		List<JobSessionJobEntity> jobSessionJobList
		= em.createNamedQuery("JobSessionJobEntity.findChild", JobSessionJobEntity.class)
		.setParameter("sessionId", sessionId)
		.setParameter("parentJobunitId", parentJobunitId)
		.setParameter("parentJobId", parentJobId).getResultList();
		return jobSessionJobList;
	}

	public static List<JobSessionJobEntity> getJobSessionJobByParentStatus(String sessionId, String parentJobunitId, String parentJobId, int status){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		//ジョブ
		List<JobSessionJobEntity> jobSessionJobList
		= em.createNamedQuery("JobSessionJobEntity.findByParentStatus", JobSessionJobEntity.class)
		.setParameter("sessionId", sessionId)
		.setParameter("parentJobunitId", parentJobunitId)
		.setParameter("parentJobId", parentJobId)
		.setParameter("status", status).getResultList();
		return jobSessionJobList;
	}

	public static List<JobSessionJobEntity> getJobSessionJobBySessionId(String sessionId){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		//ジョブ
		List<JobSessionJobEntity> jobSessionJobList
		= em.createNamedQuery("JobSessionJobEntity.findByJobSessionId", JobSessionJobEntity.class)
		.setParameter("sessionId", sessionId).getResultList();
		return jobSessionJobList;
	}

	public static List<JobSessionJobEntity> getChildJobSessionJobOrderByStartDate(String sessionId, String parentJobunitId, String parentJobId){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		//ジョブ
		List<JobSessionJobEntity> jobSessionJobList
		= em.createNamedQuery("JobSessionJobEntity.findByStartDate", JobSessionJobEntity.class)
		.setParameter("sessionId", sessionId)
		.setParameter("parentJobunitId", parentJobunitId)
		.setParameter("parentJobId", parentJobId).getResultList();
		return jobSessionJobList;
	}

	public static List<JobKickEntity> getJobKickEntityFindByCalendarId_NONE(String calendarId){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		//ジョブ実行契機
		List<JobKickEntity> jobKickList
		= em.createNamedQuery("JobKickEntity.findByCalendarId", JobKickEntity.class, ObjectPrivilegeMode.NONE)
		.setParameter("calendarId", calendarId).getResultList();
		return jobKickList;
	}

	public static List<JobKickEntity> getJobKickEntityFindByOwnerRoleId_NONE(String roleId){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		//ジョブ実行契機
		List<JobKickEntity> jobScheduleList
		= em.createNamedQuery("JobKickEntity.findByOwnerRoleId", JobKickEntity.class, ObjectPrivilegeMode.NONE)
		.setParameter("ownerRoleId", roleId).getResultList();
		return jobScheduleList;
	}

	public static List<JobKickEntity> getJobKickEntityFindByJobKickType(Integer jobkickType){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		//ジョブ実行契機
		List<JobKickEntity> jobScheduleList
		= em.createNamedQuery("JobKickEntity.findByJobKickType", JobKickEntity.class)
		.setParameter("jobkickType", jobkickType).getResultList();
		return jobScheduleList;
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
		
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

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
				typedQuery = typedQuery.setParameter("jobkickId", jobkickId);
			}else{
				typedQuery = typedQuery.setParameter("jobkickId", jobkickId.substring(notInclude.length()));
			}
		}
		// jobkickName
		if(jobkickName != null && !"".equals(jobkickName)) {
			if(!jobkickName.startsWith(notInclude)) {
				typedQuery = typedQuery.setParameter("jobkickName", jobkickName);
			}else{
				typedQuery = typedQuery.setParameter("jobkickName", jobkickName.substring(notInclude.length()));
			}
		}
		// jobkickType
		if(jobkickType != null) {
			typedQuery = typedQuery.setParameter("jobkickType", jobkickType);
		}
		// jobunitId
		if(jobunitId != null && !"".equals(jobunitId)) {
			if(!jobunitId.startsWith(notInclude)) {
				typedQuery = typedQuery.setParameter("jobunitId", jobunitId);
			}else{
				typedQuery = typedQuery.setParameter("jobunitId", jobunitId.substring(notInclude.length()));
			}
		}
		// jobId
		if(jobId != null && !"".equals(jobId)) {
			if(!jobId.startsWith(notInclude)) {
				typedQuery = typedQuery.setParameter("jobId", jobId);
			}else{
				typedQuery = typedQuery.setParameter("jobId", jobId.substring(notInclude.length()));
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
				typedQuery = typedQuery.setParameter("regUser", regUser);
			}else{
				typedQuery = typedQuery.setParameter("regUser", regUser.substring(notInclude.length()));
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
				typedQuery = typedQuery.setParameter("updateUser", updateUser);
			}else{
				typedQuery = typedQuery.setParameter("updateUser", updateUser.substring(notInclude.length()));
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
				typedQuery = typedQuery.setParameter("ownerRoleId", ownerRoleId);
			}else{
				typedQuery = typedQuery.setParameter("ownerRoleId", ownerRoleId.substring(notInclude.length()));
			}
		}
		return typedQuery.getResultList();
	}

	public static JobmapIconImageEntity getJobmapIconImagePK(String filename, ObjectPrivilegeMode mode) throws IconFileNotFound, InvalidRole {
		JpaTransactionManager jtm = null;
		JobmapIconImageEntity jobmapIconImageEntity = null;
		try {
			jtm = new JpaTransactionManager();
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
		} finally {
			if (jtm != null){
				jtm.close();
			}
		}
		return jobmapIconImageEntity;
	}

	public static List<JobmapIconImageEntity> getJobmapIconImageEntities() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		return em.createNamedQuery("JobmapIconImageEntity.findAll", JobmapIconImageEntity.class)
				.getResultList();
	}

	public static List<String> getJobmapIconImageIdExceptDefaultList_OR(String jobnetIconId, String jobIconId, String orderRoleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		return em.createNamedQuery_OR("JobmapIconImageEntity.findAllIconIdExceptDefault", String.class, orderRoleId)
				.setParameter("jobnetIconId", jobnetIconId)
				.setParameter("jobIconId", jobIconId)
				.getResultList();
	}
	public static List<MonitorInfo> getMonitorInfoByMonitorTypeIds_OR(List<String> monitorTypeIds, String ownerRoleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		StringBuffer sbJpql = new StringBuffer();
		sbJpql.append("SELECT a FROM MonitorInfo a");
		if (monitorTypeIds != null && monitorTypeIds.size() > 0) {
			sbJpql.append(" WHERE a.monitorTypeId IN (" + HinemosEntityManager.getParamNameString("monitorTypeId", monitorTypeIds) + ")");
		}
		sbJpql.append(" ORDER BY a.monitorId");
		TypedQuery<MonitorInfo> typedQuery
			= em.createQuery_OR(sbJpql.toString(), MonitorInfo.class, ownerRoleId);
		if (monitorTypeIds != null && monitorTypeIds.size() > 0) {
			typedQuery = HinemosEntityManager.appendParam(typedQuery, "monitorTypeId", monitorTypeIds.toArray(new String[0]));
		}
		return typedQuery.getResultList();
	}

	public static List<JobSessionNodeEntity> getJobSessionNodeEntityFindByJobTypeEndIsNull_NONE(int jobType){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<JobSessionNodeEntity> jobSessionNodeEntities
			= em.createNamedQuery("JobSessionNodeEntity.findByJobTypeEndIsNull", JobSessionNodeEntity.class, ObjectPrivilegeMode.NONE)
			.setParameter("jobType", jobType)
			.getResultList();
		return jobSessionNodeEntities;
	}
}
