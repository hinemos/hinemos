/*

Copyright (C) since 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.util;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.ScheduleConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.IconFileNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.JobInvalid;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.jobmanagement.bean.JobCommandInfo;
import com.clustercontrol.jobmanagement.bean.JobCommandParam;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobEndStatusInfo;
import com.clustercontrol.jobmanagement.bean.JobEnvVariableInfo;
import com.clustercontrol.jobmanagement.bean.JobFileCheck;
import com.clustercontrol.jobmanagement.bean.JobFileInfo;
import com.clustercontrol.jobmanagement.bean.JobInfo;
import com.clustercontrol.jobmanagement.bean.JobKick;
import com.clustercontrol.jobmanagement.bean.JobKickConstant;
import com.clustercontrol.jobmanagement.bean.JobObjectInfo;
import com.clustercontrol.jobmanagement.bean.JobRuntimeParam;
import com.clustercontrol.jobmanagement.bean.JobRuntimeParamDetail;
import com.clustercontrol.jobmanagement.bean.JobRuntimeParamTypeConstant;
import com.clustercontrol.jobmanagement.bean.JobSchedule;
import com.clustercontrol.jobmanagement.bean.JobTreeItem;
import com.clustercontrol.jobmanagement.bean.JobWaitRuleInfo;
import com.clustercontrol.jobmanagement.bean.JobmapIconImage;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.jobmanagement.bean.MonitorJobInfo;
import com.clustercontrol.jobmanagement.bean.OperationConstant;
import com.clustercontrol.jobmanagement.bean.SystemParameterConstant;
import com.clustercontrol.jobmanagement.factory.SelectJobmap;
import com.clustercontrol.jobmanagement.model.JobKickEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntityPK;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.util.MessageConstant;

/**
 * ジョブ管理の入力チェッククラス
 * 
 * @since 4.0
 */
public class JobValidator {
	private static Log m_log = LogFactory.getLog( JobValidator.class );

	// 待ち条件,開始遅延,終了遅延で設定する日時の最大,最小値
	private static final long DATETIME_VALUE_MIN = -392399000L; //「-99:59:59」のエポック秒
	private static final long DATETIME_VALUE_MAX = 3567599000L; //「999:59:59」のエポック秒
	private static final String DATETIME_STRING_MIN = "-99:59:59"; //日時下限越えエラー通知用文字列
	private static final String DATETIME_STRING_MAX = "999:59:59"; //日時上限越えエラー通知用文字列
	
