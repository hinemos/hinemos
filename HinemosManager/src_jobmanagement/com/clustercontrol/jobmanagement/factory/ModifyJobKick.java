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

import java.io.Serializable;
import java.util.ArrayList;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.scheduler.QuartzUtil;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.JobKickDuplicate;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.jobmanagement.bean.JobFileCheck;
import com.clustercontrol.jobmanagement.bean.JobKick;
import com.clustercontrol.jobmanagement.bean.JobKickConstant;
import com.clustercontrol.jobmanagement.bean.JobRuntimeParam;
import com.clustercontrol.jobmanagement.bean.JobRuntimeParamDetail;
import com.clustercontrol.jobmanagement.bean.JobSchedule;
import com.clustercontrol.jobmanagement.bean.JobTriggerInfo;
import com.clustercontrol.jobmanagement.bean.JobTriggerTypeConstant;
import com.clustercontrol.jobmanagement.bean.QuartzConstant;
import com.clustercontrol.jobmanagement.model.JobKickEntity;
import com.clustercontrol.jobmanagement.model.JobRuntimeParamDetailEntity;
import com.clustercontrol.jobmanagement.model.JobRuntimeParamDetailEntityPK;
import com.clustercontrol.jobmanagement.model.JobRuntimeParamEntity;
import com.clustercontrol.jobmanagement.model.JobRuntimeParamEntityPK;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.plugin.impl.SchedulerPlugin;
import com.clustercontrol.plugin.impl.SchedulerPlugin.SchedulerType;
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
		JpaTransactionManager jtm = new JpaTransactionManager();
		//最終更新日時を設定
		long now = HinemosTime.currentTimeMillis();
		// DBにスケジュール情報を保存
		try {
			// IDの重複チェック
			String id = info.getId();
			jtm.checkEntityExists(JobKickEntity.class, id);

			// 値設定
			JobKickEntity jobKickEntity = new JobKickEntity(info.getId());
			jobKickEntity.setJobkickName(info.getName());
			jobKickEntity.setJobkickType(jobkickType);
			jobKickEntity.setJobunitId(info.getJobunitId());
			jobKickEntity.setJobId(info.getJobId());

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
					
					// ランタイムジョブ変数詳細情報
					if (jobRuntimeParam.getJobRuntimeParamDetailList() != null
							&& jobRuntimeParam.getJobRuntimeParamDetailList().size() > 0) {
						int detailIdx = 0;
						for (JobRuntimeParamDetail jobRuntimeParamDetail 
								: jobRuntimeParam.getJobRuntimeParamDetailList()) {
							JobRuntimeParamDetailEntity jobRuntimeParamDetailEntity
								= new JobRuntimeParamDetailEntity(jobRuntimeParamEntity, 
										detailIdx);
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
			} else if (jobkickType == JobKickConstant.TYPE_MANUAL) {
				jobKickEntity.setCalendarId(null);
				jobKickEntity.setValidFlg(true);
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
			// ジョブスケジュールの場合、実行契機情報の作成
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
			jdArgs[QuartzConstant.INDEX_TRIGGER_JOB_WAIT_MINUTE] = triggerInfo.getJobWaitTime();
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
			
			//Cron表記へ変換
			String cronString = QuartzUtil.getCronString(
					((JobSchedule)info).getScheduleType(),
					((JobSchedule)info).getWeek(),
					((JobSchedule)info).getHour(),
					((JobSchedule)info).getMinute(),
					((JobSchedule)info).getFromXminutes(),
					((JobSchedule)info).getEveryXminutes());
			
			m_log.trace("CronString =" + cronString);
			
			// スケジュール定義を登録
			try {
				if (info.isValid().booleanValue()) {
					SchedulerPlugin.scheduleCronJob(SchedulerType.DBMS, info.getId(), QuartzConstant.GROUP_NAME, HinemosTime.currentTimeMillis() + 15 * 1000, cronString,
							true, JobControllerBean.class.getName(), QuartzConstant.METHOD_NAME, jdArgsType, jdArgs);
				} else {
					SchedulerPlugin.deleteJob(SchedulerType.DBMS, info.getId(), QuartzConstant.GROUP_NAME);
				}
			} catch (HinemosUnknown e) {
				m_log.error(e);
			}
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
		JpaTransactionManager jtm = new JpaTransactionManager();
		HinemosEntityManager em = jtm.getEntityManager();
		//最終更新日時を設定
		long now = HinemosTime.currentTimeMillis();
		// DBにスケジュール情報を保存
		try {
			JobKickEntity bean  = em.find(JobKickEntity.class, info.getId(),
					ObjectPrivilegeMode.MODIFY);
			if (bean == null) {
				JobInfoNotFound e = new JobInfoNotFound("JobKickEntity.findByPrimaryKey");
				m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			bean.setJobkickId(info.getId());
			bean.setJobkickName(info.getName());
			bean.setJobunitId(info.getJobunitId());
			bean.setJobId(info.getJobId());

			// ランタイムジョブ変数情報
			if (info.getJobRuntimeParamList() != null
					&& info.getJobRuntimeParamList().size() > 0) {
				ArrayList<JobRuntimeParamEntityPK> jobRuntimeParamEntityPKList = new ArrayList<>();
				
				for (JobRuntimeParam jobRuntimeParam : info.getJobRuntimeParamList()) {
					JobRuntimeParamEntityPK jobRuntimeParamEntityPK 
						= new JobRuntimeParamEntityPK(info.getId(), jobRuntimeParam.getParamId());
					JobRuntimeParamEntity jobRuntimeParamEntity 
						= em.find(JobRuntimeParamEntity.class, jobRuntimeParamEntityPK, 
								ObjectPrivilegeMode.MODIFY);
					if (jobRuntimeParamEntity == null) {
						// 新規登録
						jobRuntimeParamEntity 
							= new JobRuntimeParamEntity(jobRuntimeParamEntityPK, bean);
					}
					jobRuntimeParamEntity.setParamType(jobRuntimeParam.getParamType());
					jobRuntimeParamEntity.setDefaultValue(jobRuntimeParam.getValue());
					jobRuntimeParamEntity.setDescription(jobRuntimeParam.getDescription());
					jobRuntimeParamEntity.setRequiredFlg(jobRuntimeParam.getRequiredFlg());
					jobRuntimeParamEntityPKList.add(jobRuntimeParamEntityPK);
					
					// ランタイムジョブ変数詳細情報
					if (jobRuntimeParam.getJobRuntimeParamDetailList() != null
							&& jobRuntimeParam.getJobRuntimeParamDetailList().size() > 0) {
						ArrayList<JobRuntimeParamDetailEntityPK> jobRuntimeParamDetailEntityPKList 
							= new ArrayList<>();
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
										jobRuntimeParamDetailEntityPK,
										jobRuntimeParamEntity);
							}
							jobRuntimeParamDetailEntity.setParamValue(
									jobRuntimeParamDetail.getParamValue());
							jobRuntimeParamDetailEntity.setDescription(
									jobRuntimeParamDetail.getDescription());
							jobRuntimeParamDetailEntityPKList.add(jobRuntimeParamDetailEntityPK);
							detailIdx++;
						}
						// 不要なJobRuntimeParamDetailEntityを削除
						jobRuntimeParamEntity.deleteJobRuntimeParamDetailEntities(jobRuntimeParamDetailEntityPKList);
					}
				}
				// 不要なJobRuntimeParamEntityを削除
				bean.deleteJobRuntimeParamEntities(jobRuntimeParamEntityPKList);
			}

			if (jobkickType == JobKickConstant.TYPE_SCHEDULE) {
				// ジョブスケジュール
				if (!(info instanceof JobSchedule)) {
					throw new HinemosUnknown("type error : " + info.getClass() + "!=JobSchedule");
				}
				JobSchedule jobSchedule = (JobSchedule)info;
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
			} else if (jobkickType == JobKickConstant.TYPE_MANUAL) {
				bean.setCalendarId(null);
				bean.setValidFlg(true);
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
			// ジョブスケジュールの場合、実行契機情報の作成
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
			
			//Cron表記へ変換
			String cronString = QuartzUtil.getCronString(
					((JobSchedule)info).getScheduleType(),
					((JobSchedule)info).getWeek(),
					((JobSchedule)info).getHour(),
					((JobSchedule)info).getMinute(),
					((JobSchedule)info).getFromXminutes(),
					((JobSchedule)info).getEveryXminutes());
	
			
			m_log.trace("CronString =" + cronString);
			
			// スケジュール定義を登録
			try {
				if (info.isValid().booleanValue()) {
					SchedulerPlugin.scheduleCronJob(SchedulerType.DBMS, info.getId(), QuartzConstant.GROUP_NAME, HinemosTime.currentTimeMillis() + 15 * 1000, cronString,
							true, JobControllerBean.class.getName(), QuartzConstant.METHOD_NAME, jdArgsType, jdArgs);
				} else {
					SchedulerPlugin.deleteJob(SchedulerType.DBMS, info.getId(), QuartzConstant.GROUP_NAME);
				}
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

		JpaTransactionManager jtm = new JpaTransactionManager();
		HinemosEntityManager em = jtm.getEntityManager();
		// DBのスケジュール情報を削除
		try {
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
		
		if (jobkickType == JobKickConstant.TYPE_SCHEDULE) {
			// スケジュールの場合、CronTriggerを削除
			try {
				m_log.debug("deleteJob() : id=" + jobkickId);
				SchedulerPlugin.deleteJob(SchedulerType.DBMS, jobkickId, QuartzConstant.GROUP_NAME);
			} catch (HinemosUnknown e) {
				m_log.error(e);
			}
		}
	}
}
