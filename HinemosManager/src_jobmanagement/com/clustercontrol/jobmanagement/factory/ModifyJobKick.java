/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.factory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.RunInterval;
import com.clustercontrol.bean.ScheduleConstant;
import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.commons.scheduler.QuartzUtil;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.JobKickDuplicate;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.jobmanagement.bean.JobFileCheck;
import com.clustercontrol.jobmanagement.bean.JobKick;
import com.clustercontrol.jobmanagement.bean.JobKickConstant;
import com.clustercontrol.jobmanagement.bean.JobLinkConstant;
import com.clustercontrol.jobmanagement.bean.JobLinkExpInfo;
import com.clustercontrol.jobmanagement.bean.JobLinkRcv;
import com.clustercontrol.jobmanagement.bean.JobRuntimeParam;
import com.clustercontrol.jobmanagement.bean.JobRuntimeParamDetail;
import com.clustercontrol.jobmanagement.bean.JobSchedule;
import com.clustercontrol.jobmanagement.bean.JobTriggerInfo;
import com.clustercontrol.jobmanagement.bean.JobTriggerTypeConstant;
import com.clustercontrol.jobmanagement.bean.QuartzConstant;
import com.clustercontrol.jobmanagement.bean.SessionPremakeScheduleType;
import com.clustercontrol.jobmanagement.model.JobKickEntity;
import com.clustercontrol.jobmanagement.model.JobLinkJobkickExpInfoEntity;
import com.clustercontrol.jobmanagement.model.JobLinkJobkickExpInfoEntityPK;
import com.clustercontrol.jobmanagement.model.JobRuntimeParamDetailEntity;
import com.clustercontrol.jobmanagement.model.JobRuntimeParamDetailEntityPK;
import com.clustercontrol.jobmanagement.model.JobRuntimeParamEntity;
import com.clustercontrol.jobmanagement.model.JobRuntimeParamEntityPK;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.jobmanagement.util.DeletePremakeWorker;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.maintenance.factory.MaintenanceJob;
import com.clustercontrol.plugin.impl.SchedulerPlugin;
import com.clustercontrol.util.HinemosTime;

/**
 * スケジュール情報を操作するクラスです。
 *
 * @version 2.4.0
 * @since 1.0.0
 */
public class ModifyJobKick {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( ModifyJobKick.class );