	/**
	 * 実行契機のvalidate
	 * @param JobKick
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public static void validateJobKick(JobKick jobKick) throws InvalidSetting, HinemosUnknown, InvalidRole {
		String id = jobKick.getId();
		// jobkickId
		CommonValidator.validateId(MessageConstant.JOBKICK_ID.getMessage(), id, 64);
		// jobkickName
		CommonValidator.validateString(MessageConstant.JOBKICK_NAME.getMessage(), jobKick.getName(), true, 1, 64);
		// ownerRoleId
		CommonValidator.validateOwnerRoleId(jobKick.getOwnerRoleId(), true, jobKick.getId(), HinemosModuleConstant.JOB_KICK);
		// jobid
		validateJobId(jobKick.getJobunitId(),jobKick.getJobId(), jobKick.getOwnerRoleId());

		if (jobKick.getType() != JobKickConstant.TYPE_MANUAL) {
			// calenderId
			CommonValidator.validateCalenderId(jobKick.getCalendarId(), false, jobKick.getOwnerRoleId());
		}

		// jobRuntimeParamList
		if (jobKick.getJobRuntimeParamList() != null) {
			for (JobRuntimeParam jobRuntimeParam : jobKick.getJobRuntimeParamList()) {
				// paramId
				CommonValidator.validateId(MessageConstant.JOBKICK_PARAM_ID.getMessage(), jobRuntimeParam.getParamId(), 64);
				//paramType
				if (jobRuntimeParam.getParamType() != JobRuntimeParamTypeConstant.TYPE_INPUT
						&& jobRuntimeParam.getParamType() != JobRuntimeParamTypeConstant.TYPE_RADIO
						&& jobRuntimeParam.getParamType() != JobRuntimeParamTypeConstant.TYPE_COMBO
						&& jobRuntimeParam.getParamType() != JobRuntimeParamTypeConstant.TYPE_FIXED) {
					InvalidSetting e = new InvalidSetting("unknown jobkick type");
					m_log.info("validateJobKick() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				// defaultValue
				if (jobRuntimeParam.getParamType() == JobRuntimeParamTypeConstant.TYPE_FIXED
						|| jobRuntimeParam.getParamType() == JobRuntimeParamTypeConstant.TYPE_RADIO) {
					CommonValidator.validateString(MessageConstant.JOBKICK_DEFAULT_VALUE.getMessage(), jobRuntimeParam.getValue(), true, 1, 1024);
				}
				// description
				CommonValidator.validateString(MessageConstant.JOBKICK_DESCRIPTION.getMessage(), jobRuntimeParam.getDescription(), true, 1, 256);
				// requiredFlg
				if (jobRuntimeParam.getRequiredFlg() == null) {
					InvalidSetting e = new InvalidSetting("required flag is null");
					m_log.info("validateJobKick() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				// jobRuntimeParamDetailList
				if (jobRuntimeParam.getJobRuntimeParamDetailList() == null
						|| jobRuntimeParam.getJobRuntimeParamDetailList().size() == 0) {
					if (jobRuntimeParam.getParamType() == JobRuntimeParamTypeConstant.TYPE_RADIO
							|| jobRuntimeParam.getParamType() == JobRuntimeParamTypeConstant.TYPE_COMBO) {
						InvalidSetting e = new InvalidSetting("select item is null");
						m_log.info("validateJobKick() : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage());
						throw e;
					}
				} else {
					for (JobRuntimeParamDetail jobRuntimeParamDetail : jobRuntimeParam.getJobRuntimeParamDetailList()) {
						// paramValue
						CommonValidator.validateString(MessageConstant.JOBKICK_DETAIL_PARAM_VALUE.getMessage(), jobRuntimeParamDetail.getParamValue(), true, 1, 1024);
						// description
						CommonValidator.validateString(MessageConstant.JOBKICK_DETAIL_DESCRIPTION.getMessage(), jobRuntimeParamDetail.getDescription(), true, 1, 1024);
					}
				}
			}
		}
	}

	/**
	 * ジョブスケジュールのvalidate
	 * 
	 * @param jobSchedule
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public static void validateJobSchedule(JobSchedule jobSchedule) throws InvalidSetting, HinemosUnknown, InvalidRole {

		// jobkick
		validateJobKick(jobSchedule);

		/**
		 * スケジュール設定
		 */
		//p分かq分毎に繰り返し実行の場合
		if(jobSchedule.getScheduleType() == ScheduleConstant.TYPE_REPEAT){
			//「p分から」のバリデート
			Integer pMinute = jobSchedule.getFromXminutes();
			if (pMinute != null) {
				if (pMinute < 0 || 60 <= pMinute) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_FROM_MIN.getMessage());
					m_log.info("validateJobSchedule() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			} else {
				// 分は必須。
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_FROM_MIN.getMessage());
				m_log.info("validateJobSchedule() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			//「q分毎に」のバリデート
			Integer qMinute = jobSchedule.getEveryXminutes();
			if (qMinute != null) {
				if (qMinute <= 0 || 60 < qMinute || qMinute <= pMinute ||
						!(qMinute == 5 || qMinute == 10 || qMinute == 15 ||
						qMinute == 20 || qMinute == 30 || qMinute == 60)) { 
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_MIN_EACH.getMessage());
					m_log.info("validateJobSchedule() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			} else {
				// 分は必須。
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_MIN_EACH.getMessage());
				m_log.info("validateSchedule() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage() + "null");
				throw e;
			}
		}
		//上記以外の場合
		else {
			//3つに当てはまらない場合
			if (jobSchedule.getScheduleType() != ScheduleConstant.TYPE_DAY
					&& jobSchedule.getScheduleType() != ScheduleConstant.TYPE_WEEK) {
				InvalidSetting e = new InvalidSetting("unknown schedule type");
				m_log.info("validateJobSchedule() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			//曜日の場合
			if (jobSchedule.getScheduleType() == ScheduleConstant.TYPE_WEEK) {
				if (jobSchedule.getWeek() == null ||
						jobSchedule.getWeek() < 0 || 7 < jobSchedule.getWeek()) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_WEEK.getMessage());
					m_log.info("validateJobSchedule() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}

			/*
			 * 時のバリデート
			 * 「時」は、
			 * 「*」または、「00」 - 「48」が設定可能
			 * 「*」は「null」としてDBに格納される
			 */
			if (jobSchedule.getHour() != null) {
				if (jobSchedule.getHour() < 0 || 48 < jobSchedule.getHour()) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_HOUR.getMessage());
					m_log.info("validateJobSchedule() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			//分のバリデート
			if (jobSchedule.getMinute() != null) {
				if (jobSchedule.getMinute() < 0 || 60 < jobSchedule.getMinute()) {
					String[] args = {"0","59"};
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_RANGE_OVER.getMessage(args));
					m_log.info("validateJobSchedule()  "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			} else {
				// 分は必須。
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_MIN.getMessage());
				m_log.info("validateJobSchedule() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			//48:01以上は設定できない
			if (jobSchedule.getHour() != null && jobSchedule.getMinute() != null) {
				if (jobSchedule.getHour() == 48) {
					if (jobSchedule.getMinute() != 0) {
						String[] args = {"00:00","48:00"};
						InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_RANGE_OVER.getMessage(args));
						m_log.info("validateJobSchedule()  "
								+ e.getClass().getSimpleName() + ", " + e.getMessage());
						throw e;
					}
				}
			}
		}
	}

	/**
	 * 実行契機[ファイルチェック]のvalidate
	 * @param jobFileCheck
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public static void validateJobFileCheck(JobFileCheck jobFileCheck) throws InvalidSetting, HinemosUnknown, InvalidRole {
		// jobkick
		validateJobKick(jobFileCheck);

		// 実行するファシリティIDのチェック
		if(jobFileCheck.getFacilityId() == null || "".equals(jobFileCheck.getFacilityId())){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SCOPE.getMessage());
			m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}else{
			//ファシリティIDが正しい形式かチェック
			if(!SystemParameterConstant.isParam(
					jobFileCheck.getFacilityId(),
					SystemParameterConstant.FACILITY_ID)){
				try {
					FacilityTreeCache.validateFacilityId(jobFileCheck.getFacilityId(), jobFileCheck.getOwnerRoleId(), false);
				} catch (FacilityNotFound e) {
					InvalidSetting e1 = new InvalidSetting("FacilityId is not exist in repository. FacilityId = " + jobFileCheck.getFacilityId()
							+ ", JobFileCheck  = " + jobFileCheck.getId());
					m_log.info("validateJobFileCheck() : "
							+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
					throw e1;
				} catch (InvalidRole e) {
					throw e;
				} catch (Exception e) {
					m_log.warn("validateJobFileCheck() add job unknown error. FacilityId = " + jobFileCheck.getFacilityId()
							+ ", JobFileCheck  = " + jobFileCheck.getId()  + " : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw new HinemosUnknown("add job unknown error. FacilityId = " + jobFileCheck.getFacilityId() + ", JobFileCheck  = " + jobFileCheck.getId(), e);
				}
			}
		}

		//ディレクトリ
		if(jobFileCheck.getDirectory() == null || jobFileCheck.getDirectory().equals("")){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_DIR_NAME.getMessage());
			m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		//ファイル名
		if(jobFileCheck.getFileName() == null || jobFileCheck.getFileName().equals("")){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_FILE_NAME.getMessage());
			m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateString(MessageConstant.DIRECTORY.getMessage(), jobFileCheck.getDirectory(), true, 1, 1024);
		CommonValidator.validateString(MessageConstant.FILE_NAME.getMessage(), jobFileCheck.getFileName(), true, 1, 64);
	}

	/**
	 * ジョブIDの存在チェック
	 * @param jobunitId
	 * @param jobId
	 * @param isFlag true:参照権限関係無しに全件検索 false : 通常時
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public static void validateJobId (String jobunitId, String jobId,Boolean isFlag) throws InvalidSetting, InvalidRole {

		try {
			//参照権限関係無しに全件検索する場合
			if (isFlag) {
				QueryUtil.getJobMstPK_NONE(new JobMstEntityPK(jobunitId, jobId));
			}
			//参照権限あり
			else {
				QueryUtil.getJobMstPK(jobunitId, jobId);
			}
		} catch (InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.info("validateJobId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_JOB.getMessage() +
					" Target job is not exist! jobunitId = " + jobunitId +
					", jobId = " + jobId);
			throw e1;
		}
	}

	/**
	 * ジョブIDの存在チェック(オーナーロールIDによるチェック）
	 * @param jobunitId
	 * @param jobId
	 * @param ownerRoleId
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public static void validateJobId (String jobunitId, String jobId, String ownerRoleId) throws InvalidSetting, InvalidRole {

		try {
			QueryUtil.getJobMstPK_OR(jobunitId, jobId, ownerRoleId);
		} catch (InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.info("validateJobId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_JOB.getMessage() +
					" Target job is not exist! jobunitId = " + jobunitId +
					", jobId = " + jobId);
			throw e1;
		}
	}

	/**
	 * ジョブを登録、変更、削除した際に各種参照に問題がないか
	 * INSERT, UPDATE, DELETE直後に実行する。
	 * 
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 */
	public static void validateJobMaster() throws InvalidSetting, HinemosUnknown, JobInfoNotFound, InvalidRole {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		m_log.debug("validateJobMaster()");

		// ジョブ実行契機
		m_log.debug("validateJobMaster() jobschedule check start");
		try{
			Collection<JobKickEntity> jobKickList =
					em.createNamedQuery("JobKickEntity.findAll",
							JobKickEntity.class, ObjectPrivilegeMode.NONE).getResultList();

			if (jobKickList == null) {
				JobInfoNotFound je = new JobInfoNotFound("JobKickEntity.findAll");
				m_log.info("validateJobMaster() : "
						+ je.getClass().getSimpleName() + ", " + je.getMessage());
				throw je;
			}
			for(JobKickEntity jobKick : jobKickList){
				String jobunitId = jobKick.getJobunitId();
				String jobId = jobKick.getJobId();

				m_log.debug("validateJobMaster() target jobkick " + jobKick.getJobkickId() +
						", jobunitId = " + jobunitId + ", jobId = " + jobId);
				try{
					// jobunitId,jobidの存在チェック
					//true : 参照権限関係なしに全件検索する場合
					validateJobId(jobunitId,jobId,true);

					String[] args = {jobKick.getJobkickId()};
					m_log.debug(MessageConstant.MESSAGE_JOBTRIGGERTYPE_NOT_EXIST_REFERENCE.getMessage(args));
				} catch (InvalidSetting e) {
					// 削除対象のジョブツリーの中にジョブ実行契機からの参照がある
					String[] args = {jobKick.getJobkickId(), jobunitId, jobId};
					m_log.info("validateJobMaster() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw new InvalidSetting(MessageConstant.MESSAGE_DELETE_NG_JOBTRIGGERTYPE_REFERENCE.getMessage(args));
				}
			}
		} catch (InvalidSetting e) {
			throw e;
		} catch (JobInfoNotFound e) {
			throw e;
		} catch (InvalidRole e) {
			throw e;
		} catch (Exception e) {
			HinemosUnknown e1 = new HinemosUnknown(e.getMessage(), e);
			m_log.warn("validateJobMaster() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw e1;
		}
	}

	/**
	 * ジョブ定義のvalidate
	 * INSERT, UPDATE前に実行する。
	 * 
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws JobInvalid
	 */
	public static void validateJobUnit(JobTreeItem item) throws InvalidSetting, InvalidRole, HinemosUnknown, JobInvalid {
		validateJobInfo(item);
		validateDuplicateJobId(item);
		validateWaitRule(item);
		validateReferJob(item);
		validateReferJobNet(item);
	}


	/**
	 * ジョブマップ用イメージファイルのvalidate
	 * @param JobKick
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public static void validateJobmapIconImage(JobmapIconImage jobmapIconImage) throws InvalidSetting, HinemosUnknown, InvalidRole {
		// iconID
		CommonValidator.validateString(MessageConstant.FILE_NAME.getMessage(), jobmapIconImage.getIconId(), true, 1, 64);
		// description
		CommonValidator.validateString(MessageConstant.DESCRIPTION.getMessage(), jobmapIconImage.getDescription(), true, 1, 256);
		// filedata
		if (jobmapIconImage.getFiledata() == null) {
			InvalidSetting e = new InvalidSetting("filedata is not defined.");
			m_log.info("validateJobmapIconImage() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		// ownerRoleId
		CommonValidator.validateOwnerRoleId(jobmapIconImage.getOwnerRoleId(), true, jobmapIconImage.getIconId(), HinemosModuleConstant.JOBMAP_IMAGE_FILE);
	}

	/**
	 * ジョブにてジョブマップ用アイコンが参照状態であるか調査する。
	 * 参照状態の場合、メッセージダイアログが出力される。
	 * @param iconId アイコンID
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	public static void valideDeleteJobmapIconImage(String iconId) throws InvalidSetting, HinemosUnknown{
		try{
			if (iconId == null || iconId.equals("")) {
				InvalidSetting e = new InvalidSetting("iconId is not defined.");
				m_log.info("valideDeleteJobmapIconImage() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			// デフォルトのジョブマップアイコンイメージは削除対象外
			String defaultJobIconId =  new JobControllerBean().getJobmapIconIdJobDefault();
			String defaultJobnetIconId =  new JobControllerBean().getJobmapIconIdJobnetDefault();
			String defaultApprovalIconId = new JobControllerBean().getJobmapIconIdApprovalDefault();
			String defaultMonitorIconId = new JobControllerBean().getJobmapIconIdMonitorDefault();
			String defaultFileIconId = new JobControllerBean().getJobmapIconIdFileDefault();
			if (iconId.equals(defaultJobIconId) || iconId.equals(defaultJobnetIconId) 
					|| iconId.equals(defaultApprovalIconId) || iconId.equals(defaultMonitorIconId) 
					|| iconId.equals(defaultFileIconId)) {
				String[] args = {iconId};
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_DELETE_NG_ICONID_DEFAULT.getMessage(args));
				m_log.info("valideDeleteJobmapIconImage() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}

			//ジョブ
			List<JobMstEntity> jobMstList =
					QueryUtil.getJobMstEnityFindByIconId(iconId);
			if (jobMstList != null && jobMstList.size() > 0) {
				for(JobMstEntity jobMst : jobMstList){
					m_log.debug("valideDeleteJobmapIconImage() target JobMaster " + jobMst.getId().getJobId() + ", iconId = " + iconId);
					if(jobMst.getIconId() != null){
						String[] args = {jobMst.getId().getJobId(), iconId};
						throw new InvalidSetting(MessageConstant.MESSAGE_DELETE_NG_JOB_REFERENCE_TO_ICONFILE.getMessage(args));
					}
				}
			}
			
			// log.cc_job_infoから対象アイコンを参照していても関係なく削除するのでチェック不要
			// 指定されたlog.cc_job_infoのiconIdがアイコンリストに存在しない場合はデフォルトアイコンで表示する

		} catch (InvalidSetting e) {
			throw e;
		} catch (Exception e) {
			HinemosUnknown e1 = new HinemosUnknown(e.getMessage(), e);
			m_log.warn("valideDeleteJobmapIconImage() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw e1;
		}
	}

	private static void validateJobInfo(JobTreeItem item) throws InvalidSetting, InvalidRole, HinemosUnknown{

		if(item == null || item.getData() == null){
			m_log.warn("validateJobInfo is null");
			return ;
		}

		JobInfo jobInfo = item.getData();

		// ジョブID
		String jobId = jobInfo.getId();
		CommonValidator.validateId(MessageConstant.JOB_ID.getMessage(), jobId, 64);

		// ジョブユニットID
		String jobunitId = jobInfo.getJobunitId();
		CommonValidator.validateId(MessageConstant.JOBUNIT_ID.getMessage(), jobunitId, 64);

		// ジョブ名
		String jobName = jobInfo.getName();
		CommonValidator.validateString(MessageConstant.JOB_NAME.getMessage(), jobName, true, 1, 64);

		// 説明
		String description = jobInfo.getDescription();
		CommonValidator.validateString(MessageConstant.DESCRIPTION.getMessage(), description, true, 0, 256);

		// ownerRoleId
		CommonValidator.validateOwnerRoleId(jobInfo.getOwnerRoleId(), true,
				new JobMstEntityPK(jobunitId, jobId), HinemosModuleConstant.JOB);

		// ジョブユニットの場合は、jobIdとjobunitIdは一緒。
		if (jobInfo.getType() == JobConstant.TYPE_JOBUNIT) {
			if (!jobId.equals(jobunitId)) {
				InvalidSetting e = new InvalidSetting("jobType is TYPE_JOBUNIT, but jobId != jobunitId");
				m_log.info("validateJobUnit() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}

		// ジョブの場合は、ファシリティIDの存在チェック
		if (jobInfo.getType() == JobConstant.TYPE_JOB) {
			JobCommandInfo command = jobInfo.getCommand();

			// 実行するファシリティIDのチェック
			if(command.getFacilityID() == null || "".equals(command.getFacilityID())){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SCOPE.getMessage());
				m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}else{

				// ジョブ変数でない場合は、ファシリティIDのチェックを行う
				if(!SystemParameterConstant.isParam(
						command.getFacilityID(),
						SystemParameterConstant.FACILITY_ID)){
					try {
						FacilityTreeCache.validateFacilityId(command.getFacilityID(), jobInfo.getOwnerRoleId(), false);
					} catch (FacilityNotFound e) {
						InvalidSetting e1 = new InvalidSetting("FacilityId is not exist in repository. FacilityId = " + command.getFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId);
						m_log.info("validateJobUnit() : "
								+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
						throw e1;
					} catch (InvalidRole e) {
						throw e;
					} catch (Exception e) {
						m_log.warn("validateJobUnit() add job unknown error. FacilityId = " + command.getFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId + " : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
						throw new HinemosUnknown("add job unknown error. FacilityId = " + command.getFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId, e);

					}
				}
				try {
					// 試行回数の未設定時(インポート時を想定)
					if (command.getMessageRetry() == null || command.getCommandRetry() == null) {
						String message = "validateJobUnit() messageRetry or commandRetry is null(job). messageRetry =" + command.getMessageRetry()
								+ ", commandRetry =" + command.getCommandRetry();
						m_log.info(message);
						throw new InvalidSetting(message);
					}

					// 試行回数のチェック
					CommonValidator.validateInt(MessageConstant.JOB_RETRIES.getMessage(), command.getMessageRetry(), 1, DataRangeConstant.SMALLINT_HIGH);

					if (command.getCommandRetryFlg()) {
						CommonValidator.validateInt(MessageConstant.JOB_RETRIES.getMessage(), command.getCommandRetry(), 1, DataRangeConstant.SMALLINT_HIGH);
					}
				} catch (Exception e) {
					m_log.info("validateJobUnit() add job retry error. FacilityId = " + command.getFacilityID() + ", jobunitId = " + jobunitId + ", jobId = "
							+ jobId + ",messageRetry =" + command.getMessageRetry() + ",commandRetry =" + command.getCommandRetry() + ",commandRetryFlg ="
							+ command.getCommandRetryFlg() + " : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw e;
				}
				
				// スクリプト配布のチェック
				// スクリプトの最大サイズはHinemosプロパティから取得
				int scriptMaxSize = HinemosPropertyUtil.getHinemosPropertyNum("job.script.maxsize", Long.valueOf(8192)).intValue();
				if(command.getManagerDistribution()) {
					// スクリプト名
					String scriptName = command.getScriptName();
					CommonValidator.validateString(MessageConstant.JOB_SCRIPT_NAME.getMessage(), scriptName, true, 1, 256);
					// エンコーディング
					String scriptEncoding = command.getScriptEncoding();
					CommonValidator.validateString(MessageConstant.JOB_SCRIPT_ENCODING.getMessage(), scriptEncoding, true, 1, 32);
					// スクリプト
					String scriptContent = command.getScriptContent();
					CommonValidator.validateString(MessageConstant.JOB_SCRIPT.getMessage(), scriptContent, true, 1, scriptMaxSize);
				} else {
					// スクリプト名
					String scriptName = command.getScriptName();
					CommonValidator.validateString(MessageConstant.JOB_SCRIPT_NAME.getMessage(), scriptName, false, 0, 256);
					// エンコーディング
					String scriptEncoding = command.getScriptEncoding();
					CommonValidator.validateString(MessageConstant.JOB_SCRIPT_ENCODING.getMessage(), scriptEncoding, false, 0, 32);
					// スクリプト
					String scriptContent = command.getScriptContent();
					CommonValidator.validateString(MessageConstant.JOB_SCRIPT.getMessage(), scriptContent, false, 0, scriptMaxSize);
				}
				
				// ジョブ変数のチェック
				ArrayList<JobCommandParam> jobCommandParamList = jobInfo.getCommand().getJobCommandParamList();
				if(jobCommandParamList != null && jobCommandParamList.size() > 0) {
					for (JobCommandParam jobCommandParam : jobCommandParamList) {
						// パラメータID
						String paramId = jobCommandParam.getParamId();
						CommonValidator.validateId(MessageConstant.JOB_PARAM_ID.getMessage(), paramId, 64);
						// 値
						String jobCommandParamValue = jobCommandParam.getValue();
						CommonValidator.validateString(MessageConstant.JOB_PARAM_VALUE.getMessage(), jobCommandParamValue, true, 1, 256);
					}
				}
				
				// 環境変数のチェック
				List<JobEnvVariableInfo> envInfoList = command.getEnvVariableInfo();
				if(envInfoList != null && envInfoList.size() > 0) {
					for (JobEnvVariableInfo envInfo : envInfoList) {
						// 名前
						String envId = envInfo.getEnvVariableId();
						CommonValidator.validateId(MessageConstant.JOB_ENV_ID.getMessage(), envId, 64);
						// 値
						String envValue = envInfo.getValue();
						CommonValidator.validateString(MessageConstant.JOB_ENV_VALUE.getMessage(), envValue, true, 1, 256);
						// 説明
						String envDescription = envInfo.getDescription();
						CommonValidator.validateString(MessageConstant.JOB_ENV_DESCRIPTION.getMessage(), envDescription, false, 0, 256);
					}
				}
			}
		} else if (jobInfo.getType() == JobConstant.TYPE_FILEJOB) {
			JobFileInfo file = jobInfo.getFile();

			// 送信元ファシリティID(ノード)
			if(file.getSrcFacilityID() == null || "".equals(file.getSrcFacilityID())){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_NODE.getMessage());
				m_log.info("validateJobUnit() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}else{
				try {
					FacilityTreeCache.validateFacilityId(file.getSrcFacilityID(), jobInfo.getOwnerRoleId(), true);
				} catch (FacilityNotFound e) {
					InvalidSetting e1 = new InvalidSetting("FacilityId is not exist in repository. Src FacilityId = " + file.getSrcFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId);
					m_log.info("validateJobUnit() : "
							+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
					throw e1;
				} catch (InvalidRole e) {
					throw e;
				} catch (InvalidSetting e) {
					InvalidSetting e1 = new InvalidSetting("Src FacilityId is not node. Src FacilityId = " + file.getSrcFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId);
					m_log.info("validateJobUnit() : "
							+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
					throw new HinemosUnknown("add file transfer job unknown error. Src FacilityId = " + file.getSrcFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId, e);
				} catch (Exception e) {
					m_log.warn("validateJobUnit() add file transfer job unknown error. Src FacilityId = " + file.getSrcFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId + " : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw new HinemosUnknown("add file transfer job unknown error. Src FacilityId = " + file.getSrcFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId, e);
				}
			}

			// 受信先ファシリティID(ノード/スコープ)
			if(file.getDestFacilityID() == null || "".equals(file.getDestFacilityID())){
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SCOPE.getMessage());
			}else{
				try {
					FacilityTreeCache.validateFacilityId(file.getDestFacilityID(), jobInfo.getOwnerRoleId(), false);
				} catch (FacilityNotFound e) {
					InvalidSetting e1 = new InvalidSetting("FacilityId is not exist in repository. Dest FacilityId = " + file.getDestFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId);
					m_log.info("validateJobUnit() : "
							+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
					throw e1;
				} catch (InvalidRole e) {
					throw e;
				} catch (Exception e) {
					m_log.warn("validateJobUnit() add file transfer job unknown error. Dest FacilityId = " + file.getDestFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId + " "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw new HinemosUnknown("add file transfer job unknown error. Dest FacilityId = " + file.getDestFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId, e);
				}
			}

			// 停止[コマンド]が選択されていないか
			if(jobInfo.getWaitRule().isEnd_delay_operation() && jobInfo.getWaitRule().getEnd_delay_operation_type() == OperationConstant.TYPE_STOP_AT_ONCE){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_STOPCOMMAND_NG_IN_FILE_TRANSFER.getMessage());
				m_log.info("validateJobUnit() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			
			try {
				// 試行回数の未設定時(インポート時を想定)
				if (file.getMessageRetry() == null || file.getCommandRetry() == null) {
					String message = "validateJobUnit() messageRetry or commandRetry is null(file transfer job). messageRetry =" + file.getMessageRetry()
							+ ", commandRetry =" + file.getCommandRetry();
					m_log.info(message);
					throw new InvalidSetting(message);
				}

				// 試行回数のチェック
				CommonValidator.validateInt(MessageConstant.JOB_RETRIES.getMessage(), file.getMessageRetry(), 1, DataRangeConstant.SMALLINT_HIGH);

				if (file.isCommandRetryFlg()) {
					CommonValidator.validateInt(MessageConstant.JOB_RETRIES.getMessage(), file.getCommandRetry(), 1, DataRangeConstant.SMALLINT_HIGH);
				}
			} catch (Exception e) {
				m_log.info("validateJobUnit() add file transfer job retry error.Dest FacilityId = " + file.getDestFacilityID() + ", jobunitId = " + jobunitId
						+ ", jobId = " + jobId + ",messageRetry =" + file.getMessageRetry() + ",commandRetry =" + file.getCommandRetry() + ",commandRetryFlg ="
						+ file.isCommandRetryFlg() + " : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				throw e;
			}
		}else if (jobInfo.getType() == JobConstant.TYPE_REFERJOB || jobInfo.getType() == JobConstant.TYPE_REFERJOBNET ) {
			if ( jobInfo.getReferJobId() == null || jobInfo.getReferJobId().equals("")) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_REFERENCE_JOBID.getMessage());
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			if ( jobInfo.getReferJobUnitId() == null || jobInfo.getReferJobUnitId().equals("")) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_REFERENCE_JOBUNITID.getMessage());
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}else if (jobInfo.getType() == JobConstant.TYPE_APPROVALJOB ) {
			if (jobInfo.getApprovalReqRoleId() == null || jobInfo.getApprovalReqRoleId().equals("")) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_APPROVAL_REQ_ROLEID.getMessage());
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			if (jobInfo.getApprovalReqUserId() == null || jobInfo.getApprovalReqUserId().equals("")) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_APPROVAL_REQ_USERID.getMessage());
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			if (jobInfo.getApprovalReqSentence() == null || jobInfo.getApprovalReqSentence().equals("")) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_APPROVAL_REQ_SENTENCE.getMessage());
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			if (jobInfo.getApprovalReqMailTitle() == null || jobInfo.getApprovalReqMailTitle().equals("")) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_APPROVAL_REQ_MAIL_TITLE.getMessage());
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			if(!jobInfo.isUseApprovalReqSentence()){
				if (jobInfo.getApprovalReqMailBody() == null || jobInfo.getApprovalReqMailBody().equals("")) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_APPROVAL_REQ_MAIL_BODY.getMessage());
					m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
		}else if (jobInfo.getType() == JobConstant.TYPE_MONITORJOB ) {
			MonitorJobInfo monitor = jobInfo.getMonitor();
			if (monitor.getMonitorId() == null || monitor.getMonitorId().equals("")) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_MONITOR_ID.getMessage());
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			MonitorInfo monitorInfo = null;
			try {
				monitorInfo = com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK_OR(
					monitor.getMonitorId(), jobInfo.getOwnerRoleId());
				
			} catch (InvalidRole e) {
				throw e;
			} catch (Exception e) {
				String[] args = {monitor.getMonitorId()};
				InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_JOB_MONITOR_NOT_FOUND.getMessage(args));
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e1;
			}

			if (monitor.getMonitorInfoEndValue() == null) {
				String[] args = { MessageConstant.INFO.getMessage()};
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_JOB_MONITOR_END_VALUE.getMessage(args));
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			CommonValidator.validateInt(MessageConstant.MONITORJOB_RETURNVALUE_INFO.getMessage(), monitor.getMonitorInfoEndValue(), DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH);
			if (monitor.getMonitorWarnEndValue() == null) {
				String[] args = { MessageConstant.WARNING.getMessage()};
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_JOB_MONITOR_END_VALUE.getMessage(args));
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			CommonValidator.validateInt(MessageConstant.MONITORJOB_RETURNVALUE_WARNING.getMessage(), monitor.getMonitorWarnEndValue(), DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH);
			if (monitor.getMonitorCriticalEndValue() == null) {
				String[] args = { MessageConstant.CRITICAL.getMessage()};
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_JOB_MONITOR_END_VALUE.getMessage(args));
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			CommonValidator.validateInt(MessageConstant.MONITORJOB_RETURNVALUE_CRITICAL.getMessage(), monitor.getMonitorCriticalEndValue(), DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH);
			if (monitor.getMonitorUnknownEndValue() == null) {
				String[] args = { MessageConstant.UNKNOWN.getMessage()};
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_JOB_MONITOR_END_VALUE.getMessage(args));
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			CommonValidator.validateInt(MessageConstant.MONITORJOB_RETURNVALUE_UNKNOWN.getMessage(), monitor.getMonitorUnknownEndValue(), DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH);
			if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SNMPTRAP)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SYSTEMLOG)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_LOGFILE)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_WINEVENT)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S)) {
				if (monitor.getMonitorWaitTime() == null) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_JOB_MONITOR_WAIT_TIME.getMessage());
					m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				CommonValidator.validateInt(MessageConstant.MONITORJOB_MINUTE_WAIT.getMessage(), monitor.getMonitorWaitTime(), 0, DataRangeConstant.SMALLINT_HIGH);
				if (monitor.getMonitorWaitEndValue() == null) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_JOB_MONITOR_WAIT_END_VALUE.getMessage());
					m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				CommonValidator.validateInt(MessageConstant.MONITORJOB_RETURNVALUE_WAIT.getMessage(), monitor.getMonitorWaitEndValue(), DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH);
			}
		}

		int type = jobInfo.getType();
		if (type == JobConstant.TYPE_JOB ||
				type == JobConstant.TYPE_JOBNET ||
				type == JobConstant.TYPE_JOBUNIT ||
				type == JobConstant.TYPE_APPROVALJOB ||
				type == JobConstant.TYPE_MONITORJOB ||
				type == JobConstant.TYPE_FILEJOB) {
			ArrayList<JobEndStatusInfo> endStatusList = item.getData().getEndStatus();
			if (endStatusList == null) {
				String message = "JobEndStatus is null [" + item.getData().getId() + "]";
				m_log.info(message);
				throw new InvalidSetting(message);
			}
			if (endStatusList.size() != 3) {
				String message = "the number of JobEndStatus is too few [" + item.getData().getId() + "] " +
						endStatusList.size();
				m_log.info(message);
				throw new InvalidSetting(message);
			}

			if (jobInfo.getBeginPriority() == null
					|| jobInfo.getNormalPriority() == null
					|| jobInfo.getWarnPriority() == null
					|| jobInfo.getAbnormalPriority() == null) {
				String message = "the priorities of JobInfo less than 4 [" + jobInfo.getId() + "]";
				m_log.info(message);
				throw new InvalidSetting(message);
			}

			// 通知の権限チェック
			if (jobInfo.getNotifyRelationInfos() != null) {
				for(NotifyRelationInfo notifyInfo : jobInfo.getNotifyRelationInfos()){
					CommonValidator.validateNotifyId(notifyInfo.getNotifyId(), true, jobInfo.getOwnerRoleId());
				}
			}
		}

		if (type == JobConstant.TYPE_JOB ||
				type == JobConstant.TYPE_FILEJOB ||
				type == JobConstant.TYPE_APPROVALJOB ||
				type == JobConstant.TYPE_MONITORJOB ||
				type == JobConstant.TYPE_JOBNET) {

			// カレンダの権限チェック
			if (jobInfo.getWaitRule().isCalendar()) {
				CommonValidator.validateCalenderId(jobInfo.getWaitRule().getCalendarId(), true, jobInfo.getOwnerRoleId());
			}
		}

		if (type == JobConstant.TYPE_JOB ||
				type == JobConstant.TYPE_FILEJOB ||
				type == JobConstant.TYPE_APPROVALJOB ||
				type == JobConstant.TYPE_MONITORJOB ||
				type == JobConstant.TYPE_REFERJOBNET ||
				type == JobConstant.TYPE_REFERJOB) {

			// アイコンIDの存在チェック
			if (jobInfo.getIconId() != null && !"".equals(jobInfo.getIconId())) {
				try {
					new SelectJobmap().getJobmapIconImage(jobInfo.getIconId());
				} catch (IconFileNotFound e) {
					InvalidSetting e1 = new InvalidSetting("Icon Image is not exist in repository. Icon Id = " + jobInfo.getIconId());
					m_log.info("validateJobUnit() : "
							+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
					throw e1;
				} catch (ObjectPrivilege_InvalidRole e) {
					throw new InvalidRole(e.getMessage(), e);
				} catch (Exception e) {
					m_log.warn("validateJobUnit() add job unknown error. Icon Id = " + jobInfo.getIconId() + " : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw new HinemosUnknown("add job unknown error. Icon Id = " + jobInfo.getIconId(), e);
				}
			}
		}
			
		//子JobTreeItemを取得
		for(JobTreeItem child : item.getChildren()){
			validateJobInfo(child);
		}
	}

	/**
	 * 重複しているIDをチェック
	 * @param item
	 * @return
	 * @throws JobInvalid
	 */
	private static void validateDuplicateJobId(JobTreeItem item) throws JobInvalid {
		if(item == null || item.getData() == null) {
			return;
		}

		ArrayList<String> jobList = getJobIdList(item);
		Collections.sort(jobList);
		for (int i = 0; i < jobList.size() - 1; i++) {
			if (jobList.get(i).equals(jobList.get(i + 1))) {
				JobInvalid e = new JobInvalid(MessageConstant.MESSAGE_JOBUNIT_NG_DUPLICATE_JOB.getMessage(item.getData().getJobunitId(), jobList.get(i)));
				m_log.info("findDuplicateJobId() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
	}

	private static ArrayList<String> getJobIdList(JobTreeItem item) {
		if(item == null || item.getData() == null) {
			return new ArrayList<String>();
		}
		ArrayList<String> ret = new ArrayList<String>();
		ret.add(item.getData().getId());
		for (JobTreeItem child : item.getChildren()) {
			ret.addAll(getJobIdList(child));
		}
		return ret;
	}

	/**
	 * ジョブツリーアイテムのジョブ待ち条件情報をチェックする
	 * 
	 * @param item ジョブ待ち条件情報をチェックするジョブツリーアイテム
	 */
	private static void validateWaitRule(JobTreeItem item) throws InvalidSetting, JobInvalid{
		if(item == null || item.getData() == null) {
			return;
		}
		//ジョブID取得
		String jobId = item.getData().getId();
		//待ち条件情報を取得する
		JobWaitRuleInfo waitRule = item.getData().getWaitRule();
		if(waitRule != null && waitRule.getObject() != null && waitRule.getObject().size() > 0){
			for (JobObjectInfo objectInfo : waitRule.getObject()) {
				m_log.debug("objectInfo=" + objectInfo);

				if(objectInfo.getType() != JudgmentObjectConstant.TYPE_TIME
						&& objectInfo.getType() != JudgmentObjectConstant.TYPE_START_MINUTE
						&& objectInfo.getType() != JudgmentObjectConstant.TYPE_JOB_PARAMETER){
					m_log.debug("Not Time and Not Delay");
					//判定対象のジョブIDが同一階層に存在するかチェック
					boolean find = false;
					String targetJobId = objectInfo.getJobId();
					for(JobTreeItem child : item.getParent().getChildren()){
						//ジョブIDをチェック
						JobInfo childInfo = child.getData();
						if(childInfo != null && !jobId.equals(childInfo.getId())){
							if(targetJobId.equals(childInfo.getId())){
								find = true;
								break;
							}
						}
					}
					if(!find){
						String args[] = {jobId, targetJobId};
						JobInvalid ji = new JobInvalid(MessageConstant.MESSAGE_WAIT_JOBID_NG_INVALID_JOBID.getMessage(args));
						m_log.info("checkWaitRule() : " + ji.getClass().getSimpleName() + ", " + ji.getMessage());
						throw ji;
					}
				}else if (objectInfo.getType() == JudgmentObjectConstant.TYPE_JOB_PARAMETER) {
					// 条件判定の場合、設定値の妥当性チェックを行う
					CommonValidator.validateString(MessageConstant.WAIT_RULE_DECISION_VALUE_1.getMessage(), objectInfo.getDecisionValue01(), true, 1, 128);
					CommonValidator.validateString(MessageConstant.WAIT_RULE_DECISION_VALUE_2.getMessage(), objectInfo.getDecisionValue02(), true, 1, 128);
					CommonValidator.validateInt(MessageConstant.WAIT_RULE_DECISION_CONDITION.getMessage(), objectInfo.getDecisionCondition(), 0, 7);
				}else if (objectInfo.getType() == JudgmentObjectConstant.TYPE_TIME) {
					if(objectInfo.getTime() < DATETIME_VALUE_MIN || DATETIME_VALUE_MAX < objectInfo.getTime()){
						String[] args = {DATETIME_STRING_MIN, DATETIME_STRING_MAX, item.getData().getJobunitId(), jobId};
						InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_RANGE_OVER_JOB_SETTINGTIME.getMessage(args));
						m_log.info("validateWaitRule() : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage());
						throw e;
					}
				}
			}
			if(waitRule.isStart_delay_time()){
				if(waitRule.getStart_delay_time_value() < DATETIME_VALUE_MIN || DATETIME_VALUE_MAX < waitRule.getStart_delay_time_value()){
					String[] args = {DATETIME_STRING_MIN, DATETIME_STRING_MAX, item.getData().getJobunitId(), jobId};
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_RANGE_OVER_JOB_SETTINGTIME.getMessage(args));
					m_log.info("validateWaitRule() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			if(waitRule.isEnd_delay_time()){
				if(waitRule.getEnd_delay_time_value() < DATETIME_VALUE_MIN || DATETIME_VALUE_MAX < waitRule.getEnd_delay_time_value()){
					String[] args = {DATETIME_STRING_MIN, DATETIME_STRING_MAX, item.getData().getJobunitId(), jobId};
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_RANGE_OVER_JOB_SETTINGTIME.getMessage(args));
					m_log.info("validateWaitRule() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
		}
		//子JobTreeItemを取得
		for(JobTreeItem child : item.getChildren()){
			validateWaitRule(child);
		}
		return;
	}

	/**
	 * 参照ジョブにて指定された参照先のジョブ情報をチェックする
	 * 
	 * @param item 参照先のジョブ情報をチェックするジョブツリーアイテム
	 */
	private static void validateReferJob(JobTreeItem item) throws JobInvalid{
		if(item == null || item.getData() == null) {
			return;
		}

		//配下に存在する参照ジョブのみを取得する
		ArrayList<JobInfo> referJobList = JobUtil.findReferJob(item);
		m_log.trace("ReferJob count : " + referJobList.size());
		for (JobInfo referJob : referJobList) {
			String referJobId = referJob.getReferJobId();
			m_log.trace("ReferJobID : " + referJobId);
			//参照先に有効なジョブが存在しているか調べる
			int ret = JobUtil.checkValidJob(item, referJobId, referJob.getReferJobSelectType());
			if(ret != 0) {
				//有効なジョブが存在しないため、メッセージ出力
				String args[] = {referJob.getId(), referJobId};
				if(ret == 1) {
					// モジュール登録設定不一致
					throw new JobInvalid(MessageConstant.MESSAGE_REFERENCE_JOBID_NG_INVALID_SETTING.getMessage(args));
				}else{
					// 参照先が存在しない
					throw new JobInvalid(MessageConstant.MESSAGE_REFERENCE_JOBID_NG_INVALID_JOBID.getMessage(args));
				}
			}
		}
		return;
	}
	
	/**
	 * 参照ジョブネットにて指定された参照先のジョブネット情報をチェックする
	 * 
	 * @param item 参照先のジョブネット情報をチェックするジョブツリーアイテム
	 */
	private static void validateReferJobNet(JobTreeItem item) throws JobInvalid{
		if(item == null || item.getData() == null) {
			return;
		}

		//配下に存在する参照ジョブネットのみを取得する
		ArrayList<JobInfo> referJobNetList = JobUtil.findReferJobNet(item);
		m_log.trace("ReferJobNet count : " + referJobNetList.size());
		for (JobInfo referJobNet : referJobNetList) {
			String referJobNetId = referJobNet.getReferJobId();
			m_log.trace("ReferJobNetID : " + referJobNetId);
			//参照先に有効なジョブネットが存在しているか調べる
			int ret = JobUtil.checkValidJobNet(item, referJobNetId, referJobNet);
			if(ret != 0) {
				//有効なジョブネットが存在しないため、メッセージ出力
				String args[] = {referJobNet.getId(), referJobNetId};
				if(ret == 1) {
					// モジュール登録設定不一致
					throw new JobInvalid(MessageConstant.MESSAGE_REFERENCE_JOBID_NG_INVALID_SETTING.getMessage(args));
				} else if(ret == 2) {
					// 参照先に参照ジョブネットが含まれる
					throw new JobInvalid(MessageConstant.MESSAGE_REFERENCE_JOBID_NG_INVALID_SUBORDINATE_JOB.getMessage(args));
				}else{
					// 参照先が存在しない
					throw new JobInvalid(MessageConstant.MESSAGE_REFERENCE_JOBID_NG_INVALID_JOBID.getMessage(args));
				}
				
			}
		}
		return;
	}
}