	/**
	 * ジョブ実行契機情報をDBに反映します。
	 * スケジュール情報の場合はスケジューラにジョブを登録します。<BR>
	 *
	 * @param info ジョブ実行契機情報
	 * @param user ユーザID
	 * @param jobkickType ジョブ実行契機種別
	 * @throws HinemosUnknown
	 * @throws JobKickDuplicate
	 *
	 * @see com.clustercontrol.jobmanagement.bean.QuartzConstant
	 * @see com.clustercontrol.jobmanagement.bean.JobTriggerInfo
	 * @see com.clustercontrol.jobmanagement.util.QuartzUtil#getQuartzManager()
	 */
	public void addJobKick(final JobKick info, String loginUser, Integer jobkickType) throws HinemosUnknown, JobKickDuplicate {
		m_log.debug("addJobKick() : id=" + info.getId() + ", jobId=" + info.getJobId() + ", jobkickType=" + jobkickType);
		//最終更新日時を設定
		long now = HinemosTime.currentTimeMillis();
		boolean isPremakeSchedule = false;

		// DBにスケジュール情報を保存
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// IDの重複チェック
			String id = info.getId();
			jtm.checkEntityExists(JobKickEntity.class, id);

			// 値設定
			JobKickEntity jobKickEntity = new JobKickEntity(info.getId());
			jobKickEntity.setJobkickName(info.getName());
			jobKickEntity.setJobkickType(jobkickType);
			jobKickEntity.setJobunitId(info.getJobunitId());
			jobKickEntity.setJobId(info.getJobId());
			// 登録
			em.persist(jobKickEntity);
			// ランタイムジョブ変数情報
			if (info.getJobRuntimeParamList() != null
					&& info.getJobRuntimeParamList().size() > 0) {
				for (JobRuntimeParam jobRuntimeParam : info.getJobRuntimeParamList()) {
					JobRuntimeParamEntity jobRuntimeParamEntity 
						= new JobRuntimeParamEntity(jobKickEntity, jobRuntimeParam.getParamId());
					jobRuntimeParamEntity.setParamType(jobRuntimeParam.getParamType());
					jobRuntimeParamEntity.setDefaultValue(jobRuntimeParam.getValue());
					jobRuntimeParamEntity.setDescription(jobRuntimeParam.getDescription());
					jobRuntimeParamEntity.setRequiredFlg(jobRuntimeParam.getRequiredFlg());
					em.persist(jobRuntimeParamEntity);
					jobRuntimeParamEntity.relateToJobKickEntity(jobKickEntity);

					// ランタイムジョブ変数詳細情報
					if (jobRuntimeParam.getJobRuntimeParamDetailList() != null
							&& jobRuntimeParam.getJobRuntimeParamDetailList().size() > 0) {
						int detailIdx = 0;
						for (JobRuntimeParamDetail jobRuntimeParamDetail 
								: jobRuntimeParam.getJobRuntimeParamDetailList()) {
							JobRuntimeParamDetailEntity jobRuntimeParamDetailEntity
								= new JobRuntimeParamDetailEntity(jobRuntimeParamEntity, 
										detailIdx);
							// 登録
							em.persist(jobRuntimeParamDetailEntity);
							jobRuntimeParamDetailEntity.relateToJobRuntimeParamEntity(jobRuntimeParamEntity);
							jobRuntimeParamDetailEntity.setParamValue(
									jobRuntimeParamDetail.getParamValue());
							jobRuntimeParamDetailEntity.setDescription(
									jobRuntimeParamDetail.getDescription());
							detailIdx++;
						}
					}
				}
			}

			if (jobkickType == JobKickConstant.TYPE_SCHEDULE) {
				// ジョブスケジュール
				if (!(info instanceof JobSchedule)) {
					throw new HinemosUnknown("type error : " + info.getClass() + "!=JobSchedule");
				}
				JobSchedule jobSchedule = (JobSchedule)info;
				if (!"".equals(jobSchedule.getCalendarId())) {
					jobKickEntity.setCalendarId(jobSchedule.getCalendarId());
				}
				jobKickEntity.setValidFlg(info.isValid());
				jobKickEntity.setScheduleType(jobSchedule.getScheduleType());
				jobKickEntity.setWeek(jobSchedule.getWeek());
				jobKickEntity.setHour(jobSchedule.getHour());
				jobKickEntity.setMinute(jobSchedule.getMinute());
				jobKickEntity.setFromXMinutes(jobSchedule.getFromXminutes());
				jobKickEntity.setEveryXMinutes(jobSchedule.getEveryXminutes());
				jobKickEntity.setSessionPremakeFlg(jobSchedule.getSessionPremakeFlg());
				jobKickEntity.setSessionPremakeScheduleType(jobSchedule.getSessionPremakeScheduleType());
				jobKickEntity.setSessionPremakeWeek(jobSchedule.getSessionPremakeWeek());
				jobKickEntity.setSessionPremakeHour(jobSchedule.getSessionPremakeHour());
				jobKickEntity.setSessionPremakeMinute(jobSchedule.getSessionPremakeMinute());
				jobKickEntity.setSessionPremakeEveryXHour(jobSchedule.getSessionPremakeEveryXHour());
				jobKickEntity.setSessionPremakeDate(jobSchedule.getSessionPremakeDate());
				jobKickEntity.setSessionPremakeToDate(jobSchedule.getSessionPremakeToDate());
				jobKickEntity.setSessionPremakeInternalFlg(jobSchedule.getSessionPremakeInternalFlg());

				if (!jobSchedule.isValid().booleanValue()
						|| !jobSchedule.getSessionPremakeFlg().booleanValue()) {
					isPremakeSchedule = false;
				} else {
					isPremakeSchedule = true;
				}
			} else if (jobkickType == JobKickConstant.TYPE_FILECHECK) {
				// ファイルチェック
				if (!(info instanceof JobFileCheck)) {
					throw new HinemosUnknown("type error : " + info.getClass() + "!=JobFileCheck");
				}
				JobFileCheck jobFileCheck = (JobFileCheck)info;
				if (!"".equals(jobFileCheck.getCalendarId())) {
					jobKickEntity.setCalendarId(jobFileCheck.getCalendarId());
				}
				jobKickEntity.setValidFlg(info.isValid());
				jobKickEntity.setFacilityId(jobFileCheck.getFacilityId());
				jobKickEntity.setFileName(jobFileCheck.getFileName());
				jobKickEntity.setDirectory(jobFileCheck.getDirectory());
				jobKickEntity.setEventType(jobFileCheck.getEventType());
				jobKickEntity.setModifyType(jobFileCheck.getModifyType());
				jobKickEntity.setCarryOverJudgementFlg(jobFileCheck.getCarryOverJudgmentFlg());
			} else if (jobkickType == JobKickConstant.TYPE_MANUAL) {
				jobKickEntity.setCalendarId(null);
				jobKickEntity.setValidFlg(true);

			} else if (jobkickType == JobKickConstant.TYPE_JOBLINKRCV) {
				// ジョブ連携受信
				if (!(info instanceof JobLinkRcv)) {
					throw new HinemosUnknown("type error : " + info.getClass() + "!=JobLinkRcv");
				}
				JobLinkRcv jobLinkRcv = (JobLinkRcv)info;
				if (!"".equals(jobLinkRcv.getCalendarId())) {
					jobKickEntity.setCalendarId(jobLinkRcv.getCalendarId());
				}
				jobKickEntity.setValidFlg(jobLinkRcv.isValid());
				jobKickEntity.setFacilityId(jobLinkRcv.getFacilityId());
				jobKickEntity.setJoblinkMessageId(jobLinkRcv.getJoblinkMessageId());
				jobKickEntity.setInfoValidFlg(jobLinkRcv.getInfoValidFlg());
				jobKickEntity.setWarnValidFlg(jobLinkRcv.getWarnValidFlg());
				jobKickEntity.setCriticalValidFlg(jobLinkRcv.getCriticalValidFlg());
				jobKickEntity.setUnknownValidFlg(jobLinkRcv.getUnknownValidFlg());
				jobKickEntity.setApplicationFlg(jobLinkRcv.getApplicationFlg());
				jobKickEntity.setApplication(jobLinkRcv.getApplication());
				jobKickEntity.setMonitorDetailIdFlg(jobLinkRcv.getMonitorDetailIdFlg());
				jobKickEntity.setMonitorDetailId(jobLinkRcv.getMonitorDetailId());
				jobKickEntity.setMessageFlg(jobLinkRcv.getMessageFlg());
				jobKickEntity.setMessage(jobLinkRcv.getMessage());
				jobKickEntity.setExpFlg(jobLinkRcv.getExpFlg());
				if (jobLinkRcv.getJobLinkExpList() != null
						&& jobLinkRcv.getJobLinkExpList().size()> 0) {
					for (JobLinkExpInfo jobLinkExpInfo : jobLinkRcv.getJobLinkExpList()) {
						JobLinkJobkickExpInfoEntity entity = new JobLinkJobkickExpInfoEntity(
								info.getId(), jobLinkExpInfo.getKey());
						entity.setValue(jobLinkExpInfo.getValue());
						em.persist(entity);
						entity.relateToJobKickEntity(jobKickEntity);
						jobKickEntity.getJobLinkJobkickExpInfoEntities().add(entity);
					}
				}
			}
			jobKickEntity.setOwnerRoleId(info.getOwnerRoleId());
			jobKickEntity.setRegDate(now);
			jobKickEntity.setUpdateDate(now);
			jobKickEntity.setRegUser(loginUser);
			jobKickEntity.setUpdateUser(loginUser);
		} catch (EntityExistsException e) {
			m_log.info("addJobKick() JobKickEntity.create() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new JobKickDuplicate(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("addJobKick() JobKickEntity.create() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
		
		if (jobkickType == JobKickConstant.TYPE_SCHEDULE) {
			// ジョブスケジュール定義、ジョブセッション事前生成のスケジュール定義の登録・削除
			registScheduler(info.isValid().booleanValue(), isPremakeSchedule, (JobSchedule)info);
		} else if (jobkickType == JobKickConstant.TYPE_JOBLINKRCV) {
			// ジョブ連携受信実行契機スケジュール定義
			registJobLinkRcvScheduler(info.isValid().booleanValue(), (JobLinkRcv)info);
		}

	}

	/**
	 * DBのジョブ契機情報を変更します。<BR>
	 * @param info ジョブ実行契機情報
	 * @param loginUser ユーザID
	 * @param jobkickType ジョブ実行契機種別
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws ObjectPrivilege_InvalidRole
	 */
	public void modifyJobKick(final JobKick info, String loginUser, Integer jobkickType) throws HinemosUnknown, JobInfoNotFound, ObjectPrivilege_InvalidRole{
		m_log.debug("modifyJobKick() : id=" + info.getId() + ", jobId=" + info.getJobId() + ", jobkickType=" + jobkickType);
		//最終更新日時を設定
		long now = HinemosTime.currentTimeMillis();
		boolean isPremakeSchedule = false;
		boolean isDeletePremake = false;
		String beforeJobunitId = "";
		String beforeJobId = "";

		// 変更前の事前ジョブセッション生成フラグと設定の有効無効を退避
		boolean beforeSessionPremakeFlg = false;
		boolean beforeValidFlg = false;

		// DBにスケジュール情報を保存
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			JobKickEntity bean  = em.find(JobKickEntity.class, info.getId(),
					ObjectPrivilegeMode.MODIFY);
			if (bean == null) {
				JobInfoNotFound e = new JobInfoNotFound("JobKickEntity.findByPrimaryKey");
				m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			bean.setJobkickId(info.getId());
			bean.setJobkickName(info.getName());
			beforeJobunitId = bean.getJobunitId();
			bean.setJobunitId(info.getJobunitId());
			beforeJobId = bean.getJobId();
			bean.setJobId(info.getJobId());

			// ランタイムジョブ変数情報
			ArrayList<JobRuntimeParamEntityPK> jobRuntimeParamEntityPKList = new ArrayList<>();
				
			if (info.getJobRuntimeParamList() != null
					&& info.getJobRuntimeParamList().size() > 0) {
				for (JobRuntimeParam jobRuntimeParam : info.getJobRuntimeParamList()) {
					JobRuntimeParamEntityPK jobRuntimeParamEntityPK 
						= new JobRuntimeParamEntityPK(info.getId(), jobRuntimeParam.getParamId());
					JobRuntimeParamEntity jobRuntimeParamEntity 
						= em.find(JobRuntimeParamEntity.class, jobRuntimeParamEntityPK, 
								ObjectPrivilegeMode.MODIFY);
					if (jobRuntimeParamEntity == null) {
						// 新規登録
						jobRuntimeParamEntity 
							= new JobRuntimeParamEntity(jobRuntimeParamEntityPK);
						em.persist(jobRuntimeParamEntity);
						jobRuntimeParamEntity.relateToJobKickEntity(bean);
					}
					jobRuntimeParamEntity.setParamType(jobRuntimeParam.getParamType());
					jobRuntimeParamEntity.setDefaultValue(jobRuntimeParam.getValue());
					jobRuntimeParamEntity.setDescription(jobRuntimeParam.getDescription());
					jobRuntimeParamEntity.setRequiredFlg(jobRuntimeParam.getRequiredFlg());
					jobRuntimeParamEntityPKList.add(jobRuntimeParamEntityPK);
					
					// ランタイムジョブ変数詳細情報
					ArrayList<JobRuntimeParamDetailEntityPK> jobRuntimeParamDetailEntityPKList = new ArrayList<>();
					if (jobRuntimeParam.getJobRuntimeParamDetailList() != null
							&& jobRuntimeParam.getJobRuntimeParamDetailList().size() > 0) {
						int detailIdx = 0;
						for (JobRuntimeParamDetail jobRuntimeParamDetail 
								: jobRuntimeParam.getJobRuntimeParamDetailList()) {
							JobRuntimeParamDetailEntityPK jobRuntimeParamDetailEntityPK
								= new JobRuntimeParamDetailEntityPK(
										info.getId(), 
										jobRuntimeParam.getParamId(),
										detailIdx);
							JobRuntimeParamDetailEntity jobRuntimeParamDetailEntity
								= em.find(JobRuntimeParamDetailEntity.class, 
										jobRuntimeParamDetailEntityPK,
										ObjectPrivilegeMode.MODIFY);
							if (jobRuntimeParamDetailEntity == null) {
								// 新規登録
								jobRuntimeParamDetailEntity = new JobRuntimeParamDetailEntity(
										jobRuntimeParamDetailEntityPK);
								// 登録
								em.persist(jobRuntimeParamDetailEntity);
								jobRuntimeParamDetailEntity.relateToJobRuntimeParamEntity(jobRuntimeParamEntity);
							}
							jobRuntimeParamDetailEntity.setParamValue(
									jobRuntimeParamDetail.getParamValue());
							jobRuntimeParamDetailEntity.setDescription(
									jobRuntimeParamDetail.getDescription());
							jobRuntimeParamDetailEntityPKList.add(jobRuntimeParamDetailEntityPK);
							detailIdx++;
						}
					}
					// 不要なJobRuntimeParamDetailEntityを削除
					jobRuntimeParamEntity.deleteJobRuntimeParamDetailEntities(jobRuntimeParamDetailEntityPKList);
				}
			}
			// 不要なJobRuntimeParamEntityを削除
			bean.deleteJobRuntimeParamEntities(jobRuntimeParamEntityPKList);

			if (jobkickType == JobKickConstant.TYPE_SCHEDULE) {
				// ジョブスケジュール
				if (!(info instanceof JobSchedule)) {
					throw new HinemosUnknown("type error : " + info.getClass() + "!=JobSchedule");
				}
				// スケジュールと判定された場合は事前ジョブセッション生成フラグと設定の有効無効を退避
				beforeSessionPremakeFlg = bean.getSessionPremakeFlg();
				beforeValidFlg = bean.getValidFlg();

				JobSchedule jobSchedule = (JobSchedule)info;

				// ジョブセッション事前生成用フラグ反転
				if (!jobSchedule.isValid().booleanValue()
						|| !jobSchedule.getSessionPremakeFlg().booleanValue()) {
					// ジョブスケジュール、もしくはジョブセッション事前生成無効の場合
					isDeletePremake = true;
					isPremakeSchedule = false;
				} else {
					isPremakeSchedule = true;

					if (jobSchedule.getScheduleType() != bean.getScheduleType()
						|| !Objects.equals(jobSchedule.getWeek(), bean.getWeek())
						|| !Objects.equals(jobSchedule.getHour(), bean.getHour())
						|| !Objects.equals(jobSchedule.getMinute(), bean.getMinute())
						|| !Objects.equals(jobSchedule.getFromXminutes(), bean.getFromXMinutes())
						|| !Objects.equals(jobSchedule.getEveryXminutes(), bean.getEveryXMinutes())
						|| !jobSchedule.getJobunitId().equals(beforeJobunitId)
						|| (!jobSchedule.getJobId().equals(beforeJobId))) {
						// ジョブスケジュール変更
						isDeletePremake = true;
					}
				}

				// 前回が無効だった場合は削除フラグをリセットする
				if (!beforeSessionPremakeFlg
					|| !beforeValidFlg) {
					isDeletePremake = false;
				}

				if ("".equals(jobSchedule.getCalendarId())) {
					bean.setCalendarId(null);
				} else {
					bean.setCalendarId(jobSchedule.getCalendarId());
				}
				bean.setValidFlg(info.isValid());
				bean.setScheduleType(jobSchedule.getScheduleType());
				bean.setWeek(jobSchedule.getWeek());
				bean.setHour(jobSchedule.getHour());
				bean.setMinute(jobSchedule.getMinute());
				bean.setFromXMinutes(jobSchedule.getFromXminutes());
				bean.setEveryXMinutes(jobSchedule.getEveryXminutes());
				bean.setSessionPremakeFlg(jobSchedule.getSessionPremakeFlg());
				bean.setSessionPremakeScheduleType(jobSchedule.getSessionPremakeScheduleType());
				bean.setSessionPremakeWeek(jobSchedule.getSessionPremakeWeek());
				bean.setSessionPremakeHour(jobSchedule.getSessionPremakeHour());
				bean.setSessionPremakeMinute(jobSchedule.getSessionPremakeMinute());
				bean.setSessionPremakeEveryXHour(jobSchedule.getSessionPremakeEveryXHour());
				bean.setSessionPremakeDate(jobSchedule.getSessionPremakeDate());
				bean.setSessionPremakeToDate(jobSchedule.getSessionPremakeToDate());
				bean.setSessionPremakeInternalFlg(jobSchedule.getSessionPremakeInternalFlg());
			} else if (jobkickType == JobKickConstant.TYPE_FILECHECK) {
				// ファイルチェック
				if (!(info instanceof JobFileCheck)) {
					throw new HinemosUnknown("type error : " + info.getClass() + "!=JobFileCheck");
				}
				JobFileCheck jobFileCheck = (JobFileCheck)info;
				if ("".equals(jobFileCheck.getCalendarId())) {
					bean.setCalendarId(null);
				} else {
					bean.setCalendarId(jobFileCheck.getCalendarId());
				}
				bean.setValidFlg(info.isValid());
				bean.setFacilityId(jobFileCheck.getFacilityId());
				bean.setDirectory(jobFileCheck.getDirectory());
				bean.setFileName(jobFileCheck.getFileName());
				bean.setEventType(jobFileCheck.getEventType());
				bean.setModifyType(jobFileCheck.getModifyType());
				bean.setCarryOverJudgementFlg(jobFileCheck.getCarryOverJudgmentFlg());
			} else if (jobkickType == JobKickConstant.TYPE_MANUAL) {
				bean.setCalendarId(null);
				bean.setValidFlg(true);

			} else if (jobkickType == JobKickConstant.TYPE_JOBLINKRCV) {
				// ジョブ連携受信
				if (!(info instanceof JobLinkRcv)) {
					throw new HinemosUnknown("type error : " + info.getClass() + "!=JobLinkRcv");
				}
				JobLinkRcv jobLinkRcv = (JobLinkRcv)info;
				if (!"".equals(jobLinkRcv.getCalendarId())) {
					bean.setCalendarId(jobLinkRcv.getCalendarId());
				}
				bean.setValidFlg(jobLinkRcv.isValid());
				bean.setFacilityId(jobLinkRcv.getFacilityId());
				bean.setJoblinkMessageId(jobLinkRcv.getJoblinkMessageId());
				bean.setInfoValidFlg(jobLinkRcv.getInfoValidFlg());
				bean.setWarnValidFlg(jobLinkRcv.getWarnValidFlg());
				bean.setCriticalValidFlg(jobLinkRcv.getCriticalValidFlg());
				bean.setUnknownValidFlg(jobLinkRcv.getUnknownValidFlg());
				bean.setApplicationFlg(jobLinkRcv.getApplicationFlg());
				bean.setApplication(jobLinkRcv.getApplication());
				bean.setMonitorDetailIdFlg(jobLinkRcv.getMonitorDetailIdFlg());
				bean.setMonitorDetailId(jobLinkRcv.getMonitorDetailId());
				bean.setMessageFlg(jobLinkRcv.getMessageFlg());
				bean.setMessage(jobLinkRcv.getMessage());
				bean.setExpFlg(jobLinkRcv.getExpFlg());
				// 拡張情報
				ArrayList<JobLinkJobkickExpInfoEntityPK> jobLinkJobkickExpInfoEntityPKList = new ArrayList<>();
				if (jobLinkRcv.getJobLinkExpList() != null
						&& jobLinkRcv.getJobLinkExpList().size() > 0) {
					for (JobLinkExpInfo jobLinkExpInfo : jobLinkRcv.getJobLinkExpList()) {
						JobLinkJobkickExpInfoEntityPK jobLinkJobkickExpInfoEntityPK 
							= new JobLinkJobkickExpInfoEntityPK(info.getId(), jobLinkExpInfo.getKey());
						JobLinkJobkickExpInfoEntity jobLinkJobkickExpInfoEntity 
							= em.find(JobLinkJobkickExpInfoEntity.class, jobLinkJobkickExpInfoEntityPK, 
									ObjectPrivilegeMode.MODIFY);
						if (jobLinkJobkickExpInfoEntity == null) {
							// 新規登録
							jobLinkJobkickExpInfoEntity 
								= new JobLinkJobkickExpInfoEntity(jobLinkJobkickExpInfoEntityPK);
							em.persist(jobLinkJobkickExpInfoEntity);
							jobLinkJobkickExpInfoEntity.relateToJobKickEntity(bean);
						}
						jobLinkJobkickExpInfoEntity.setValue(jobLinkExpInfo.getValue());
						jobLinkJobkickExpInfoEntityPKList.add(jobLinkJobkickExpInfoEntityPK);
					}
				}
				// 不要なJobRuntimeParamEntityを削除
				bean.deleteJobLinkJobkickExpInfoEntities(jobLinkJobkickExpInfoEntityPKList);
			}
			bean.setOwnerRoleId(info.getOwnerRoleId());
			bean.setUpdateDate(now);
			bean.setUpdateUser(loginUser);
		} catch (JobInfoNotFound e) {
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("modifyJobKick() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		if (jobkickType == JobKickConstant.TYPE_SCHEDULE) {
			// ジョブスケジュール定義、ジョブセッション事前生成のスケジュール定義の登録・削除
			registScheduler(info.isValid().booleanValue(), isPremakeSchedule, (JobSchedule)info);

			// ジョブセッション削除
			if (isDeletePremake) {
				try {
					// 既に作成されたジョブセッションを削除
					DeletePremakeWorker.deletePremake(info.getId());
				} catch (HinemosUnknown e) {
					m_log.error(e);
				}
			}
		} else if (jobkickType == JobKickConstant.TYPE_JOBLINKRCV) {
			// ジョブ連携受信実行契機スケジュール定義
			registJobLinkRcvScheduler(info.isValid().booleanValue(), (JobLinkRcv)info);
		}
	}

	/**
	 * ジョブスケジュール定義、ジョブセッション事前生成のスケジュール定義の登録・削除
	 * 
	 * @param isSchedule true:ジョブスケジュール定義の登録、false:削除
	 * @param isPremakeSession true:ジョブセッション事前生成のスケジュール定義の登録、false:削除
	 * @param info
	 */
	private void registScheduler(boolean isSchedule, boolean isPremakeSession, JobSchedule info) {

		if (isSchedule) {
			// ジョブスケジュール定義の登録
			JobTriggerInfo triggerInfo = new JobTriggerInfo();
			triggerInfo.setTrigger_type(JobTriggerTypeConstant.TYPE_SCHEDULE);
			triggerInfo.setTrigger_info(info.getName()+"("+info.getId()+")");
			
			//JobDetailに呼び出すメソッドの引数を設定
			Serializable[] jdArgs = new Serializable[QuartzConstant.ARGS_NUM];
			@SuppressWarnings("unchecked")
			Class<? extends Serializable>[] jdArgsType = new Class[QuartzConstant.ARGS_NUM];
			//ジョブユニットIDを設定
			jdArgs[QuartzConstant.INDEX_JOBUNIT_ID] = info.getJobunitId();
			jdArgsType[QuartzConstant.INDEX_JOBUNIT_ID] = String.class;
			
			//ジョブIDを設定
			jdArgs[QuartzConstant.INDEX_JOB_ID] = info.getJobId();
			jdArgsType[QuartzConstant.INDEX_JOB_ID] = String.class;
			
			//カレンダIDを設定
			jdArgs[QuartzConstant.INDEX_CALENDAR_ID] = info.getCalendarId();
			jdArgsType[QuartzConstant.INDEX_CALENDAR_ID] = String.class;
			
			//実行契機種別を設定
			jdArgs[QuartzConstant.INDEX_TRIGGER_TYPE] = triggerInfo.getTrigger_type();
			jdArgsType[QuartzConstant.INDEX_TRIGGER_TYPE] = Integer.class;
			
			//実行契機情報を設定
			jdArgs[QuartzConstant.INDEX_TRIGGER_INFO] = triggerInfo.getTrigger_info();
			jdArgsType[QuartzConstant.INDEX_TRIGGER_INFO] = String.class;
			
			//ファイル名を設定
			jdArgs[QuartzConstant.INDEX_TRIGGER_FILENAME] = triggerInfo.getFilename();
			jdArgsType[QuartzConstant.INDEX_TRIGGER_FILENAME] = String.class;
			
			//ディレクトリ名を設定
			jdArgs[QuartzConstant.INDEX_TRIGGER_DIRECTORY] = triggerInfo.getDirectory();
			jdArgsType[QuartzConstant.INDEX_TRIGGER_DIRECTORY] = String.class;
			
			//ジョブの待ち条件（時刻）の有効・無効を設定
			jdArgs[QuartzConstant.INDEX_TRIGGER_JOB_WAIT_TIME] = triggerInfo.getJobWaitTime();
			jdArgsType[QuartzConstant.INDEX_TRIGGER_JOB_WAIT_TIME] = Boolean.class;
			
			//ジョブの待ち条件（ジョブセッション開始後の時間の有効・無効を設定
			jdArgs[QuartzConstant.INDEX_TRIGGER_JOB_WAIT_MINUTE] = triggerInfo.getJobWaitMinute();
			jdArgsType[QuartzConstant.INDEX_TRIGGER_JOB_WAIT_MINUTE] = Boolean.class;
			
			//ジョブの起動コマンドの置換の有無を設定
			jdArgs[QuartzConstant.INDEX_TRIGGER_JOB_COMMAND] = triggerInfo.getJobCommand();
			jdArgsType[QuartzConstant.INDEX_TRIGGER_JOB_COMMAND] = Boolean.class;
			
			//ジョブの起動コマンドの置換の有無を設定
			jdArgs[QuartzConstant.INDEX_TRIGGER_JOB_COMMAND_TEXT] = triggerInfo.getJobCommandText();
			jdArgsType[QuartzConstant.INDEX_TRIGGER_JOB_COMMAND_TEXT] = String.class;
			
			//ジョブ実行契機IDを設定
			jdArgs[QuartzConstant.INDEX_TRIGGER_JOBKICK_ID] = info.getId();
			jdArgsType[QuartzConstant.INDEX_TRIGGER_JOBKICK_ID] = String.class;
		
			//ジョブ実行予定時間を設定
			jdArgs[QuartzConstant.INDEX_EXECUTE_TIME] = 0L;
			jdArgsType[QuartzConstant.INDEX_EXECUTE_TIME] = Long.class;
			

			try {
				if (info.getScheduleType() == ScheduleConstant.TYPE_INTERVAL) {

					// 「時」は23時以下になるまで元の数値から24を引く
					int hour = info.getHour();
					while (hour >= 24) {
						hour = hour - 24;
					}

					// スケジュール開始日時（日付は現在時刻から取得）
					Calendar startCalendar = HinemosTime.getCalendarInstance();
					startCalendar.set(Calendar.HOUR_OF_DAY, hour);
					startCalendar.set(Calendar.MINUTE, info.getMinute());
					startCalendar.set(Calendar.SECOND, 0);
					long startDate = startCalendar.getTime().getTime();

					// 現在時刻を過ぎていたら、翌日にする
					if (startDate < HinemosTime.currentTimeMillis()) {
						startDate += (24 * 60 * 60 * 1000);
					}

					// 繰り返し間隔（秒）
					int intervalSec = info.getEveryXminutes() * 60;

					SchedulerPlugin.scheduleSimpleIntervalJob(SchedulerPlugin.toSchedulerTypeForDBMS(QuartzConstant.GROUP_NAME),
							info.getId(), QuartzConstant.GROUP_NAME, startDate, intervalSec, true,
							JobControllerBean.class.getName(), QuartzConstant.METHOD_NAME, jdArgsType, jdArgs);
				} else {
					//Cron表記へ変換
					String cronString = QuartzUtil.getCronString(
							((JobSchedule)info).getScheduleType(),
							((JobSchedule)info).getWeek(),
							((JobSchedule)info).getHour(),
							((JobSchedule)info).getMinute(),
							((JobSchedule)info).getFromXminutes(),
							((JobSchedule)info).getEveryXminutes());

					m_log.trace("CronString =" + cronString);

					SchedulerPlugin.scheduleCronJob(SchedulerPlugin.toSchedulerTypeForDBMS(QuartzConstant.GROUP_NAME), info.getId(), QuartzConstant.GROUP_NAME, HinemosTime.currentTimeMillis() + 15 * 1000, cronString,
							true, JobControllerBean.class.getName(), QuartzConstant.METHOD_NAME, jdArgsType, jdArgs);
				}
			} catch (HinemosUnknown e) {
				m_log.error(e);
			}
		} else {
			// ジョブスケジュール定義の削除
			try {
				SchedulerPlugin.deleteJob(SchedulerPlugin.toSchedulerTypeForDBMS(QuartzConstant.GROUP_NAME), info.getId(), QuartzConstant.GROUP_NAME);
			} catch (HinemosUnknown e) {
				m_log.error(e);
			}
		}

		if (isPremakeSession) {
			// ジョブセッション事前生成のスケジュール定義の登録
			JobTriggerInfo triggerInfoJobpremake = new JobTriggerInfo();
			triggerInfoJobpremake.setTrigger_type(JobTriggerTypeConstant.TYPE_PREMAKESESSION);
			triggerInfoJobpremake.setTrigger_info(info.getName()+"("+info.getId()+")");
		
			//JobDetailに呼び出すメソッドの引数を設定
			Serializable[] jdArgsJobpremake = new Serializable[QuartzConstant.ARGS_NUM_FOR_JOBPREMAKE];
			@SuppressWarnings("unchecked")
			Class<? extends Serializable>[] jdArgsTypeJobpremake = new Class[QuartzConstant.ARGS_NUM_FOR_JOBPREMAKE];
			
			// ジョブ実行契機ID
			jdArgsJobpremake[QuartzConstant.INDEX_JOBKICK_ID_FOR_JOBPREMAKE] = info.getId();
			jdArgsTypeJobpremake[QuartzConstant.INDEX_JOBKICK_ID_FOR_JOBPREMAKE] = String.class;
			
			//　実行契機種別を設定
			jdArgsJobpremake[QuartzConstant.INDEX_TRIGGER_TYPE_FOR_JOBPREMAKE] = triggerInfoJobpremake.getTrigger_type();
			jdArgsTypeJobpremake[QuartzConstant.INDEX_TRIGGER_TYPE_FOR_JOBPREMAKE] = Integer.class;
			
			// 実行契機情報
			jdArgsJobpremake[QuartzConstant.INDEX_TRIGGER_INFO_FOR_JOBPREMAKE] = triggerInfoJobpremake.getTrigger_info();
			jdArgsTypeJobpremake[QuartzConstant.INDEX_TRIGGER_INFO_FOR_JOBPREMAKE] = String.class;
		
			// ジョブセッション作成範囲（処理日時～処理日時＋ELAPSED_TIME）
			jdArgsJobpremake[QuartzConstant.INDEX_PREMAKE_ELAPSED_TIME_FOR_JOBPREMAKE] = calculateElapsedTime((JobSchedule)info);
			jdArgsTypeJobpremake[QuartzConstant.INDEX_PREMAKE_ELAPSED_TIME_FOR_JOBPREMAKE] = Long.class;
			
			// スケジューラ実行予定時間
			jdArgsJobpremake[QuartzConstant.INDEX_EXECUTE_TIME_FOR_JOBPREMAKE] = triggerInfoJobpremake.getExecuteTime();
			jdArgsTypeJobpremake[QuartzConstant.INDEX_EXECUTE_TIME_FOR_JOBPREMAKE] = Long.class;
		
			// スケジュール定義を登録
			try {
				if (((JobSchedule)info).getSessionPremakeScheduleType() == SessionPremakeScheduleType.TYPE_DATETIME) {
					SchedulerPlugin.scheduleSimpleJob(
							SchedulerPlugin.toSchedulerTypeForDBMS(QuartzConstant.GROUP_NAME_FOR_JOBPREMAKE),
							info.getId(),
						QuartzConstant.GROUP_NAME_FOR_JOBPREMAKE,
						((JobSchedule)info).getSessionPremakeDate(),
						RunInterval.TYPE_MIN_60.toSec(),
						false,
						JobControllerBean.class.getName(),
						QuartzConstant.METHOD_NAME_FOR_JOBPREMAKE_ONCE,
						jdArgsTypeJobpremake,
						jdArgsJobpremake);
				} else {
					//Cron表記へ変換
					String cronStringJobpremake = QuartzUtil.getCronStringJobpremake(
							((JobSchedule)info).getSessionPremakeScheduleType(),
							((JobSchedule)info).getSessionPremakeWeek(),
							((JobSchedule)info).getSessionPremakeHour(),
							((JobSchedule)info).getSessionPremakeMinute(),
							((JobSchedule)info).getSessionPremakeEveryXHour());
	
					m_log.trace("cronStringJobpremake =" + cronStringJobpremake);

					long startTime = HinemosTime.currentTimeMillis() + 30 * 1000;
					if (((JobSchedule)info).getSessionPremakeScheduleType() == SessionPremakeScheduleType.TYPE_TIME) {
						Calendar cal = Calendar.getInstance();
						cal.setTime(new Date(startTime));
						cal.set(Calendar.HOUR_OF_DAY, ((JobSchedule)info).getSessionPremakeHour());
						cal.set(Calendar.MINUTE, ((JobSchedule)info).getSessionPremakeMinute());
						cal.set(Calendar.SECOND, 0);
						if (cal.getTimeInMillis() < startTime) {
							cal.add(Calendar.DATE, 1);
						}
						cal.add(Calendar.SECOND, -1);
						startTime = cal.getTimeInMillis();
					}
					SchedulerPlugin.scheduleCronJob(SchedulerPlugin.toSchedulerTypeForDBMS(QuartzConstant.GROUP_NAME_FOR_JOBPREMAKE), info.getId(),
							QuartzConstant.GROUP_NAME_FOR_JOBPREMAKE, startTime, cronStringJobpremake,
							true, JobControllerBean.class.getName(), QuartzConstant.METHOD_NAME_FOR_JOBPREMAKE, jdArgsTypeJobpremake, jdArgsJobpremake);
				}
			} catch (HinemosUnknown e) {
				m_log.error(e);
			}
		} else {
			try {
				SchedulerPlugin.deleteJob(
						SchedulerPlugin.toSchedulerTypeForDBMS(QuartzConstant.GROUP_NAME_FOR_JOBPREMAKE),
						info.getId(),
						QuartzConstant.GROUP_NAME_FOR_JOBPREMAKE);
			} catch (HinemosUnknown e) {
				m_log.error(e);
			}
		}
	}

	/**
	 * ジョブ連携受信実行契機のスケジュール定義の登録・削除
	 * 
	 * @param isSchedule true:スケジュール定義の登録、false:削除
	 * @param info
	 */
	private void registJobLinkRcvScheduler(boolean isSchedule, JobLinkRcv info) throws HinemosUnknown {

		if (isSchedule) {
			// ジョブ連携受信実行契機のスケジュール定義の登録
			Serializable[] jdArgs = new Serializable[QuartzConstant.ARGS_NUM_FOR_JOBLINKRCV];
			@SuppressWarnings("unchecked")
			Class<? extends Serializable>[] jdArgsType = new Class[QuartzConstant.ARGS_NUM_FOR_JOBLINKRCV];
			
			// ジョブ実行契機ID
			jdArgs[QuartzConstant.INDEX_JOBKICK_ID_FOR_JOBLINKRCV] = info.getId();
			jdArgsType[QuartzConstant.INDEX_JOBKICK_ID_FOR_JOBLINKRCV] = String.class;
			
			// 開始日時
			jdArgs[QuartzConstant.INDEX_EXECUTE_TIME_FOR_JOBLINKRCV] = 0L;
			jdArgsType[QuartzConstant.INDEX_EXECUTE_TIME_FOR_JOBLINKRCV] = Long.class;
			
			// 前回実行日時
			jdArgs[QuartzConstant.INDEX_PREVIOUS_FIRE_TIME_FOR_JOBLINKRCV] = 0L;
			jdArgsType[QuartzConstant.INDEX_PREVIOUS_FIRE_TIME_FOR_JOBLINKRCV] = Long.class;

			// スケジューラ開始日時
			Long startDate = (Long)(HinemosTime.currentTimeMillis() / JobLinkConstant.RCV_TARGET_TIME_PERIOD) * JobLinkConstant.RCV_TARGET_TIME_PERIOD
					+ JobLinkConstant.RCV_TARGET_TIME_PERIOD;

			SchedulerPlugin.scheduleSimpleJob(
				SchedulerPlugin.toSchedulerTypeForDBMS(QuartzConstant.GROUP_NAME_FOR_JOBLINKRCV),
				info.getId(),
				QuartzConstant.GROUP_NAME_FOR_JOBLINKRCV,
				startDate,
				RunInterval.TYPE_MIN_01.toSec(),
				true,
				JobControllerBean.class.getName(),
				QuartzConstant.METHOD_NAME_FOR_JOBLINKRCV,
				jdArgsType,
				jdArgs);
		} else {
			// スケジュール定義の削除
			try {
				SchedulerPlugin.deleteJob(SchedulerPlugin.toSchedulerTypeForDBMS(QuartzConstant.GROUP_NAME_FOR_JOBLINKRCV), info.getId(), QuartzConstant.GROUP_NAME_FOR_JOBLINKRCV);
			} catch (HinemosUnknown e) {
				m_log.error(e);
			}
		}
	}

	/**
	 * ジョブ実行契機を削除します。
	 * スケジュールの場合は、スケジュール情報を基にQuartzに登録したジョブを削除します。
	 *
	 * @param jobkickId ジョブ実行契機ID
	 * @param jobkickType ジョブ実行契機種別
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws ObjectPrivilege_InvalidRole
	 *
	 * @see com.clustercontrol.jobmanagement.bean.QuartzConstant
	 * @see com.clustercontrol.jobmanagement.util.QuartzUtil#getQuartzManager()
	 * @see com.clustercontrol.quartzmanager.ejb.session.QuartzManager#deleteSchedule(java.lang.String, java.lang.String)
	 */
	public void deleteJobKick(final String jobkickId, Integer jobkickType) throws HinemosUnknown, JobInfoNotFound, ObjectPrivilege_InvalidRole {
		// スケジュール定義を削除
		m_log.debug("deleteJobKick() : id=" + jobkickId);

		// DBのスケジュール情報を削除
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			//削除対象を検索
			JobKickEntity jobKickEntity = em.find(JobKickEntity.class, jobkickId,
					ObjectPrivilegeMode.MODIFY);
			if (jobKickEntity == null) {
				JobInfoNotFound e = new JobInfoNotFound("JobKickEntity.findByPrimaryKey");
				m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			//削除
			em.remove(jobKickEntity);
		} catch (JobInfoNotFound e) {
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("deleteSchedule() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
		
		if (jobkickType != null) {
			if (jobkickType == JobKickConstant.TYPE_SCHEDULE) {
				// スケジュールの場合
				try {
					m_log.debug("deleteJobKick() : id=" + jobkickId);
					// CronTriggerを削除
					SchedulerPlugin.deleteJob(SchedulerPlugin.toSchedulerTypeForDBMS(QuartzConstant.GROUP_NAME), jobkickId, QuartzConstant.GROUP_NAME);
					SchedulerPlugin.deleteJob(SchedulerPlugin.toSchedulerTypeForDBMS(QuartzConstant.GROUP_NAME_FOR_JOBPREMAKE), jobkickId, QuartzConstant.GROUP_NAME_FOR_JOBPREMAKE);
					// 既に作成されたジョブセッションを削除
					DeletePremakeWorker.deletePremake(jobkickId);
	
				} catch (HinemosUnknown e) {
					m_log.error(e);
				}
			} else if (jobkickType == JobKickConstant.TYPE_JOBLINKRCV) {
				// ジョブ連携受信実行契機の場合
				try {
					m_log.debug("deleteJobKick() : id=" + jobkickId);
					// CronTriggerを削除
					SchedulerPlugin.deleteJob(SchedulerPlugin.toSchedulerTypeForDBMS(QuartzConstant.GROUP_NAME_FOR_JOBLINKRCV), jobkickId, QuartzConstant.GROUP_NAME_FOR_JOBLINKRCV);	
				} catch (HinemosUnknown e) {
					m_log.error(e);
				}
			}
		}
	}

	/**
	 * ジョブセッション事前生成スケジュール毎の終了時間-開始時間を計算する
	 * 
	 * @param info ジョブスケジュール
	 * @return 終了時間-開始時間
	 */
	private long calculateElapsedTime(JobSchedule info) {
		Date currentDatetime = HinemosTime.getDateInstance();
		Calendar calendarFromTime = Calendar.getInstance();
		calendarFromTime.setTime(currentDatetime);
		Calendar calendarToTime = Calendar.getInstance();
		calendarFromTime.setTime(currentDatetime);
		if (info.getSessionPremakeScheduleType() == SessionPremakeScheduleType.TYPE_EVERY_DAY) {
			// 毎日
			calendarToTime.add(Calendar.DATE, 1);
			calendarToTime.add(Calendar.MINUTE, -1);
		} else if (info.getSessionPremakeScheduleType() == SessionPremakeScheduleType.TYPE_EVERY_WEEK) {
			// 毎週
			calendarToTime.add(Calendar.WEEK_OF_YEAR, 1);
			calendarToTime.add(Calendar.MINUTE, -1);
		} else if (info.getSessionPremakeScheduleType() == SessionPremakeScheduleType.TYPE_TIME) {
			// 毎時
			calendarToTime.add(Calendar.HOUR, info.getSessionPremakeEveryXHour());
			calendarToTime.add(Calendar.MINUTE, -1);
		} else if (info.getSessionPremakeScheduleType() == SessionPremakeScheduleType.TYPE_DATETIME) {
			// 日時
			calendarFromTime.setTime(new Date(info.getSessionPremakeDate()));
			calendarToTime.setTime(new Date(info.getSessionPremakeToDate()));
		}
		return calendarToTime.getTimeInMillis() - calendarFromTime.getTimeInMillis();
	}

	/**
	 * 指定された日時以前の事前生成ジョブセッションの削除を行う
	 * 
	 * @param jobkickId 実行契機ID
	 * @param ownerRoleId オーナーロールID
	 */
	public int deleteJobSession(Long scheduleDate, String ownerRoleId) throws HinemosUnknown {
		m_log.debug("deleteJobSession() start : scheduleDate=" + scheduleDate);

		int cnt = 0;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			synchronized (MaintenanceJob._deleteLock) {
				// 削除対象取得
				List<JobSessionEntity> list = null;
				if (ownerRoleId == null) {
					list = QueryUtil.getJobSessionListByDateAndStatus(scheduleDate, 
							StatusConstant.TYPE_SCHEDULED);
				} else {
					list = QueryUtil.getJobSessionListDateStatusAndOwnerRoleId(scheduleDate, 
							StatusConstant.TYPE_SCHEDULED, ownerRoleId);
				}
				// 削除処理
				for (JobSessionEntity entity : list) {
					ILockManager lm = LockManagerFactory.instance().create();
					lm.delete(JobSessionImpl.class.getName() + "-" + entity.getSessionId());
					em.remove(entity);
					cnt++;
				}
			}
		} catch (Exception e) {
			String countMessage = "delete count : " + cnt + " records" + "\n";
			m_log.warn("deleteJobSession() : " + countMessage
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
		return cnt;
	}
}
