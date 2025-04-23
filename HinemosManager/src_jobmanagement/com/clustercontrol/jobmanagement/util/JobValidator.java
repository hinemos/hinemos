/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.accesscontrol.util.UserRoleCache;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.ScheduleConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.IconFileNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.JobInvalid;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.fault.RoleNotFound;
import com.clustercontrol.fault.RpaManagementToolAccountNotFound;
import com.clustercontrol.fault.RpaManagementToolEndStatusMasterNotFound;
import com.clustercontrol.fault.RpaManagementToolMasterNotFound;
import com.clustercontrol.fault.RpaManagementToolRunParamMasterNotFound;
import com.clustercontrol.fault.RpaManagementToolRunTypeMasterNotFound;
import com.clustercontrol.fault.RpaManagementToolStopModeMasterNotFound;
import com.clustercontrol.fault.RpaToolMasterNotFound;
import com.clustercontrol.fault.UserNotFound;
import com.clustercontrol.jobmanagement.bean.CommandStopTypeConstant;
import com.clustercontrol.jobmanagement.bean.DecisionObjectConstant;
import com.clustercontrol.jobmanagement.bean.FileCheckConstant;
import com.clustercontrol.jobmanagement.bean.JobCommandInfo;
import com.clustercontrol.jobmanagement.bean.JobCommandParam;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobEndStatusInfo;
import com.clustercontrol.jobmanagement.bean.JobEnvVariableInfo;
import com.clustercontrol.jobmanagement.bean.JobFileCheck;
import com.clustercontrol.jobmanagement.bean.JobFileCheckInfo;
import com.clustercontrol.jobmanagement.bean.JobFileInfo;
import com.clustercontrol.jobmanagement.bean.JobInfo;
import com.clustercontrol.jobmanagement.bean.JobInfoParameterConstant;
import com.clustercontrol.jobmanagement.bean.JobKick;
import com.clustercontrol.jobmanagement.bean.JobKickConstant;
import com.clustercontrol.jobmanagement.bean.JobLinkRcv;
import com.clustercontrol.jobmanagement.bean.JobLinkRcvInfo;
import com.clustercontrol.jobmanagement.bean.JobLinkSendInfo;
import com.clustercontrol.jobmanagement.bean.JobNextJobOrderInfo;
import com.clustercontrol.jobmanagement.bean.JobObjectGroupInfo;
import com.clustercontrol.jobmanagement.bean.JobObjectInfo;
import com.clustercontrol.jobmanagement.bean.JobParameterInfo;
import com.clustercontrol.jobmanagement.bean.JobQueueConstant;
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
import com.clustercontrol.jobmanagement.bean.ProcessingMethodConstant;
import com.clustercontrol.jobmanagement.bean.ResourceJobInfo;
import com.clustercontrol.jobmanagement.bean.RpaJobCheckEndValueInfo;
import com.clustercontrol.jobmanagement.bean.RpaJobEndValueConditionInfo;
import com.clustercontrol.jobmanagement.bean.RpaJobInfo;
import com.clustercontrol.jobmanagement.bean.RpaJobOptionInfo;
import com.clustercontrol.jobmanagement.bean.RpaJobRunParamInfo;
import com.clustercontrol.jobmanagement.bean.RpaStopTypeConstant;
import com.clustercontrol.jobmanagement.bean.SystemParameterConstant;
import com.clustercontrol.jobmanagement.bean.ValueSeparatorConstant;
import com.clustercontrol.jobmanagement.factory.CreateJobSession;
import com.clustercontrol.jobmanagement.factory.SelectJobmap;
import com.clustercontrol.jobmanagement.model.JobKickEntity;
import com.clustercontrol.jobmanagement.model.JobLinkSendSettingEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntityPK;
import com.clustercontrol.jobmanagement.queue.JobQueueContainer;
import com.clustercontrol.jobmanagement.queue.JobQueueNotFoundException;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueSetting;
import com.clustercontrol.jobmanagement.queue.internal.JobQueueEntity;
import com.clustercontrol.jobmanagement.rpa.bean.RpaJobEndValueConditionTypeConstant;
import com.clustercontrol.jobmanagement.rpa.bean.RpaJobReturnCodeConditionConstant;
import com.clustercontrol.jobmanagement.rpa.bean.RpaJobTypeConstant;
import com.clustercontrol.jobmanagement.rpa.util.ReturnCodeConditionChecker;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.bean.NotifyJobType;
import com.clustercontrol.notify.model.NotifyJobInfo;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.rest.endpoint.cloud.RestSessionScope;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ResourceJobActionEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ResourceJobTypeEnum;
import com.clustercontrol.rpa.bean.RpaManagementToolRunParamTypeConstant;
import com.clustercontrol.rpa.model.RpaManagementToolAccount;
import com.clustercontrol.rpa.model.RpaManagementToolRunParamMst;
import com.clustercontrol.rpa.util.RpaUtil;
import com.clustercontrol.sdml.util.SdmlUtil;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Singletons;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.bean.CloudConstant;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.factory.CloudManager;
import com.fasterxml.jackson.core.JsonProcessingException;

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
				CommonValidator.validateJobParamId(MessageConstant.JOBKICK_PARAM_ID.getMessage(), jobRuntimeParam.getParamId(), 64);
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
				} else {
					CommonValidator.validateString(MessageConstant.JOBKICK_DEFAULT_VALUE.getMessage(), jobRuntimeParam.getValue(), false, 0, 1024);
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
					// 「入力」「固定」のとき、選択候補が存在する場合はエラー
					if (jobRuntimeParam.getParamType() == JobRuntimeParamTypeConstant.TYPE_INPUT
							|| jobRuntimeParam.getParamType() == JobRuntimeParamTypeConstant.TYPE_FIXED) {
						InvalidSetting e = new InvalidSetting("select item is not necessary");
						m_log.info("validateJobKick() : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage());
						throw e;
					}
					boolean isMatched = false;
					for (JobRuntimeParamDetail jobRuntimeParamDetail : jobRuntimeParam.getJobRuntimeParamDetailList()) {
						// paramValue
						CommonValidator.validateString(MessageConstant.JOBKICK_DETAIL_PARAM_VALUE.getMessage(), jobRuntimeParamDetail.getParamValue(), true, 1, 1024);
						// description
						CommonValidator.validateString(MessageConstant.JOBKICK_DETAIL_DESCRIPTION.getMessage(), jobRuntimeParamDetail.getDescription(), true, 1, 1024);

						if (jobRuntimeParamDetail.getParamValue().equals(jobRuntimeParam.getValue())) {
							isMatched = true;
						}
					}
					// デフォルト値に対応する選択候補がない場合はエラー
					if (jobRuntimeParam.getValue() != null && !jobRuntimeParam.getValue().equals("") && !isMatched) {
						InvalidSetting e = new InvalidSetting("there is no select item corresponding to default value");
						m_log.info("validateJobKick() : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage());
						throw e;
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
						!(qMinute == 1 || qMinute == 2 || qMinute == 3 || qMinute == 5 || qMinute == 10 || qMinute == 15 ||
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
		} else if(jobSchedule.getScheduleType() == ScheduleConstant.TYPE_INTERVAL){
			//「時」、「分」、「繰り返し間隔(x分)」のバリデート
			CommonValidator.validateInt(MessageConstant.HOUR.getMessage(), jobSchedule.getHour(), 0, 48);
			CommonValidator.validateInt(MessageConstant.MINUTE.getMessage(), jobSchedule.getMinute(), 0, 59);
			CommonValidator.validateInt(MessageConstant.JOB_SCHEDULE_RUN_INTERVAL.getMessage(), jobSchedule.getEveryXminutes(), 1, 
				JobInfoParameterConstant.JOBSCHEDULE_INTERVAL_EVERY_MINUTES_MAX);
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
		CommonValidator.validateRegex(MessageConstant.FILE_NAME.getMessage(), jobFileCheck.getFileName(), true);
	}

	/**
	 * 実行契機[ジョブ連携受信実行契機]のvalidate
	 * @param jobLinkRcv
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public static void validateJobLinkRcv(JobLinkRcv jobLinkRcv) throws InvalidSetting, HinemosUnknown, InvalidRole {
		// jobkick
		validateJobKick(jobLinkRcv);

		// 実行するファシリティIDのチェック
		if(jobLinkRcv.getFacilityId() == null || "".equals(jobLinkRcv.getFacilityId())){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SCOPE.getMessage());
			m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}else{
			//ファシリティIDが正しい形式かチェック
			if(!SystemParameterConstant.isParam(
					jobLinkRcv.getFacilityId(),
					SystemParameterConstant.FACILITY_ID)){
				try {
					FacilityTreeCache.validateFacilityId(jobLinkRcv.getFacilityId(), jobLinkRcv.getOwnerRoleId(), false);
				} catch (FacilityNotFound e) {
					InvalidSetting e1 = new InvalidSetting("FacilityId is not exist in repository. FacilityId = " + jobLinkRcv.getFacilityId()
							+ ", JobLinkRcv  = " + jobLinkRcv.getId());
					m_log.info("validateJobLinkRcv() : "
							+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
					throw e1;
				} catch (InvalidRole e) {
					throw e;
				} catch (Exception e) {
					m_log.warn("validateJobLinkRcv() add job unknown error. FacilityId = " + jobLinkRcv.getFacilityId()
							+ ", JobLinkRcv  = " + jobLinkRcv.getId()  + " : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw new HinemosUnknown("add job unknown error. FacilityId = " + jobLinkRcv.getFacilityId() + ", JobLinkRcv  = " + jobLinkRcv.getId(), e);
				}
			}
		}
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
		} catch (JobMasterNotFound e) {
			String[] args = {jobunitId,jobId};
			InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_JOB_ID_NOT_EXIST.getMessage(args)) ;
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

		m_log.debug("validateJobMaster()");

		// ジョブ実行契機
		m_log.debug("validateJobMaster() jobschedule check start");
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
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
	 * 削除予定のジョブをバリデーションします。<br/>
	 * ・指定されたジョブを参照している実行契機がある場合は {@link InvalidSetting} を投げます。
	 *
	 * @param jobsToDelete 削除予定のジョブの情報。
	 *        もう1つのパラメータ jobsToAdd も含め、全て同じジョブユニットである必要があります。
	 *        このコレクションに含まれるジョブ全てがバリデーションの対象ではなく、
	 *        jobsToAdd にも同じジョブIDのエントリがある(＝更新予定のジョブである)場合は除外します。
	 *
	 * @param jobsToAdd 追加予定のジョブの情報。詳しくは jobToDelete パラメータの説明を参照。
	 */
	public static void validateJobsToDelete(Collection<JobInfo> jobsToDelete, Collection<JobInfo> jobsToAdd)
			throws InvalidSetting, HinemosUnknown {

		if (jobsToDelete.isEmpty()) {
			m_log.debug("validateJobsToDelete: Empty.");
			return;
		}

		// 後の処理のため「追加予定ジョブのID」を抽出しておく
		Collection<String> jobIdsToAdd = jobsToAdd.stream().map(it -> it.getId()).collect(Collectors.toCollection(HashSet::new));

		validateNoKickRefs(
				// 最初の JobInfo の jobunitId を採用
				jobsToDelete.iterator().next().getJobunitId(),
				// ジョブIDのコレクションを生成
				jobsToDelete.stream()  // 削除予定ジョブの情報をストリーミング
						.map(it -> it.getId())  // jobId を抽出
						.filter(jobId -> !jobIdsToAdd.contains(jobId))  // 更新予定を除外
						.collect(Collectors.toCollection(HashSet::new))  // コレクション化 (contains が効率よく動作するように HashSet を使う)
		);

		validateNoNotifyRefs(
				// 最初の JobInfo の jobunitId を採用
				jobsToDelete.iterator().next().getJobunitId(),
				// ジョブIDのコレクションを生成
				jobsToDelete.stream()  // 削除予定ジョブの情報をストリーミング
						.map(it -> it.getId())  // jobId を抽出
						.filter(jobId -> !jobIdsToAdd.contains(jobId))  // 更新予定を除外
						.collect(Collectors.toCollection(HashSet::new))  // コレクション化 (contains が効率よく動作するように HashSet を使う)
		);
	}

	/**
	 * 削除予定のジョブユニットをバリデーションします。<br/>
	 * ・指定されたジョブユニットを参照している実行契機がある場合は {@link InvalidSetting} を投げます。
	 */
	public static void validateJobunitToDelete(String targetJobunitId) throws InvalidSetting, HinemosUnknown {
		validateNoKickRefs(targetJobunitId, null);
		validateNoNotifyRefs(targetJobunitId, null);
	}

	/**
	 * 削除予定のジョブを参照している通知がある場合は {@link InvalidSetting} を投げます。
	 * 
	 * @param targetJobunitId ジョブユニットID。
	 * @param targetJobIds 削除予定のジョブIDのコレクション。nullならジョブユニット内の全てのジョブが対象。
	 */
	private static void validateNoNotifyRefs(String targetJobunitId, Collection<String> targetJobIds)
			throws InvalidSetting, HinemosUnknown {
		// 引数をdebugログ出力
		if (m_log.isDebugEnabled()) {
			StringBuilder target = new StringBuilder();
			target.append(targetJobunitId);
			if (targetJobIds != null) {
				target.append(":").append(String.join(",", targetJobIds));
			}
			m_log.debug("validateNoNotifyRefs: Target=" + target.toString());
		}

		List<NotifyJobInfo> notifyList = com.clustercontrol.notify.util.QueryUtil
				.getNotifyJobInfoByNotifyJobType_NONE(NotifyJobType.TYPE_DIRECT);
		for (NotifyJobInfo jobNotifyInfo : notifyList) {
			m_log.debug("validateNoNotifyRefs: jobNotifyId=" + jobNotifyInfo.getNotifyId());

			Set<String> refsJobList = new HashSet<String>();
			// ジョブユニット全体が削除対象の場合(targetJobId == null)
			// ジョブ通知の各重要度ごとにジョブユニットIDが削除対象ジョブユニットIDと一致するかチェック
			if (targetJobIds == null) {
				if (targetJobunitId.equals(jobNotifyInfo.getInfoJobunitId())) {
					refsJobList.add(jobNotifyInfo.getInfoJobId());
				}
				if (targetJobunitId.equals(jobNotifyInfo.getWarnJobunitId())) {
					refsJobList.add(jobNotifyInfo.getWarnJobId());
				}
				if (targetJobunitId.equals(jobNotifyInfo.getCriticalJobunitId())) {
					refsJobList.add(jobNotifyInfo.getCriticalJobId());
				}
				if (targetJobunitId.equals(jobNotifyInfo.getUnknownJobunitId())) {
					refsJobList.add(jobNotifyInfo.getUnknownJobId());
				}
			} else {
				// ジョブユニット以外のジョブが削除対象の場合
				// ジョブ通知の各重要度ごとにジョブユニットIDが一致し、かつジョブIDが削除対象ジョブ群に含まれるかチェック
				if (targetJobunitId.equals(jobNotifyInfo.getInfoJobunitId())
						&& targetJobIds.contains(jobNotifyInfo.getInfoJobId())) {
					refsJobList.add(jobNotifyInfo.getInfoJobId());
				}
				if (targetJobunitId.equals(jobNotifyInfo.getWarnJobunitId())
						&& targetJobIds.contains(jobNotifyInfo.getWarnJobId())) {
					refsJobList.add(jobNotifyInfo.getWarnJobId());
				}
				if (targetJobunitId.equals(jobNotifyInfo.getCriticalJobunitId())
						&& targetJobIds.contains(jobNotifyInfo.getCriticalJobId())) {
					refsJobList.add(jobNotifyInfo.getCriticalJobId());
				}
				if (targetJobunitId.equals(jobNotifyInfo.getUnknownJobunitId())
						&& targetJobIds.contains(jobNotifyInfo.getUnknownJobId())) {
					refsJobList.add(jobNotifyInfo.getUnknownJobId());
				}
			}

			// 一致するジョブIDがある場合はInvalidSetting
			if (refsJobList.size() > 0) {
				m_log.info("validateNoNotifyRefs: Job[JobUnitId=" + targetJobunitId + ",JobId="
						+ String.join(",", refsJobList) + "] is referred from " + jobNotifyInfo.getNotifyId());
				throw new InvalidSetting(MessageConstant.MESSAGE_DELETE_NG_NOTIFY_REFERENCE_TO_JOB.getMessage(
						new String[] { jobNotifyInfo.getNotifyId(), targetJobunitId, String.join(",", refsJobList) }));
			}
		}
	}

	/**
	 * 削除予定のジョブを参照している実行契機がある場合は {@link InvalidSetting} を投げます。
	 * 
	 * @param targetJobunitId ジョブユニットID。
	 * @param targetJobIds 削除予定のジョブIDのコレクション。nullならジョブユニット内の全てのジョブが対象。
	 */
	private static void validateNoKickRefs(String targetJobunitId, Collection<String> targetJobIds) throws InvalidSetting, HinemosUnknown {
		// 引数をdebugログ出力
		if (m_log.isDebugEnabled()) {
			StringBuilder target = new StringBuilder();
			target.append(targetJobunitId);
			if (targetJobIds != null) {
				target.append(":").append(String.join(",", targetJobIds));
			}
			m_log.debug("validateNoKickRefs: Target=" + target.toString());
		}

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// ジョブ定義を大きく変更した場合に、実行契機の数よりもジョブの数のほうが非常に多くなる可能性が高いので、
			// 実行契機を軸にループを回したほうが効率が良い。
			List<JobKickEntity> kicks = em.createNamedQuery("JobKickEntity.findAll", JobKickEntity.class, ObjectPrivilegeMode.NONE)
					.getResultList();
			for (JobKickEntity kick : kicks) {
				m_log.debug("validateNoKickRefs: jobKickId=" + kick.getJobkickId());
				// ジョブユニットIDが異なるならジョブIDを比較するまでもない
				if (!kick.getJobunitId().equals(targetJobunitId)) continue;
				// ジョブユニット全体(targetJobId == null) or 一致するジョブIDがある場合は、InvalidSetting
				if (targetJobIds == null || targetJobIds.contains(kick.getJobId())) {
					m_log.info("validateNoKickRefs: Job[" + kick.getJobunitId() + "," + kick.getJobId() + "] is referred from " + kick.getJobkickId());
					throw new InvalidSetting(MessageConstant.MESSAGE_DELETE_NG_JOBTRIGGERTYPE_REFERENCE.getMessage(
							new String[] { kick.getJobkickId(), kick.getJobunitId(), kick.getJobId() }));
				}
			}
		} catch (InvalidSetting e) {
			throw e;
		} catch (Exception e) {
			HinemosUnknown e1 = new HinemosUnknown(e.getMessage(), e);
			m_log.warn("validateNoKickRefs: Error.", e);
			throw e1;
		}
	}

	/**
	 * ジョブ連携送信設定情報の登録時バリデートチェック
	 */
	public static void validateJobLinkSendSetting(JobLinkSendSettingEntity info)
			throws InvalidRole, InvalidSetting, HinemosUnknown {

		// 送信先スコープのチェック
		if(info.getFacilityId() == null || info.getFacilityId().isEmpty()){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SCOPE.getMessage());
			m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		try {
			FacilityTreeCache.validateFacilityId(info.getFacilityId(), info.getOwnerRoleId(), false);
		} catch (FacilityNotFound e) {
			InvalidSetting e1 = new InvalidSetting("FacilityId is not exist in repository. "
					+ "FacilityId = " + info.getFacilityId() + ", joblinkSendSettingId = " + info.getJoblinkSendSettingId());
			m_log.info("validateJobLinkSendSetting() : "
					+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
			throw e1;
		} catch (InvalidRole e) {
			throw e;
		} catch (Exception e) {
			HinemosUnknown e1 = new HinemosUnknown(e.getMessage(), e);
			m_log.warn("validateJobLinkSendSetting() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw e1;
		}
	}

	/**
	 * 削除対象のジョブ連携送信設定情報が使用されているか<br/>
	 * ・ジョブ連携送信設定情報が使用されている場合は {@link InvalidSetting} を投げます。
	 */
	public static void validateJobLinkSendSettingToDelete(String joblinkSendSettingId)
			throws InvalidSetting, HinemosUnknown {
		try{
			//ジョブ
			List<JobMstEntity> jobMstList =
					com.clustercontrol.jobmanagement.util.QueryUtil.getJobMstByJoblinkSendSettingId_NONE(joblinkSendSettingId);
			if (jobMstList != null && jobMstList.size() > 0) {
				JobMstEntity jobMst = jobMstList.get(0);
				m_log.debug("validateJobLinkSendSettingToDelete() target JobMaster " + jobMst.getId().getJobId() + ", joblinkSendSettingId = " + joblinkSendSettingId);
				String[] args = {jobMst.getId().getJobunitId() + " "+ jobMst.getId().getJobId() , joblinkSendSettingId};
				throw new InvalidSetting(MessageConstant.MESSAGE_DELETE_NG_JOB_REFERENCE_TO_JOBLINKSENDSETTING.getMessage(args));
			}
			//通知
			List<NotifyJobInfo> notifyInfoList =
					com.clustercontrol.notify.util.QueryUtil.getNotifyJobInfoByJoblinkSendSettingId_NONE(joblinkSendSettingId);
			if (notifyInfoList != null && notifyInfoList.size() > 0) {
				NotifyJobInfo notifyInfo = notifyInfoList.get(0);
				m_log.debug("validateJobLinkSendSettingToDelete() target NotifyInfo " + notifyInfo.getNotifyId() + ", joblinkSendSettingId = " + joblinkSendSettingId);
				String[] args = {notifyInfo.getNotifyId(), joblinkSendSettingId};
				throw new InvalidSetting(MessageConstant.MESSAGE_DELETE_NG_NOTIFY_REFERENCE_TO_JOBLINKSENDSETTING.getMessage(args));
			}
		} catch (InvalidSetting e) {
			throw e;
		} catch (Exception e) {
			HinemosUnknown e1 = new HinemosUnknown(e.getMessage(), e);
			m_log.warn("validateJobLinkSendSettingToDelete() : "
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
	public static void validateJobmapIconImage(JobmapIconImage jobmapIconImage, boolean isModify) throws InvalidSetting, HinemosUnknown, InvalidRole {
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
		if (!isModify) {
			// ownerRoleId
			CommonValidator.validateOwnerRoleId(jobmapIconImage.getOwnerRoleId(), true, jobmapIconImage.getIconId(), HinemosModuleConstant.JOBMAP_IMAGE_FILE);
		}
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
			String defaultFileCheckIconId = new JobControllerBean().getJobmapIconIdFileCheckDefault();
			String defaultResourceIconId = new JobControllerBean().getJobmapIconIdResourceDefault();
			String defaultJobLinkSendIconId = new JobControllerBean().getJobmapIconIdJobLinkSendDefault();
			String defaultJobLinkRcvIconId = new JobControllerBean().getJobmapIconIdJobLinkRcvDefault();
			if (iconId.equals(defaultJobIconId) || iconId.equals(defaultJobnetIconId) 
					|| iconId.equals(defaultApprovalIconId) || iconId.equals(defaultMonitorIconId) 
					|| iconId.equals(defaultFileCheckIconId)
					|| iconId.equals(defaultFileIconId) || iconId.equals(defaultResourceIconId)
					|| iconId.equals(defaultJobLinkSendIconId) || iconId.equals(defaultJobLinkRcvIconId)) {
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
		// jobIdについて、予約語（利用不可）を用いていないかをチェック
		if(isReservedJobId(jobId)){
			String[] messageArgs = { jobId };
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_JOB_ID_RESERVED_WORD.getMessage(messageArgs));
			m_log.info("validateJobInfo() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// ジョブユニットID
		String jobunitId = jobInfo.getJobunitId();
		CommonValidator.validateId(MessageConstant.JOBUNIT_ID.getMessage(), jobunitId, 64);
		// jobunitIdは、先行の編集ロック獲得（ジョブユニット単位）時に予約語チェック済みなので、ここで予約語チェックは行わない

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
		}else{
			// ジョブユニット(TYPE_JOBUNIT)以外に対してジョブ変数を設定しようとしていないかチェック
			ArrayList<JobParameterInfo> Jobparam = jobInfo.getParam();
			if (Jobparam != null && Jobparam.size() != 0) {
				InvalidSetting e = new InvalidSetting("JobParam is set, but jobType is not TYPE_JOBUNIT");
				m_log.info("validateJobUnit() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}

		if (jobInfo.getType() == JobConstant.TYPE_JOB) {
			JobCommandInfo command = jobInfo.getCommand();

			// 実行コマンドが存在するかチェック
			if (command.getStartCommand() == null || "".equals(command.getStartCommand())) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_COMMAND.getMessage());
					m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
			}
			CommonValidator.validateString(MessageConstant.START_COMMAND.getMessage(), command.getStartCommand(), false, 0, 1024);
			
			// 停止コマンドが存在するかチェック（指定している場合のみ)
			if (command.getStopType() == CommandStopTypeConstant.EXECUTE_COMMAND) {
				if (command.getStopCommand() == null || "".equals(command.getStopCommand())) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_COMMAND.getMessage());
					m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			CommonValidator.validateString(MessageConstant.STOP_COMMAND.getMessage(), command.getStopCommand(), false, 0, 1024);
			
			// 実行ユーザのチェック
			if (command.getSpecifyUser()) {
				CommonValidator.validateString(MessageConstant.EFFECTIVE_USER.getMessage(), command.getUser(), true, 1, DataRangeConstant.VARCHAR_64);
			}
			
			// 実行するファシリティIDのチェック
			if(command.getFacilityID() == null || "".equals(command.getFacilityID())){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SCOPE.getMessage());
				m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			// ジョブ変数でない場合は、ファシリティIDのチェックを行う
			if(!SystemParameterConstant.isParamFormat(command.getFacilityID())){
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
			} else {
				CommonValidator.validateString(MessageConstant.JOB_PARAM_ID.getMessage(), command.getFacilityID(), true, 1, 512);
			}

			// 試行回数の未設定時(インポート時を想定)
			if (command.getMessageRetry() == null || command.getCommandRetry() == null) {
				String message = "validateJobUnit() messageRetry or commandRetry is null(job). messageRetry =" + command.getMessageRetry()
						+ ", commandRetry =" + command.getCommandRetry();
				m_log.info(message);
				throw new InvalidSetting(message);
			}

			// 試行回数のチェック
			CommonValidator.validateInt(MessageConstant.MESSAGE_RETRIES.getMessage(), command.getMessageRetry(), 1, DataRangeConstant.SMALLINT_HIGH);
			CommonValidator.validateInt(MessageConstant.MESSAGE_RETRIES_END_VALUE.getMessage(), command.getMessageRetryEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);

			// コマンドを繰り返し実行する 試行回数
			CommonValidator.validateInt(MessageConstant.COMMAND_RETRIES.getMessage(), command.getCommandRetry(), 1, DataRangeConstant.SMALLINT_HIGH);

			// スクリプト配布のチェック
			// スクリプトの最大サイズはHinemosプロパティから取得
			int scriptMaxSize = HinemosPropertyCommon.job_script_maxsize.getIntegerValue();
			if(command.getManagerDistribution()) {
				// スクリプト名
				String scriptName = command.getScriptName();
				CommonValidator.validateString(MessageConstant.JOB_SCRIPT_NAME.getMessage(), scriptName, true, 1, 64);
				// エンコーディング
				String scriptEncoding = command.getScriptEncoding();
				CommonValidator.validateString(MessageConstant.JOB_SCRIPT_ENCODING.getMessage(), scriptEncoding, true, 1, 32);
				// スクリプト
				String scriptContent = command.getScriptContent();
				CommonValidator.validateString(MessageConstant.JOB_SCRIPT.getMessage(), scriptContent, true, 1, scriptMaxSize);
			} else {
				// スクリプト名
				String scriptName = command.getScriptName();
				CommonValidator.validateString(MessageConstant.JOB_SCRIPT_NAME.getMessage(), scriptName, false, 0, 64);
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
					CommonValidator.validateJobParamId(MessageConstant.JOB_PARAM_ID.getMessage(), paramId, 64);
					// 値
					String jobCommandParamValue = jobCommandParam.getValue();
					CommonValidator.validateString(MessageConstant.JOB_PARAM_VALUE.getMessage(), jobCommandParamValue, true, 1, 256);
					// 標準出力から取得(正規表現)が有効の場合は、値は正規表現であること
					Boolean jobStandardOutputFlg = jobCommandParam.getJobStandardOutputFlg();
					if (jobStandardOutputFlg != null && jobStandardOutputFlg.booleanValue()) {
						CommonValidator.validateRegex(MessageConstant.JOB_PARAM_VALUE.getMessage(), jobCommandParamValue, true);
					}
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
		} else if (jobInfo.getType() == JobConstant.TYPE_FILEJOB) {
			JobFileInfo file = jobInfo.getFile();
			
			// 転送ファイルの入力が存在するかチェック
			if (file.getSrcFile() == null || "".equals(file.getSrcFile())) {
				 InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_FILE_NOT_FOUND.getMessage(jobInfo.getId()));
				 m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
				 throw e;
			}
			// 受信先ディレクトリの入力が存在するかチェック
			if (file.getDestDirectory() == null || "".equals(file.getDestDirectory())) {
				 InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_DIR.getMessage());
				 m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
				 throw e;
			}
			// 送信元ファシリティID(ノード)
			if(file.getSrcFacilityID() == null || "".equals(file.getSrcFacilityID())){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_NODE.getMessage());
				m_log.info("validateJobUnit() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			
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
			
			// ユーザの指定
			if (file.isSpecifyUser()) {
				CommonValidator.validateString(MessageConstant.EFFECTIVE_USER.getMessage(), file.getUser(), true, 1, DataRangeConstant.VARCHAR_64);
			}
			
			try {
				// 試行回数の未設定時(インポート時を想定)
				if (file.getMessageRetry() == null) {
					String message = "validateJobUnit() messageRetry or commandRetry is null(file transfer job). messageRetry =" + file.getMessageRetry();
					m_log.info(message);
					throw new InvalidSetting(message);
				}

				// 試行回数のチェック
				CommonValidator.validateInt(MessageConstant.MESSAGE_RETRIES.getMessage(), file.getMessageRetry(), 1, DataRangeConstant.SMALLINT_HIGH);
			} catch (Exception e) {
				m_log.info("validateJobUnit() add file transfer job retry error.Dest FacilityId = " + file.getDestFacilityID() + ", jobunitId = " + jobunitId
						+ ", jobId = " + jobId + ",messageRetry =" + file.getMessageRetry() + " : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				throw e;
			}
		}else if (jobInfo.getType() == JobConstant.TYPE_REFERJOB || jobInfo.getType() == JobConstant.TYPE_REFERJOBNET ) {
			if ( jobInfo.getReferJobId() == null || jobInfo.getReferJobId().equals("")) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_REFERENCE_JOBID.getMessage());
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			if ( jobInfo.getReferJobUnitId() == null || jobInfo.getReferJobUnitId().equals("") || !jobInfo.getReferJobUnitId().equals(jobInfo.getJobunitId())) {
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
			// ロールの存在確認
			try {
				com.clustercontrol.accesscontrol.util.QueryUtil.getRolePK(jobInfo.getApprovalReqRoleId());
			} catch (RoleNotFound e) {
				throw new InvalidSetting(e.getMessage(), e);
			}
			if (jobInfo.getApprovalReqUserId() == null || jobInfo.getApprovalReqUserId().equals("")) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_APPROVAL_REQ_USERID.getMessage());
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			// ユーザの存在確認
			if (!jobInfo.getApprovalReqUserId().equals("*")) {
				try {
					com.clustercontrol.accesscontrol.util.QueryUtil.getUserPK(jobInfo.getApprovalReqUserId());
				} catch (UserNotFound e) {
					throw new InvalidSetting(e.getMessage(), e);
				}
				
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
			if (SdmlUtil.isCreatedBySdml(monitorInfo)) {
				// SDMLで自動作成された監視設定は監視ジョブに設定不可
				InvalidSetting e = new InvalidSetting("Monitor Setting auto created by SDML is not available."
						+ " monitorId = " + monitorInfo.getMonitorId() + ", jobunitId = " + jobunitId + ", jobId = " + jobId);
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}

			if (monitor.getMonitorInfoEndValue() == null) {
				String[] args = { MessageConstant.INFO.getMessage()};
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_JOB_MONITOR_END_VALUE.getMessage(args));
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			CommonValidator.validateInt(MessageConstant.MONITORJOB_RETURNVALUE_INFO.getMessage(), monitor.getMonitorInfoEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
			if (monitor.getMonitorWarnEndValue() == null) {
				String[] args = { MessageConstant.WARNING.getMessage()};
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_JOB_MONITOR_END_VALUE.getMessage(args));
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			CommonValidator.validateInt(MessageConstant.MONITORJOB_RETURNVALUE_WARNING.getMessage(), monitor.getMonitorWarnEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
			if (monitor.getMonitorCriticalEndValue() == null) {
				String[] args = { MessageConstant.CRITICAL.getMessage()};
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_JOB_MONITOR_END_VALUE.getMessage(args));
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			CommonValidator.validateInt(MessageConstant.MONITORJOB_RETURNVALUE_CRITICAL.getMessage(), monitor.getMonitorCriticalEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
			if (monitor.getMonitorUnknownEndValue() == null) {
				String[] args = { MessageConstant.UNKNOWN.getMessage()};
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_JOB_MONITOR_END_VALUE.getMessage(args));
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			CommonValidator.validateInt(MessageConstant.MONITORJOB_RETURNVALUE_UNKNOWN.getMessage(), monitor.getMonitorUnknownEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);

			if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SNMPTRAP)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SYSTEMLOG)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_LOGFILE)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_BINARYFILE_BIN)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_PCAP_BIN)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_WINEVENT)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S)) {
				if (monitor.getMonitorWaitTime() == null) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_JOB_MONITOR_WAIT_TIME.getMessage());
					m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				if (monitor.getMonitorWaitEndValue() == null) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_JOB_MONITOR_WAIT_END_VALUE.getMessage());
					m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			if(monitor.getMonitorWaitTime() != null ){
				CommonValidator.validateInt(MessageConstant.MONITORJOB_MINUTE_WAIT.getMessage(), monitor.getMonitorWaitTime(), 0, DataRangeConstant.SMALLINT_HIGH);
			}
			if(monitor.getMonitorWaitEndValue() != null ){
				CommonValidator.validateInt(MessageConstant.MONITORJOB_RETURNVALUE_WAIT.getMessage(), monitor.getMonitorWaitEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
			}

			// 実行するファシリティIDのチェック
			if(monitor.getFacilityID() == null || "".equals(monitor.getFacilityID())){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SCOPE.getMessage());
				m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			// ジョブ変数でない場合は、ファシリティIDのチェックを行う
			if(!SystemParameterConstant.isParamFormat(monitor.getFacilityID())){
				try {
					FacilityTreeCache.validateFacilityId(monitor.getFacilityID(), jobInfo.getOwnerRoleId(), false);
				} catch (FacilityNotFound e) {
					InvalidSetting e1 = new InvalidSetting("FacilityId is not exist in repository. FacilityId = " + monitor.getFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId);
					m_log.info("validateJobUnit() : "
							+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
					throw e1;
				} catch (InvalidRole e) {
					throw e;
				} catch (Exception e) {
					m_log.warn("validateJobUnit() add job unknown error. FacilityId = " + monitor.getFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId + " : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw new HinemosUnknown("add job unknown error. FacilityId = " + monitor.getFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId, e);
				}
			}
		} else if (jobInfo.getType() == JobConstant.TYPE_FILECHECKJOB) {
			JobFileCheckInfo fileCheck = jobInfo.getJobFileCheck();

			// ファシリティID
			CommonValidator.validateString(MessageConstant.SCOPE.getMessage(), fileCheck.getFacilityID(), true, 1,
					DataRangeConstant.VARCHAR_512);

			// ジョブ変数でない場合は、ファシリティIDのチェックを行う
			if (!SystemParameterConstant.isParamFormat(fileCheck.getFacilityID())) {
				try {
					FacilityTreeCache.validateFacilityId(fileCheck.getFacilityID(), jobInfo.getOwnerRoleId(), false);
				} catch (FacilityNotFound e) {
					InvalidSetting e1 = new InvalidSetting("FacilityId is not exist in repository. FacilityId = "
							+ fileCheck.getFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId);
					m_log.info("validateJobInfo() : [FileCheckJob] " + e1.getClass().getSimpleName() + ", "
							+ e1.getMessage());
					throw e1;
				} catch (InvalidRole e) {
					throw e;
				} catch (Exception e) {
					m_log.warn("validateJobInfo() add job unknown error. FacilityId = " + fileCheck.getFacilityID()
							+ ", jobunitId = " + jobunitId + ", jobId = " + jobId + " : " + e.getClass().getSimpleName()
							+ ", " + e.getMessage(), e);
					throw new HinemosUnknown("add job unknown error. FacilityId = " + fileCheck.getFacilityID()
							+ ", jobunitId = " + jobunitId + ", jobId = " + jobId, e);
				}
			}
			// ディレクトリ
			CommonValidator.validateString(MessageConstant.DIRECTORY.getMessage(), fileCheck.getDirectory(), true, 1,
					DataRangeConstant.VARCHAR_4096);
			// ファイル名
			CommonValidator.validateString(MessageConstant.FILE_NAME.getMessage(), fileCheck.getFileName(), true, 1,
					DataRangeConstant.VARCHAR_4096);
			// スコープ処理
			if (fileCheck.getProcessingMethod() == null
					|| (!fileCheck.getProcessingMethod().equals(ProcessingMethodConstant.TYPE_ALL_NODE)
							&& !fileCheck.getProcessingMethod().equals(ProcessingMethodConstant.TYPE_ANY_NODE))) {
				InvalidSetting e = new InvalidSetting("unknown Processing Method");
				m_log.info("validateJobInfo() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			// チェック種別
			if (!fileCheck.getCreateValidFlg() && !fileCheck.getDeleteValidFlg() && !fileCheck.getModifyValidFlg()) {
				InvalidSetting e = new InvalidSetting(
						MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(MessageConstant.FILE_CHECK_TYPE.getMessage()));
				m_log.info("validateJobInfo() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			// 変更種別
			if (fileCheck.getModifyType() == null
					|| (!fileCheck.getModifyType().equals(FileCheckConstant.TYPE_MODIFY_FILESIZE)
							&& !fileCheck.getModifyType().equals(FileCheckConstant.TYPE_MODIFY_TIMESTAMP))) {
				InvalidSetting e = new InvalidSetting("unknown Modify Type");
				m_log.info("validateJobInfo() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			// 条件を満たした場合の終了値
			CommonValidator.validateInt(MessageConstant.SUCCESS_END_VALUE.getMessage(), fileCheck.getSuccessEndValue(),
					DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
			// 条件を満たさなければ終了する
			// タイムアウト
			CommonValidator.validateInt(MessageConstant.TIME_OUT.getMessage(), fileCheck.getFailureWaitTime(),
					JobInfoParameterConstant.FILECHECK_TIMEOUT_LEN_MIN,
					JobInfoParameterConstant.FILECHECK_TIMEOUT_LEN_MAX);
			// 終了値
			CommonValidator.validateInt(MessageConstant.END_VALUE.getMessage(), fileCheck.getFailureEndValue(),
					DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
		} else if (jobInfo.getType() == JobConstant.TYPE_RESOURCEJOB) {

			// リソース制御ジョブのバリデーション
			// ログインユーザの権限チェック(クラウド・仮想化 - 実行)の権限が必要
			String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
			SystemPrivilegeInfo systemPrivilegeInfo = new SystemPrivilegeInfo(FunctionConstant.CLOUDMANAGEMENT, SystemPrivilegeMode.EXEC);
			boolean isApprovalPrivilege = UserRoleCache.isSystemPrivilege(loginUser, systemPrivilegeInfo);
			if (!isApprovalPrivilege) {
				String roleName = systemPrivilegeInfo.getSystemFunction() + "." + systemPrivilegeInfo.getSystemPrivilege();
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_USER_AUTH_NEED_ROLL_CREATE_JOB.getMessage(
						MessageConstant.RESOURCE_JOB.getMessage(), jobInfo.getId(), roleName));
				m_log.warn("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			
			ResourceJobInfo resource = jobInfo.getResource();

			// リソース種別のチェック（Enumに持っている値か確認）
			List<ResourceJobTypeEnum> resourceJobTypeList = Arrays.asList(ResourceJobTypeEnum.values());
			boolean isResourceTypeValue = resourceJobTypeList.stream().anyMatch(type -> type.getCode().equals(resource.getResourceType()));
			if (!isResourceTypeValue) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(MessageConstant.RESOURCEJOB_ITEM_TYPE.getMessage()));
				m_log.warn("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}

			// 対象クラウドスコープIDのチェック
			String cloudScopeId = resource.getResourceCloudScopeId();
			CommonValidator.validateString(MessageConstant.RESOURCEJOB_ITEM_TARGET_CLOUD_SCOPE.getMessage(), cloudScopeId, true, 1, DataRangeConstant.VARCHAR_64);

			// 対象クラウドスコープIDの存在チェック
			String platformId;
			try(RestSessionScope sessionScope = RestSessionScope.open()) {
				platformId = CloudManager.singleton().getCloudScopes().getCloudScope(cloudScopeId).getPlatformId();
			} catch (CloudManagerException e) {
				InvalidSetting invalidSetting = new InvalidSetting(ErrorCode.CLOUDSCOPE_INVALID_CLOUDSCOPE_NOT_FOUND.getMessage(cloudScopeId));
				m_log.warn(e.getClass().getSimpleName() + ", " + e.getMessage());
				throw invalidSetting;
			}

			// 対象クラウドロケーションのチェック（対象がインスタンス、ストレージの場合のみ）
			if (resource.getResourceType() == ResourceJobTypeEnum.COMPUTE_COMPUTE_ID.getCode()
					|| resource.getResourceType() == ResourceJobTypeEnum.STORAGE.getCode()) {
				CommonValidator.validateString(MessageConstant.RESOURCEJOB_ITEM_TARGET_CLOUD_LOCATION.getMessage(), resource.getResourceLocationId(), true, 1, DataRangeConstant.VARCHAR_64);
			}

			if (resource.getResourceType() == ResourceJobTypeEnum.COMPUTE_COMPUTE_ID.getCode()
					|| resource.getResourceType() == ResourceJobTypeEnum.COMPUTE_FACILITY_ID.getCode()) {

				// 対象スコープのチェック
				CommonValidator.validateString(MessageConstant.RESOURCEJOB_ITEM_TARGET_SCOPE.getMessage(), resource.getResourceTargetId(), true, 1, DataRangeConstant.VARCHAR_512);
				//ノードやスコープだったら場合、存在するかチェック
				if (resource.getResourceType().equals(ResourceJobTypeEnum.COMPUTE_FACILITY_ID)) {
					try {
						FacilityTreeCache.validateFacilityId(resource.getResourceTargetId(), jobInfo.getOwnerRoleId(), false);
					} catch (FacilityNotFound e) {
						InvalidSetting e1 = new InvalidSetting("FacilityId is not exist in repository. ResourceTargetId = " + resource.getResourceTargetId() + ", jobunitId = " + jobunitId + ", jobId = " + jobId);
						m_log.info("validateJobUnit() : "
								+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
						throw e1;
					} catch (InvalidRole e) {
						throw e;
					} catch (Exception e) {
						m_log.warn("validateJobUnit() add resource job unknown error. ResourceTargetId= " + resource.getResourceTargetId() + ", jobunitId = " + jobunitId + ", jobId = " + jobId + " "
								+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
						throw new HinemosUnknown("add resource job unknown error. ResourceTargetId = " + resource.getResourceTargetId() + ", jobunitId = " + jobunitId + ", jobId = " + jobId, e);
					}
				}

				// アクションのチェック（コンピュートノードおよび対象プラットフォームで実行可能か）
				List<Integer> executableActionList = new ArrayList<>();

				if (CloudConstant.platform_AWS.equals(platformId)) {
					executableActionList.add(ResourceJobActionEnum.TYPE_POWERON.getCode());
					executableActionList.add(ResourceJobActionEnum.TYPE_POWEROFF.getCode());
					executableActionList.add(ResourceJobActionEnum.TYPE_REBOOT.getCode());
					executableActionList.add(ResourceJobActionEnum.TYPE_SNAPSHOT.getCode());
				} else if (CloudConstant.platform_Azure.equals(platformId)) {
					executableActionList.add(ResourceJobActionEnum.TYPE_POWERON.getCode());
					executableActionList.add(ResourceJobActionEnum.TYPE_POWEROFF.getCode());
					executableActionList.add(ResourceJobActionEnum.TYPE_REBOOT.getCode());
				} else if (CloudConstant.platform_HyperV.equals(platformId) || CloudConstant.platform_ESXi.equals(platformId)) {
					executableActionList.add(ResourceJobActionEnum.TYPE_POWERON.getCode());
					executableActionList.add(ResourceJobActionEnum.TYPE_POWEROFF.getCode());
					executableActionList.add(ResourceJobActionEnum.TYPE_REBOOT.getCode());
					executableActionList.add(ResourceJobActionEnum.TYPE_SUSPEND.getCode());
				} else if (CloudConstant.platform_vCenter.equals(platformId)) {
					executableActionList.add(ResourceJobActionEnum.TYPE_POWERON.getCode());
					executableActionList.add(ResourceJobActionEnum.TYPE_POWEROFF.getCode());
					executableActionList.add(ResourceJobActionEnum.TYPE_REBOOT.getCode());
					executableActionList.add(ResourceJobActionEnum.TYPE_SUSPEND.getCode());
					executableActionList.add(ResourceJobActionEnum.TYPE_SNAPSHOT.getCode());
				} else if (CloudConstant.platform_GCP.equals(platformId)) {
					executableActionList.add(ResourceJobActionEnum.TYPE_POWERON.getCode());
					executableActionList.add(ResourceJobActionEnum.TYPE_POWEROFF.getCode());
					executableActionList.add(ResourceJobActionEnum.TYPE_REBOOT.getCode());
					executableActionList.add(ResourceJobActionEnum.TYPE_SNAPSHOT.getCode());
				} else if (CloudConstant.platform_OCI.equals(platformId)) {
					executableActionList.add(ResourceJobActionEnum.TYPE_POWERON.getCode());
					executableActionList.add(ResourceJobActionEnum.TYPE_POWEROFF.getCode());
					executableActionList.add(ResourceJobActionEnum.TYPE_REBOOT.getCode());
					executableActionList.add(ResourceJobActionEnum.TYPE_SNAPSHOT.getCode());
				}

				boolean isExecutableAction = executableActionList.stream().anyMatch(action -> action.equals(resource.getResourceAction()));
				if (!isExecutableAction) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(MessageConstant.RESOURCEJOB_ITEM_ACTION.getMessage()));
					m_log.warn("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}

				// 状態確認期間のチェック
				CommonValidator.validateInt(MessageConstant.RESOURCEJOB_ITEM_CONFIRM_TIME.getMessage(), resource.getResourceStatusConfirmTime(), 0, DataRangeConstant.INTEGER_HIGH);

				// 状態確認間隔のチェック
				CommonValidator.validateInt(MessageConstant.RESOURCEJOB_ITEM_CONFIRM_INTERVAL.getMessage(), resource.getResourceStatusConfirmInterval(), 0, DataRangeConstant.INTEGER_HIGH);

			} else if (resource.getResourceType() == ResourceJobTypeEnum.STORAGE.getCode()) {

				// 対象ストレージのチェック
				CommonValidator.validateString(MessageConstant.RESOURCEJOB_ITEM_TARGET_STORAGE.getMessage(), resource.getResourceTargetId(), true, 1, DataRangeConstant.VARCHAR_512);

				// アクションのチェック（ストレージおよび対象プラットフォームで実行可能か）
				List<Integer> executableActionList = new ArrayList<>();

				if (CloudConstant.platform_AWS.equals(platformId) ||
						CloudConstant.platform_vCenter.equals(platformId) || CloudConstant.platform_ESXi.equals(platformId)) {
					executableActionList.add(ResourceJobActionEnum.TYPE_ATTACH.getCode());
					executableActionList.add(ResourceJobActionEnum.TYPE_DETACH.getCode());
					executableActionList.add(ResourceJobActionEnum.TYPE_SNAPSHOT.getCode());
				}
				boolean isExecutableAction = executableActionList.stream().anyMatch(action -> action.equals(resource.getResourceAction()));
				if (!isExecutableAction) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(MessageConstant.RESOURCEJOB_ITEM_ACTION.getMessage()));
					m_log.warn("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}

				if (resource.getResourceAction() == ResourceJobActionEnum.TYPE_ATTACH.getCode()) {

					// アタッチ先ノードのチェック（※アクションがアタッチの場合のみ）
					CommonValidator.validateString(MessageConstant.RESOURCEJOB_ITEM_ATTACH_NODE.getMessage(), resource.getResourceAttachNode(), true, 1, DataRangeConstant.VARCHAR_512);

					// アタッチ先デバイスのチェック（※アクションがアタッチ、かつAWSの場合のみ）
					if (CloudConstant.platform_AWS.equals(platformId)) {
						CommonValidator.validateString(MessageConstant.RESOURCEJOB_ITEM_ATTACH_DEVICE.getMessage(), resource.getResourceAttachDevice(), true, 1, DataRangeConstant.VARCHAR_256);
					}
				}
			}

			// 通知先スコープのチェック
			CommonValidator.validateString(MessageConstant.RESOURCEJOB_ITEM_NOTIFY_SCOPE.getMessage(), resource.getResourceNotifyScope(), true, 1, DataRangeConstant.VARCHAR_512);

			// 終了値（実行成功）のチェック
			CommonValidator.validateInt(MessageConstant.RESOURCEJOB_ITEM_SUCCESS.getMessage(), resource.getResourceSuccessValue() , DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH);

			// 終了値（実行失敗）のチェック
			CommonValidator.validateInt(MessageConstant.RESOURCEJOB_ITEM_FAILURE.getMessage(), resource.getResourceFailureValue() , DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH);

		} else if (jobInfo.getType() == JobConstant.TYPE_JOBLINKSENDJOB) {
			JobLinkSendInfo jobLinkSend = jobInfo.getJobLinkSend();

			// ジョブ連携送信設定IDチェック
			CommonValidator.validateJoblinkSendSettingId(jobLinkSend.getJoblinkSendSettingId(), jobInfo.getOwnerRoleId());

		} else if (jobInfo.getType() == JobConstant.TYPE_JOBLINKRCVJOB) {
			JobLinkRcvInfo jobLinkRcv = jobInfo.getJobLinkRcv();

			// ファシリティIDのチェック
			if(jobLinkRcv.getFacilityID() == null || "".equals(jobLinkRcv.getFacilityID())){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SCOPE.getMessage());
				m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			// ジョブ変数でない場合は、ファシリティIDのチェックを行う
			if(!SystemParameterConstant.isParamFormat(jobLinkRcv.getFacilityID())){
				try {
					FacilityTreeCache.validateFacilityId(jobLinkRcv.getFacilityID(), jobInfo.getOwnerRoleId(), false);
				} catch (FacilityNotFound e) {
					InvalidSetting e1 = new InvalidSetting("FacilityId is not exist in repository. FacilityId = " + jobLinkRcv.getFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId);
					m_log.info("validateJobUnit() : "
							+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
					throw e1;
				} catch (InvalidRole e) {
					throw e;
				} catch (Exception e) {
					m_log.warn("validateJobUnit() add job unknown error. FacilityId = " + jobLinkRcv.getFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId + " : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw new HinemosUnknown("add job unknown error. FacilityId = " + jobLinkRcv.getFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId, e);
				}
			} else {
				CommonValidator.validateString(MessageConstant.JOB_PARAM_ID.getMessage(), jobLinkRcv.getFacilityID(), true, 1, 512);
			}
		}else if (jobInfo.getType() == JobConstant.TYPE_RPAJOB ) {
			RpaJobInfo rpa = jobInfo.getRpa();
			// 直接実行
			// 直接実行の場合のみnullは不可な設定
			final boolean nullcheckIfDirect = rpa.getRpaJobType() == RpaJobTypeConstant.DIRECT;
			// ファシリティIDをチェック
			if(rpa.getFacilityID() == null || "".equals(rpa.getFacilityID())){
				if (nullcheckIfDirect) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SCOPE.getMessage());
					m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			} else {
				// ジョブ変数でない場合は、ファシリティIDのチェックを行う
				if(!ParameterUtil.isParamFormat(rpa.getFacilityID())){
					try {
						FacilityTreeCache.validateFacilityId(rpa.getFacilityID(), jobInfo.getOwnerRoleId(), false);
					} catch (FacilityNotFound e) {
						InvalidSetting e1 = new InvalidSetting("FacilityId is not exist in repository. FacilityId = " + rpa.getFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId);
						m_log.info("validateJobUnit() : "
								+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
						throw e1;
					} catch (InvalidRole e) {
						throw e;
					} catch (Exception e) {
						m_log.warn("validateJobUnit() add job unknown error. FacilityId = " + rpa.getFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId + " : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
						throw new HinemosUnknown("add job unknown error. FacilityId = " + rpa.getFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId, e);
					}
				}
			}
			// RPAツールIDをチェック
			if(rpa.getRpaToolId() == null || "".equals(rpa.getRpaToolId())){
				if (nullcheckIfDirect) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_JOB_RPA_PLEASE_SET_RPA_KIND.getMessage(rpa.getRpaToolId()));
					m_log.info("validateJobUnit() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			} else { 
				try  { 
					// シナリオ実行コマンドマスタにレコードが存在すること
					com.clustercontrol.rpa.util.QueryUtil.getRpaToolRunCommandMstPK(rpa.getRpaToolId());
				} catch (RpaToolMasterNotFound e1) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_JOB_RPA_RPA_KIND_INVALID.getMessage(rpa.getRpaToolId()));
					m_log.info("validateJobUnit() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			final int textMinSize;
			if (rpa.getRpaJobType() == RpaJobTypeConstant.DIRECT) {
				textMinSize = 1;  // 直接実行の場合は空文字不可
			} else {
				textMinSize = 0;
			}
			// 実行ファイルパスをチェック
			if(nullcheckIfDirect) {
				CommonValidator.validateNull(MessageConstant.RPAJOB_EXE_FILEPATH.getMessage(), rpa.getRpaExeFilepath());
			}
			CommonValidator.validateStringLengthSkippable(MessageConstant.RPAJOB_EXE_FILEPATH.getMessage(), rpa.getRpaExeFilepath(), textMinSize, -1);
			// シナリオファイルパスをチェック
			if (nullcheckIfDirect) {
				CommonValidator.validateNull(MessageConstant.RPAJOB_SCENARIO_FILEPATH.getMessage(), rpa.getRpaScenarioFilepath());
			}
			CommonValidator.validateStringLengthSkippable(MessageConstant.RPAJOB_SCENARIO_FILEPATH.getMessage(), rpa.getRpaScenarioFilepath(), textMinSize, -1);
			// 実行オプションをチェック
			for (RpaJobOptionInfo option : rpa.getRpaJobOptionInfos()) {
				CommonValidator.validateNull(MessageConstant.RPAJOB_SCENARIO_PARAMETER.getMessage(), option.getOption());
				CommonValidator.validateStringLengthSkippable(MessageConstant.RPAJOB_SCENARIO_PARAMETER.getMessage(), option.getOption(), 1, -1);
				CommonValidator.validateNull(MessageConstant.RPAJOB_SCENARIO_PARAMETER_DESCRIPTION.getMessage(), option.getOption());
				CommonValidator.validateStringLengthSkippable(MessageConstant.RPAJOB_SCENARIO_PARAMETER_DESCRIPTION.getMessage(), option.getDescription(), 0, -1);
			}

			// ログインされていない場合 通知フラグをチェック
			CommonValidator.validateNull(MessageConstant.RPAJOB_NOT_LOGIN_NOTIFY.getMessage(), rpa.getRpaNotLoginNotify());
			// ログインされていない場合 通知重要度をチェック
			CommonValidator.validatePriority(MessageConstant.RPAJOB_NOT_LOGIN_NOTIFY_PRIORITY.getMessage(), rpa.getRpaNotLoginNotifyPriority(), false);
			// ログインされていない場合 終了値をチェック
			if (nullcheckIfDirect) {
				CommonValidator.validateInt(MessageConstant.RPAJOB_NOT_LOGIN_END_VALUE.getMessage(), rpa.getRpaNotLoginEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
			} else {
				CommonValidator.validateNullableInt(MessageConstant.RPAJOB_NOT_LOGIN_END_VALUE.getMessage(), rpa.getRpaNotLoginEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
			}
			// RPAツールが既に動作している場合 通知フラグをチェック
			CommonValidator.validateNull(MessageConstant.RPAJOB_ALREADY_RUNNING_NOTIFY.getMessage(), rpa.getRpaAlreadyRunningNotify());
			// RPAツールが既に動作している場合 通知重要度をチェック
			CommonValidator.validatePriority(MessageConstant.RPAJOB_ALREADY_RUNNING_NOTIFY_PRIORITY.getMessage(), rpa.getRpaAlreadyRunningNotifyPriority(), false);
			// RPAツールが既に動作している場合 終了値をチェック
			if (nullcheckIfDirect) {
				CommonValidator.validateInt(MessageConstant.RPAJOB_ALREADY_RUNNING_END_VALUE.getMessage(), rpa.getRpaAlreadyRunningEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
			} else {
				CommonValidator.validateNullableInt(MessageConstant.RPAJOB_ALREADY_RUNNING_END_VALUE.getMessage(), rpa.getRpaAlreadyRunningEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
			}
			// RPAツールが既に動作している場合 通知フラグをチェック
			CommonValidator.validateNull(MessageConstant.RPAJOB_ABNORMAL_EXIT_NOTIFY.getMessage(), rpa.getRpaAbnormalExitNotify());
			// RPAツールが異常終了した場合 通知重要度をチェック
			CommonValidator.validatePriority(MessageConstant.RPAJOB_ABNORMAL_EXIT_NOTIFY_PRIORITY.getMessage(), rpa.getRpaAbnormalExitNotifyPriority(), false);
			// RPAツールが異常終了した場合 終了値をチェック
			if (nullcheckIfDirect) {
				CommonValidator.validateInt(MessageConstant.RPAJOB_ABNORMAL_EXIT_END_VALUE.getMessage(), rpa.getRpaAbnormalExitEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
			} else {
				CommonValidator.validateNullableInt(MessageConstant.RPAJOB_ABNORMAL_EXIT_END_VALUE.getMessage(), rpa.getRpaAbnormalExitEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
			}
			// いずれの判定にも一致しない場合の終了値をチェック
			if (nullcheckIfDirect) {
				CommonValidator.validateInt(MessageConstant.RPAJOB_DEFAULT_END_VALUE.getMessage(), rpa.getRpaDefaultEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
			} else {
				CommonValidator.validateNullableInt(MessageConstant.RPAJOB_DEFAULT_END_VALUE.getMessage(), rpa.getRpaDefaultEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
			}

			// シナリオの実行前後でOSへログイン・ログアウトするフラグをチェック
			CommonValidator.validateNull(MessageConstant.RPAJOB_LOGIN_FLG.getMessage(), rpa.getRpaLoginFlg());
			// 直接実行でログインを行う場合のみnullは不可な設定
			final boolean nullcheckIfDirectAndLogin = rpa.getRpaJobType() == RpaJobTypeConstant.DIRECT && rpa.getRpaLoginFlg();
			final int loginTextMinSize;
			if (nullcheckIfDirectAndLogin) {
				loginTextMinSize = 1;  // 直接実行でログインを行う場合は空文字不可
			} else {
				loginTextMinSize = 0;
			}
			// ログインユーザIDをチェック
			if (nullcheckIfDirectAndLogin) {
				CommonValidator.validateNull(MessageConstant.RPAJOB_LOGIN_USER_ID.getMessage(), rpa.getRpaLoginUserId());
			}
			CommonValidator.validateStringLengthSkippable(MessageConstant.RPAJOB_LOGIN_USER_ID.getMessage(), rpa.getRpaLoginUserId(), loginTextMinSize, -1);
			// ログインパスワードをチェック
			if (nullcheckIfDirectAndLogin) {
				CommonValidator.validateNull(MessageConstant.RPAJOB_LOGIN_PASSWORD.getMessage(), rpa.getRpaLoginPassword());
			}
			CommonValidator.validateStringLengthSkippable(MessageConstant.RPAJOB_LOGIN_PASSWORD.getMessage(), rpa.getRpaLoginPassword(),loginTextMinSize, -1);
			// ログインの解像度をチェック
			if (rpa.getRpaLoginResolution() == null) {
				if (nullcheckIfDirectAndLogin) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_JOB_RPA_PLEASE_SET_LOGIN_RESOLUTION.getMessage(rpa.getRpaLoginResolution()));
					m_log.info("validateJobUnit() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			} else {
				try  { 
					QueryUtil.getJobRpaLoginResolutionFindByPK(rpa.getRpaLoginResolution());
				} catch (JobMasterNotFound e1) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_JOB_RPA_LOGIN_RESOLUTION_INVALID.getMessage(rpa.getRpaLoginResolution()));
					m_log.info("validateJobUnit() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			// ログインリトライ回数をチェック
			if (nullcheckIfDirectAndLogin) {
				CommonValidator.validateInt(MessageConstant.RPAJOB_LOGIN_RETRY_COUNT.getMessage(), rpa.getRpaLoginRetry(), 0, DataRangeConstant.SMALLINT_HIGH);
			} else {
				CommonValidator.validateNullableInt(MessageConstant.RPAJOB_LOGIN_RETRY_COUNT.getMessage(), rpa.getRpaLoginRetry(), 0, DataRangeConstant.SMALLINT_HIGH);
			}
			// ログインできない場合の終了値をチェック
			if (nullcheckIfDirectAndLogin) {
				CommonValidator.validateInt(MessageConstant.RPAJOB_LOGIN_FAILURE_END_VALUE.getMessage(), rpa.getRpaLoginEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
			} else {
				CommonValidator.validateNullableInt(MessageConstant.RPAJOB_LOGIN_FAILURE_END_VALUE.getMessage(), rpa.getRpaLoginEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
			}
			// 異常発生時もログアウトするフラグをチェック
			if (nullcheckIfDirectAndLogin) {
				CommonValidator.validateNull(MessageConstant.RPAJOB_LOGOUT_FLG.getMessage(), rpa.getRpaLogoutFlg());
			}

			// 終了遅延発生時にスクリーンショットを取得するフラグをチェック
			CommonValidator.validateNull(MessageConstant.RPAJOB_SCREENSHOT_END_DELAY.getMessage(), rpa.getRpaScreenshotEndDelayFlg());
			// 特定の終了値の場合にスクリーンショットを取得するフラグをチェック
			CommonValidator.validateNull(MessageConstant.RPAJOB_SCREENSHOT_END_VALUE.getMessage(), rpa.getRpaScreenshotEndValueFlg());
			// 直接実行でスクリーンショットを取得する場合のみnullは不可な設定
			final boolean nullcheckIfDirectAndScreenshot = rpa.getRpaJobType() == RpaJobTypeConstant.DIRECT && rpa.getRpaScreenshotEndValueFlg();
			// スクリーンショットを取得する終了値、その条件について値チェック
			validateRpaEndValuAndCondition(rpa.getRpaScreenshotEndValue(), MessageConstant.END_VALUE.getMessage(),
					rpa.getRpaScreenshotEndValueCondition(), MessageConstant.RPAJOB_SCREENSHOT_END_VALUE_CONDITION.getMessage(),
					!nullcheckIfDirectAndScreenshot, false,
					MessageConstant.MESSAGE_JOB_RPA_PLEASE_SET_SCREENSHOT_END_VALUE,
					MessageConstant.MESSAGE_JOB_RPA_SCREENSHOT_END_VALUE_INVALID);

			// 直接実行でログファイルによる終了値判定条件が存在する場合のみnullは不可な設定
			final boolean nullcheckIfDirectAndLogEndValue = rpa.getRpaJobType() == RpaJobTypeConstant.DIRECT 
					&& rpa.getRpaJobEndValueConditionInfos().stream().filter(c -> c.getConditionType() == RpaJobEndValueConditionTypeConstant.LOG).findFirst().isPresent();
			final int textLogMinSize;
			if (nullcheckIfDirectAndLogEndValue) {
				textLogMinSize = 1;
			} else {
				textLogMinSize = 0;  // 直接実行でログファイルによる終了値判定条件が存在する場合以外は空文字も可
			}
			// 終了値判定用ログファイルディレクトリをチェック
			if (nullcheckIfDirectAndLogEndValue) {
				CommonValidator.validateNull(MessageConstant.RPAJOB_LOG_DIRECTORY.getMessage(), rpa.getRpaLogDirectory());
			}
			CommonValidator.validateStringLengthSkippable(MessageConstant.RPAJOB_LOG_DIRECTORY.getMessage(), rpa.getRpaLogDirectory(), textLogMinSize, -1);
			// 終了値判定用ログファイル名をチェック
			if (nullcheckIfDirectAndLogEndValue) {
				CommonValidator.validateNull(MessageConstant.RPAJOB_LOG_FILE_NAME.getMessage(), rpa.getRpaLogFileName());
			}
			CommonValidator.validateStringLengthSkippable(MessageConstant.RPAJOB_LOG_FILE_NAME.getMessage(), rpa.getRpaLogFileName(), textLogMinSize, -1);
			// 終了値判定用ログファイルエンコーディングをチェック
			if (nullcheckIfDirectAndLogEndValue) {
				CommonValidator.validateNull(MessageConstant.RPAJOB_LOG_FILE_ENCODING.getMessage(), rpa.getRpaLogEncoding());
			}
			CommonValidator.validateStringLengthSkippable(MessageConstant.RPAJOB_LOG_FILE_ENCODING.getMessage(), rpa.getRpaLogEncoding(), textLogMinSize, -1);
			// 終了値判定用ログファイル区切り文字をチェック
			if (nullcheckIfDirectAndLogEndValue) {
				CommonValidator.validateNull(MessageConstant.RPAJOB_LOG_FILE_RETURN_CODE.getMessage(), rpa.getRpaLogReturnCode());
			}
			CommonValidator.validateStringLengthSkippable(MessageConstant.RPAJOB_LOG_FILE_RETURN_CODE.getMessage(), rpa.getRpaLogReturnCode(), textLogMinSize, -1);

			// 終了値判定用ログファイル先頭/終端パターンをチェック
			// 先頭パターンと終端パターン両方の指定は不可
			if (rpa.getRpaLogPatternHead() != null && !"".equals(rpa.getRpaLogPatternHead())
					&& rpa.getRpaLogPatternTail() != null && !"".equals(rpa.getRpaLogPatternTail())) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_FILE_PATTERN_HEAD_OR_TAIL.getMessage());
				m_log.info("validateLogfile() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			CommonValidator.validateRegex(MessageConstant.RPAJOB_LOG_FILE_PATTERN_HEAD.getMessage(), rpa.getRpaLogPatternHead(), false);
			CommonValidator.validateRegex(MessageConstant.RPAJOB_LOG_FILE_PATTERN_TAIL.getMessage(), rpa.getRpaLogPatternTail(), false);
			// 終了値判定用ログファイル最大読み取り文字数をチェック
			if (rpa.getRpaLogMaxBytes() != null && rpa.getRpaLogMaxBytes() <= 0) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_MAX_READ_BYTE.getMessage());
				m_log.info("validateLogfile() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			// 終了値判定条件をチェック
			for (RpaJobEndValueConditionInfo condition : rpa.getRpaJobEndValueConditionInfos()) {
				switch(condition.getConditionType()) {
					case RpaJobEndValueConditionTypeConstant.LOG:
						// パターンマッチ表現をチェック
						CommonValidator.validateNull(MessageConstant.RPAJOB_END_VALUE_CONDITION_LOG_PATTERN.getMessage(), condition.getPattern());
						CommonValidator.validateStringLengthSkippable(MessageConstant.RPAJOB_END_VALUE_CONDITION_LOG_PATTERN.getMessage(), condition.getPattern(), 1, -1);
						// 条件に一致する場合・しないのフラグをチェック
						CommonValidator.validateNull(MessageConstant.RPAJOB_END_VALUE_CONDITION_PROCESS_TYPE.getMessage(), condition.getProcessType());
						// 大文字・小文字を区別しないのフラグをチェック
						CommonValidator.validateNull(MessageConstant.RPAJOB_END_VALUE_CONDITION_CASE_SENSITIVITY.getMessage(), condition.getCaseSensitivityFlg());
						break;
					case RpaJobEndValueConditionTypeConstant.RETURN_CODE:
						// リターンコードと判定条件をチェック
						validateRpaEndValuAndCondition(condition.getReturnCode(), MessageConstant.RPAJOB_END_VALUE_CONDITION_RETURN_CODE.getMessage(),
								condition.getReturnCodeCondition(), MessageConstant.RPAJOB_END_VALUE_RETURN_CODE_CONDITION.getMessage(),
								false, true,
								MessageConstant.MESSAGE_JOB_RPA_END_VALUE_CONDITION_RETURN_CODE_INVALID,
								MessageConstant.MESSAGE_JOB_RPA_END_VALUE_CONDITION_RETURN_CODE_INVALID);
						// コマンドのリターンコードをそのまま終了値とするフラグをチェック
						CommonValidator.validateNull(MessageConstant.RPAJOB_END_VALUE_CONDITION_USE_COMMAND_RETURN_CODE.getMessage(), condition.getUseCommandReturnCodeFlg());
						break;
					default:
						InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_JOB_RPA_END_VALUE_CONDITION_TYPE_INVALID.getMessage(
								String.valueOf(condition.getConditionType())));
						m_log.info("validateJobUnit() : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage());
						throw e;
				}
				// 説明をチェック
				CommonValidator.validateNull(MessageConstant.DESCRIPTION.getMessage(), condition.getDescription());
				CommonValidator.validateStringLengthSkippable(MessageConstant.DESCRIPTION.getMessage(), condition.getDescription(), 0, -1);
				// 終了値をチェック
				if (condition.getUseCommandReturnCodeFlg() != null && condition.getUseCommandReturnCodeFlg()) {
					// コマンドのリターンコードをそのまま終了値とする場合
					// nullを許可
					CommonValidator.validateNullableInt(MessageConstant.RPAJOB_END_VALUE_CONDITION_END_VALUE.getMessage(), condition.getEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
				} else {
					CommonValidator.validateInt(MessageConstant.RPAJOB_END_VALUE_CONDITION_END_VALUE.getMessage(), condition.getEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
				}
			}

			// 間接実行
			// 間接実行の場合のみnullは不可な設定
			final boolean nullcheckIfIndirect = rpa.getRpaJobType() == RpaJobTypeConstant.INDIRECT;
			// 対象アカウントをチェック
			RpaManagementToolAccount rpaManagementToolAccount = null;
			if(rpa.getRpaScopeId() == null || "".equals(rpa.getRpaScopeId())){
				if (nullcheckIfIndirect) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_JOB_RPA_PLEASE_SET_RPA_SCOPE.getMessage());
					m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			} else {
				// RPAスコープIDのチェックを行う
				try {
					rpaManagementToolAccount = com.clustercontrol.rpa.util.QueryUtil.getRpaAccountPK(rpa.getRpaScopeId());
				} catch (RpaManagementToolAccountNotFound e) {
					InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_NOT_FOUND.getMessage(
							MessageConstant.RPA_MANAGEMENT_TOOL_ACCOUNT.getMessage(), rpa.getRpaScopeId()));
					m_log.info("validateJobUnit() : "
							+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
					throw e1;
				} catch (InvalidRole e) {
					throw e;
				}
				// 実行種別をチェック
				try {
					com.clustercontrol.rpa.util.QueryUtil.getRpaManagementToolRunTypeMstPK(rpaManagementToolAccount.getRpaManagementToolId(), rpa.getRpaRunType());
				} catch (RpaManagementToolRunTypeMasterNotFound e) {
					InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_JOB_RPA_RUN_TYPE_INVALID.getMessage(
							String.valueOf(rpa.getRpaRunType()), rpaManagementToolAccount.getRpaManagementToolId()));
					m_log.info("validateJobUnit() : "
							+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
					throw e1;
				}
				// 停止方法をチェック
				try {
					com.clustercontrol.rpa.util.QueryUtil.getRpaManagementToolStopModeMstPK(rpaManagementToolAccount.getRpaManagementToolId(), rpa.getRpaStopMode());
				} catch (RpaManagementToolStopModeMasterNotFound e) {
					InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_JOB_RPA_STOP_MODE_INVALID.getMessage(
							String.valueOf(rpa.getRpaStopMode()), rpaManagementToolAccount.getRpaManagementToolId()));
					m_log.info("validateJobUnit() : "
							+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
					throw e1;
				}
			}
			// 停止種別
			if (rpa.getRpaStopType() == null || !Arrays.asList(RpaStopTypeConstant.STOP_SCENARIO, RpaStopTypeConstant.STOP_JOB).contains(rpa.getRpaStopType())) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_JOB_RPA_STOP_TYPE_INVALID.getMessage(
						String.valueOf(rpa.getRpaJobType())));
				m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			} else {
				// 停止種別が使用可能かどうかチェック
				try {
					if (rpaManagementToolAccount != null) {
						if (!RpaUtil.getRestDefine(rpaManagementToolAccount.getRpaManagementToolId()).checkRpaStopType(rpa.getRpaStopType())) {
							InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_JOB_RPA_STOP_TYPE_INVALID.getMessage(
									String.valueOf(rpa.getRpaJobType())));
							m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
							throw e;
						}
					}
				} catch (RpaManagementToolMasterNotFound e) {
					InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_NOT_FOUND.getMessage(
							MessageConstant.RPA_MANAGEMENT_TOOL_ID.getMessage(), rpaManagementToolAccount.getRpaManagementToolId()));
					m_log.info("validateJobUnit() : "
							+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
					throw e1;
				}
			}
			// シナリオ実行コネクションタイムアウトのチェック
			if (nullcheckIfIndirect) {
				CommonValidator.validateInt(MessageConstant.RPAJOB_RUN_CONNECT_TIMEOUT.getMessage(),
						rpa.getRpaRunConnectTimeout(), 0, DataRangeConstant.SMALLINT_HIGH);
			} else {
				CommonValidator.validateNullableInt(MessageConstant.RPAJOB_RUN_CONNECT_TIMEOUT.getMessage(),
						rpa.getRpaRunConnectTimeout(), 0, DataRangeConstant.SMALLINT_HIGH);
			}
			// シナリオ実行リクエストタイムアウトのチェック
			if (nullcheckIfIndirect) {
				CommonValidator.validateInt(MessageConstant.RPAJOB_RUN_REQUEST_TIMEOUT.getMessage(),
						 rpa.getRpaRunRequestTimeout(), 0, DataRangeConstant.SMALLINT_HIGH);
			} else {
				CommonValidator.validateNullableInt(MessageConstant.RPAJOB_RUN_REQUEST_TIMEOUT.getMessage(),
						 rpa.getRpaRunRequestTimeout(), 0, DataRangeConstant.SMALLINT_HIGH);
			}
			// シナリオ実行ができない場合に終了するフラグをチェック
			CommonValidator.validateNull(MessageConstant.RPAJOB_RUN_FAILURE_END.getMessage(), rpa.getRpaRunEndFlg());
			// 間接実行でシナリオ実行ができない場合に終了する場合のみnullは不可な設定
			final boolean nullcheckIfIndirectAndRunFailureEnd = rpa.getRpaJobType() == RpaJobTypeConstant.INDIRECT && rpa.getRpaRunEndFlg();
			// シナリオ実行ができない場合 リトライ回数のチェック
			if (nullcheckIfIndirectAndRunFailureEnd) {
				CommonValidator.validateInt(MessageConstant.RPAJOB_RUN_RETRIES.getMessage(), rpa.getRpaRunRetry(), 0, DataRangeConstant.SMALLINT_HIGH);
			} else {
				CommonValidator.validateNullableInt(MessageConstant.RPAJOB_RUN_RETRIES.getMessage(), rpa.getRpaRunRetry(), 0, DataRangeConstant.SMALLINT_HIGH);
			}
			// シナリオ実行ができない場合 終了値のチェック
			if (nullcheckIfIndirectAndRunFailureEnd) {
				CommonValidator.validateInt(MessageConstant.RPAJOB_RUN_FAILURE_END_VALUE.getMessage(),
						rpa.getRpaRunEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
			} else {
				CommonValidator.validateNullableInt(MessageConstant.RPAJOB_RUN_FAILURE_END_VALUE.getMessage(),
						rpa.getRpaRunEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
			}
			// シナリオ実行結果確認コネクションタイムアウトのチェック
			if (nullcheckIfIndirect) {
				CommonValidator.validateInt(MessageConstant.RPAJOB_CHECK_CONNECT_TIMEOUT.getMessage(),
						rpa.getRpaCheckConnectTimeout(), 0, DataRangeConstant.SMALLINT_HIGH);
			} else {
				CommonValidator.validateNullableInt(MessageConstant.RPAJOB_CHECK_CONNECT_TIMEOUT.getMessage(),
						rpa.getRpaCheckConnectTimeout(), 0, DataRangeConstant.SMALLINT_HIGH);
			}
			// シナリオ実行結果確認リクエストタイムアウトのチェック
			if (nullcheckIfIndirect) {
				CommonValidator.validateInt(MessageConstant.RPAJOB_CHECK_REQUEST_TIMEOUT.getMessage(),
						rpa.getRpaCheckRequestTimeout(), 0, DataRangeConstant.SMALLINT_HIGH);
			} else {
				CommonValidator.validateNullableInt(MessageConstant.RPAJOB_CHECK_REQUEST_TIMEOUT.getMessage(),
						rpa.getRpaCheckRequestTimeout(), 0, DataRangeConstant.SMALLINT_HIGH);
			}
			// シナリオ実行結果が確認できない場合に終了するフラグをチェック
			CommonValidator.validateNull(MessageConstant.RPAJOB_CHECK_FAILURE_END.getMessage(), rpa.getRpaCheckEndFlg());
			// 間接実行でシナリオ実行結果が確認できない場合に終了する場合のみnullは不可な設定
			final boolean nullcheckIfIndirectAndCheckFailureEnd = rpa.getRpaJobType() == RpaJobTypeConstant.INDIRECT && rpa.getRpaCheckEndFlg();
			// シナリオ実行結果確認ができない場合 リトライ回数のチェック
			if (nullcheckIfIndirectAndCheckFailureEnd) {
				CommonValidator.validateInt(MessageConstant.RPAJOB_CHECK_RETRIES.getMessage(), rpa.getRpaCheckRetry(), 0, DataRangeConstant.SMALLINT_HIGH);
			} else {
				CommonValidator.validateNullableInt(MessageConstant.RPAJOB_CHECK_RETRIES.getMessage(), rpa.getRpaCheckRetry(), 0, DataRangeConstant.SMALLINT_HIGH);
			}
			// シナリオ実行結果確認ができない場合 終了値のチェック
			if (nullcheckIfIndirectAndCheckFailureEnd) {
				CommonValidator.validateInt(MessageConstant.RPAJOB_CHECK_FAILURE_END_VALUE.getMessage(),
						rpa.getRpaCheckEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
			} else {
				CommonValidator.validateNullableInt(MessageConstant.RPAJOB_CHECK_FAILURE_END_VALUE.getMessage(),
						rpa.getRpaCheckEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
			}

			// 起動パラメータのチェック
			// 必須パラメータが登録されていることをチェック
			if (nullcheckIfIndirect) {
				Function<Integer, Optional<RpaJobRunParamInfo>> findParam = paramId -> rpa.getRpaJobRunParamInfos().stream().filter(r -> r.getParamId().equals(paramId)).findFirst();
				List<RpaManagementToolRunParamMst> requiredParams = com.clustercontrol.rpa.util.QueryUtil.getRpaManagementToolRequiredRunParamMstList(
						rpaManagementToolAccount.getRpaManagementToolId(), rpa.getRpaRunType());
				for (RpaManagementToolRunParamMst requiredParam : requiredParams) {
					Optional<RpaJobRunParamInfo> param = findParam.apply(requiredParam.getParamId()); 
					if (!param.isPresent()) {
						InvalidSetting e1 = new InvalidSetting(
								MessageConstant.MESSAGE_JOB_RPA_RUN_REQUIRED_PARAM_INVALID
										.getMessage(String.valueOf(requiredParam.getParamName())));
						m_log.info("validateJobUnit() : "
								+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
						throw e1;
					}
				}
				// 変更不可のパラメータをチェック
				List<RpaManagementToolRunParamMst> fixedParams = com.clustercontrol.rpa.util.QueryUtil.getRpaManagementToolFixedRunParamMstList(
						rpaManagementToolAccount.getRpaManagementToolId(), rpa.getRpaRunType());
				for (RpaManagementToolRunParamMst fixedParam : fixedParams) {
					Optional<RpaJobRunParamInfo> param = findParam.apply(fixedParam.getParamId()); 
					if (param.isPresent() && !param.get().getParamValue().equals(fixedParam.getParamValue())) {
						InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_JOB_RPA_RUN_FIXED_PARAM_INVALID
								.getMessage(String.valueOf(fixedParam.getParamName()), fixedParam.getParamValue()));
						m_log.info("validateJobUnit() : "
								+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
						throw e1;
					}
				}
			}
			// 各パラメータをチェック
			for (RpaJobRunParamInfo runParamInfo : rpa.getRpaJobRunParamInfos()) {
				CommonValidator.validateNull(MessageConstant.RPAJOB_RUN_PARAM_VALUE.getMessage(), runParamInfo.getParamValue());
				CommonValidator.validateStringLengthSkippable(MessageConstant.RPAJOB_RUN_PARAM_VALUE.getMessage(), runParamInfo.getParamValue(), 0, -1);
				try {
					// 起動パラメータマスタに存在することをチェック
					RpaManagementToolRunParamMst runParamMst = com.clustercontrol.rpa.util.QueryUtil.getRpaManagementToolRunParamMstPK(runParamInfo.getParamId());
					// データ型のチェック
					String[] params;
					if (runParamMst.getArrayFlg()) {
						params = runParamInfo.getParamValue().split(",");
					} else {
						params = new String[]{runParamInfo.getParamValue()};
					}
					for (String param : params) {
						if (ParameterUtil.isParamFormat(param)) {
							// ジョブ変数が指定されている場合はスキップ
							continue;
						}
						switch (runParamMst.getParamType()) {
						case(RpaManagementToolRunParamTypeConstant.TYPE_NUMERIC):
							// 数値
							try {
								Long.valueOf(param);
							} catch(NumberFormatException e) {
								InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_JOB_RPA_RUN_PARAM_NUMERIC_DATA_INVALID.getMessage(
										runParamMst.getParamName(), runParamInfo.getParamValue()));
								m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
								throw e1;
							}
							break;
						case(RpaManagementToolRunParamTypeConstant.TYPE_BOOLEAN):
							// 真偽値
							if (!param.toLowerCase().equals("true") && !param.toLowerCase().equals("false")) {
								InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_JOB_RPA_RUN_PARAM_BOOLEAN_DATA_INVALID.getMessage(
										runParamMst.getParamName(), runParamInfo.getParamValue()));
								throw e;
							}
							break;
						}
					}
				} catch (RpaManagementToolRunParamMasterNotFound e) {
					InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_NOT_FOUND.getMessage(
							MessageConstant.RPA_MANAGEMENT_TOOL_RUN_PARAM.getMessage(), String.valueOf(runParamInfo.getParamId())));
					m_log.info("validateJobUnit() : "
							+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
					throw e1;
				}
			}
			
			// シナリオ入力パラメータのチェック
			// nullを許可
			CommonValidator.validateStringLengthSkippable(MessageConstant.RPAJOB_SCENARIO_INPUT_PARAM.getMessage(), rpa.getRpaScenarioParam(), 0, -1);
			if (rpa.getRpaScenarioParam() != null && !rpa.getRpaScenarioParam().isEmpty() ) {
				// ジョブ変数が指定されている場合はスキップ
				if (!ParameterUtil.isParamFormat(rpa.getRpaScenarioParam())) {
					try {
						// JSON形式をチェック
						RpaJobWorker.parseScenarioParamJson(rpa.getRpaScenarioParam());
					} catch (JsonProcessingException e) {
						InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_JOB_RPA_SCENARIO_INPUT_PARAM_INVALID.getMessage(
								rpa.getRpaScenarioParam(), e.getMessage()));
						m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
						throw e1;
					}
				}
			}

			// 終了値のチェック
			for (RpaJobCheckEndValueInfo endValueInfo : rpa.getRpaJobCheckEndValueInfos()) {
				try { 
					// 終了状態マスタに存在することをチェック
					com.clustercontrol.rpa.util.QueryUtil.getRpaManagementToolEndStatusMstPK(endValueInfo.getEndStatusId());
					CommonValidator.validateInt(MessageConstant.RPAJOB_CHECK_END_VALUE.getMessage(),
							endValueInfo.getEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
				} catch (RpaManagementToolEndStatusMasterNotFound e) {
					InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_NOT_FOUND.getMessage(
							MessageConstant.RPA_MANAGEMENT_TOOL_END_STATUS.getMessage(), String.valueOf(endValueInfo.getEndStatusId())));
					m_log.info("validateJobUnit() : "
							+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
					throw e1;
				}
			}
			
			// 直接実行・間接実行共通
			// RPAジョブ種別
			if (rpa.getRpaJobType() == null || !Arrays.asList(RpaJobTypeConstant.DIRECT, RpaJobTypeConstant.INDIRECT).contains(rpa.getRpaJobType())) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_JOB_RPA_JOB_TYPE_INVALID.getMessage(
						String.valueOf(rpa.getRpaJobType())));
				m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			// 試行回数の未設定時(インポート時を想定)
			if (rpa.getMessageRetry() == null ) {
				String message = "validateJobUnit() messageRetry (rpajob). messageRetry =" + rpa.getMessageRetry();
				m_log.info(message);
				throw new InvalidSetting(message);
			}
			// リトライ回数をチェック
			CommonValidator.validateInt(MessageConstant.JOB_RETRIES.getMessage(), rpa.getMessageRetry(), 1, DataRangeConstant.SMALLINT_HIGH);
			// コマンド実行失敗時終了値をチェック
			CommonValidator.validateInt(MessageConstant.JOB_RETRIES.getMessage(), rpa.getMessageRetryEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
			// コマンドの繰り返し実行は有効化不可（RPAシナリオジョブなので意味のない項目）で関連項目は入力不可
			if( rpa.getCommandRetryFlg()){
				String[] args = { MessageConstant.COMMAND_RETRY_FLG.getMessage() };
				String message = MessageConstant.MESSAGE_JOB_RPA_INVALID_ITEM.getMessage(args);
				throw new InvalidSetting(message);
			}
			if( rpa.getCommandRetry() != null ){
				String[] args = { MessageConstant.COMMAND_RETRIES.getMessage() };
				String message = MessageConstant.MESSAGE_JOB_RPA_INVALID_ITEM.getMessage(args);
				throw new InvalidSetting(message);
			}
			if( rpa.getCommandRetryEndStatus() != null ){
				String[] args = { MessageConstant.COMMAND_RETRY_END_STATUS.getMessage() };
				String message = MessageConstant.MESSAGE_JOB_RPA_INVALID_ITEM.getMessage(args);
				throw new InvalidSetting(message);
			}
		}

		int type = jobInfo.getType();
		if (type == JobConstant.TYPE_JOB ||
				type == JobConstant.TYPE_JOBNET ||
				type == JobConstant.TYPE_JOBUNIT ||
				type == JobConstant.TYPE_APPROVALJOB ||
				type == JobConstant.TYPE_MONITORJOB ||
				type == JobConstant.TYPE_FILEJOB ||
				type == JobConstant.TYPE_JOBLINKSENDJOB ||
				type == JobConstant.TYPE_JOBLINKRCVJOB ||
				type == JobConstant.TYPE_FILECHECKJOB ||
				type == JobConstant.TYPE_RESOURCEJOB ||
				type == JobConstant.TYPE_RPAJOB) {
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
			for (JobEndStatusInfo endStatus : endStatusList) {
				CommonValidator.validateInt(MessageConstant.END_VALUE.getMessage(), endStatus.getValue(), DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH);
				CommonValidator.validateInt(MessageConstant.RANGE_END_VALUE.getMessage(), endStatus.getStartRangeValue(), DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH);
				CommonValidator.validateInt(MessageConstant.RANGE_END_VALUE.getMessage(), endStatus.getEndRangeValue(), DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH);
				if (endStatus.getStartRangeValue() > endStatus.getEndRangeValue()) {
					String message = "startRangeValue > endRangeValue. start="+ endStatus.getStartRangeValue()+", end="+endStatus.getEndRangeValue()+ ", jobunitId = " + jobunitId + ", jobId = " + jobId;
					m_log.info(message);
					throw new InvalidSetting(message);
				}
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
				type == JobConstant.TYPE_JOBNET ||
				type == JobConstant.TYPE_FILEJOB ||
				type == JobConstant.TYPE_APPROVALJOB ||
				type == JobConstant.TYPE_MONITORJOB ||
				type == JobConstant.TYPE_REFERJOBNET ||
				type == JobConstant.TYPE_REFERJOB ||
				type == JobConstant.TYPE_JOBLINKSENDJOB ||
				type == JobConstant.TYPE_JOBLINKRCVJOB ||
				type == JobConstant.TYPE_FILECHECKJOB ||
				type == JobConstant.TYPE_RESOURCEJOB ||
				type == JobConstant.TYPE_RPAJOB) {

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
	 * RPAの終了値と判定条件の正当性を確認する。
	 * 終了値：-32768～32767、null可の場合はnull可、ジョブ変数可の場合はジョブ変数可、
	 *   判定条件がEQUAL_NUMERIC、NOT_EQUAL_NUMERICの場合は範囲指定（,）および複数指定（:）が可
	 * 判定条件：RpaJobReturnCodeConditionConstant.EQUAL_NUMERIC～LESS_THAN_OR_EQUAL_TO
	 * 
	 * @param endValue 終了値
	 * @param endValueName 終了値の項目名（メッセージ用）
	 * @param endValueCondition 判定条件（RpaJobReturnCodeConditionConstantの定数）
	 * @param endValueConditionName 判定条件の項目名（メッセージ用）
	 * @param allowNull 終了値と判定条件について、nullを許容するか
	 * @param allowJobParam 終了値にジョブ変数を許容するか
	 * @param messageOfNullEndValue 終了値がnullの場合のメッセージ
	 * @param messageOfInvalidEndValue 終了値が不正な場合のメッセージ
	 * @throws InvalidSetting 終了値、判定条件、それらの相関チェックで正当でない場合に発生する
	 */
	private static void validateRpaEndValuAndCondition(String endValue, String endValueName,
			Integer endValueCondition, String endValueConditionName,
			boolean allowNull, boolean allowJobParam,
			MessageConstant messageOfNullEndValue, MessageConstant messageOfInvalidEndValue) throws InvalidSetting {

		// 判定条件のチェック
		try {
			int minSize = RpaJobReturnCodeConditionConstant.EQUAL_NUMERIC;
			int maxSize = RpaJobReturnCodeConditionConstant.LESS_THAN_OR_EQUAL_TO;
			if (allowNull) {
				// null可で値チェック
				CommonValidator.validateNullableInt(endValueConditionName, endValueCondition, minSize, maxSize);
			} else {
				// null不可で値チェック
				CommonValidator.validateInt(endValueConditionName, endValueCondition, minSize, maxSize);
			}
		} catch (InvalidSetting e) {
			// メッセージが不適切（0以上5以下の数値を指定してください）なので作り直す。
			String conditionString = "null";
			if (endValueCondition != null) {
				conditionString = endValueCondition.toString();
			}
			String[] args = { endValueConditionName, conditionString };
			// 「{0}」が不正です({1})。
			InvalidSetting ex = new InvalidSetting(MessageConstant.MESSAGE_INVALID_VALUE.getMessage(args));
			m_log.info("isValidRpaEndValuAndCondition() : " + ex.getClass().getSimpleName() + ", " + ex.getMessage());
			throw ex;
		}

		// 終了値のチェック
		// nullの場合
		if (endValue == null || endValue.isEmpty()) {
			if (allowNull) {
				// null可
				return;
			} else {
				InvalidSetting e = new InvalidSetting(messageOfNullEndValue.getMessage(endValue));
				m_log.info("isValidRpaEndValuAndCondition() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}

		// ジョブ変数可で、ジョブ変数なら以降のチェックなしで終了
		if (allowJobParam && ParameterUtil.isParamFormat(endValue)) {
			return;
		}

		// 終了値と判定条件の相関チェック
		if (endValueCondition == null
				|| endValueCondition.equals(RpaJobReturnCodeConditionConstant.EQUAL_NUMERIC)
				|| endValueCondition.equals(RpaJobReturnCodeConditionConstant.NOT_EQUAL_NUMERIC)) {
			// 判定条件が「（null可なら）null」か"="か"!="の場合は、複数指定、範囲指定の書式でチェックする。
			if (!endValue.matches(ReturnCodeConditionChecker.MULTI_RANGE_CONDITION_REGEX)) {
				InvalidSetting e = new InvalidSetting(messageOfInvalidEndValue.getMessage(endValue));
				m_log.info("isValidRpaEndValuAndCondition() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} else {
			// それ以外の場合は、数値書式であることを確認する。
			if (!endValue.matches(ReturnCodeConditionChecker.NUMBER_REGEX)) {
				InvalidSetting e = new InvalidSetting(messageOfInvalidEndValue.getMessage(endValue));
				m_log.info("isValidRpaEndValuAndCondition() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}

		// 終了値の範囲チェック
		try {
			// 区切り文字で分割して分割後の値毎にチェック
			ReturnCodeConditionChecker.comfirmReturnCodeNumberRange(endValueName, endValue);
		} catch (InvalidSetting e) {
			InvalidSetting ex = new InvalidSetting(messageOfInvalidEndValue.getMessage(endValue) + " " + e.getMessage());
			m_log.info("isValidRpaEndValuAndCondition() : " + ex.getClass().getSimpleName() + ", " + ex.getMessage());
			throw ex;
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
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 */
	private static void validateWaitRule(JobTreeItem item) throws InvalidSetting, JobInvalid, InvalidRole, HinemosUnknown{
		if(item == null || item.getData() == null) {
			return;
		}
		//ジョブID取得
		String jobId = item.getData().getId();
		//待ち条件情報を取得する
		JobWaitRuleInfo waitRule = item.getData().getWaitRule();
		if(waitRule != null) {
			// 待ち条件の判定対象チェック
			validateWaitRuleObject(item);

			// 条件を満たさない場合に終了する
			CommonValidator.validateInt(MessageConstant.END_VALUE.getMessage(), waitRule.getEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
			if (waitRule.getEndStatus() == null) {
				String message = "validateWaitRule() : endStatus(endCondition) is null";
				m_log.info(message);
				throw new InvalidSetting(message);
			}
			// カレンダのチェック
			if (waitRule.isCalendar()) {
				CommonValidator.validateCalenderId(waitRule.getCalendarId(), true, item.getData().getOwnerRoleId());
				CommonValidator.validateInt(MessageConstant.END_VALUE.getMessage(), waitRule.getCalendarEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
				if (waitRule.getSkipEndStatus() == null) {
					String message = "validateWaitRule() : endStatus(calendar) is null";
					throw new InvalidSetting(message);
				}
			} else{
				CommonValidator.validateCalenderId(waitRule.getCalendarId(), false);
				CommonValidator.validateInt(MessageConstant.END_VALUE.getMessage(), waitRule.getCalendarEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
			}
			// スキップのチェック
			if (waitRule.isSkip()) {
				CommonValidator.validateInt(MessageConstant.END_VALUE.getMessage(), waitRule.getSkipEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
				if (waitRule.getSkipEndStatus() == null) {
					String message = "validateWaitRule() : endStatus(Skip) is null";
					m_log.info(message);
					throw new InvalidSetting(message);
				}
			}
			// 開始遅延のチェック
			if(waitRule.isStart_delay_time()){
				if(waitRule.getStart_delay_time_value() < DATETIME_VALUE_MIN || DATETIME_VALUE_MAX < waitRule.getStart_delay_time_value()){
					String[] args = {DATETIME_STRING_MIN, DATETIME_STRING_MAX, item.getData().getJobunitId(), jobId};
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_RANGE_OVER_JOB_SETTINGTIME.getMessage(args));
					m_log.info("validateWaitRule() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			if(waitRule.isStart_delay_session()) {
				CommonValidator.validateInt(MessageConstant.DELAYED_START_TIME_AFTER_SESSION_START.getMessage(), waitRule.getStart_delay_session_value(), 1, DataRangeConstant.SMALLINT_HIGH);
			}
			if (waitRule.isStart_delay_notify()) {
				CommonValidator.validatePriority(MessageConstant.START_DELAY.getMessage(), waitRule.getStart_delay_notify_priority(), false);
			}
			// ジョブユニット、参照ジョブを除く開始遅延の操作チェック
			// 開始遅延-操作が有効である場合のみ、チェックする
			if (waitRule.isStart_delay_operation()) {
				if (waitRule.getStart_delay_operation_type() != OperationConstant.TYPE_STOP_SKIP
						&& waitRule.getStart_delay_operation_type() != OperationConstant.TYPE_STOP_WAIT
						&& item.getData().getType() != JobConstant.TYPE_JOBUNIT
						&& item.getData().getType() != JobConstant.TYPE_REFERJOB
						&& item.getData().getType() != JobConstant.TYPE_REFERJOBNET) {
					String args[] = {jobId};
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_START_DELAY_OPERATION_TYPE_INVALID.getMessage(args));
					m_log.info("validateWaitRule() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				CommonValidator.validateInt(MessageConstant.END_VALUE.getMessage(), waitRule.getStart_delay_operation_end_value(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
			}
			// 終了遅延のチェック
			if(waitRule.isEnd_delay_time()){
				if(waitRule.getEnd_delay_time_value() < DATETIME_VALUE_MIN || DATETIME_VALUE_MAX < waitRule.getEnd_delay_time_value()){
					String[] args = {DATETIME_STRING_MIN, DATETIME_STRING_MAX, item.getData().getJobunitId(), jobId};
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_RANGE_OVER_JOB_SETTINGTIME.getMessage(args));
					m_log.info("validateWaitRule() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			if (waitRule.isEnd_delay_session()) {
				CommonValidator.validateInt(MessageConstant.DELAYED_END_TIME_AFTER_SESSION_START.getMessage(), waitRule.getEnd_delay_session_value(), 1, DataRangeConstant.SMALLINT_HIGH);
			}
			if (waitRule.isEnd_delay_job()) {
				CommonValidator.validateInt(MessageConstant.TIME_AFTER_JOB_START.getMessage(), waitRule.getEnd_delay_job_value(), 1, DataRangeConstant.SMALLINT_HIGH);
			}
			if(waitRule.isEnd_delay_change_mount()){
				// 0を含めないため、独自の実装を行う。
				Double minSize = 0D;
				Double maxSize = 100D;
				if (waitRule.getEnd_delay_change_mount_value() == null 
						|| waitRule.getEnd_delay_change_mount_value() <= minSize 
						|| waitRule.getEnd_delay_change_mount_value() > maxSize) {
					String[] args = {MessageConstant.JOB_CHANGE_MOUNT.getMessage(),
							((new BigDecimal(minSize)).toBigInteger()).toString(),
							((new BigDecimal(maxSize)).toBigInteger()).toString()};
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_BETWEEN_EXCLUDE_MINSIZE.getMessage(args));
					m_log.info("validateWaitRule() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			if (waitRule.isEnd_delay_notify()) {
				CommonValidator.validatePriority(MessageConstant.END_DELAY.getMessage(), waitRule.getEnd_delay_notify_priority(), false);
			}
			if(waitRule.isEnd_delay_operation()){
				CommonValidator.validateInt(MessageConstant.END_VALUE.getMessage(), waitRule.getEnd_delay_operation_end_value(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
			}
			//排他条件分岐の設定をチェック
			if (waitRule.isExclusiveBranch()) {
				//後続ジョブの優先度設定のチェック
				//後続ジョブでないジョブの優先度設定が存在しないこと
				//このジョブを待ち条件としているジョブIDリスト
				List<String> nextJobIdList = new ArrayList<>();
				JobTreeItem parent= item.getParent();
				if (parent != null) {
					//同一階層のジョブリスト
					List<JobTreeItem> siblingJobList = parent.getChildren();
					//新規に作成された後続ジョブを表示するために使用する
					for (JobTreeItem sibling : siblingJobList) {
						if (sibling == item) {
							continue;
						}
						JobInfo siblingJobInfo = sibling.getData();
						if (siblingJobInfo.getWaitRule() == null) {
							continue;
						}
						if (siblingJobInfo.getWaitRule().getObjectGroup() == null) {
							continue;
						}
						for (JobObjectGroupInfo groupInfo : siblingJobInfo.getWaitRule().getObjectGroup()) {
							List<JobObjectInfo> siblingWaitJobObjectInfoList = groupInfo.getJobObjectList();
							if (siblingWaitJobObjectInfoList == null) {
								continue;
							}
							for (JobObjectInfo siblingWaitJobObjectInfo : siblingWaitJobObjectInfoList) {
								//同じ階層のジョブの中でこのジョブを待ち条件としているもの
								if ((siblingWaitJobObjectInfo.getType() == JudgmentObjectConstant.TYPE_JOB_END_STATUS ||
									siblingWaitJobObjectInfo.getType() == JudgmentObjectConstant.TYPE_JOB_END_VALUE ||
									siblingWaitJobObjectInfo.getType() == JudgmentObjectConstant.TYPE_JOB_RETURN_VALUE) &&
									siblingWaitJobObjectInfo.getJobId().equals(item.getData().getId())) {
									nextJobIdList.add(sibling.getData().getId());
									break;
								} 
							}
						}
					}
				}

				List<JobNextJobOrderInfo> nextJobOrderList = waitRule.getExclusiveBranchNextJobOrderList();
				if (nextJobOrderList != null) {
					for (JobNextJobOrderInfo nextJobOrder: nextJobOrderList){
						String targetJobId = nextJobOrder.getNextJobId();
						//優先度設定のジョブIDが後続ジョブに無ければエラー
						if (!nextJobIdList.contains(targetJobId)) {
							String[] args = {jobId, targetJobId};
							InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_NEXT_JOB_ORDER_JOBID_NG_INVALID_JOBID.getMessage(args));
							m_log.info("validateWaitRule() : "
									+ e.getClass().getSimpleName() + ", " + e.getMessage());
							throw e;
						}
					}
				}

				CommonValidator.validateInt(MessageConstant.END_VALUE.getMessage(), waitRule.getExclusiveBranchEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
				if (waitRule.getExclusiveBranchEndStatus() == null) {
					String message = "validateWaitRule() : endStatus(exclusiveBranch) is null";
					throw new InvalidSetting(message);
				}
			}
			//繰り返し設定をチェック
			if (waitRule.getJobRetry() == null) {
				String message = "validateJobUnit() jobRetry is null(job). jobRetry =" + waitRule.getJobRetry();
				m_log.info(message);
				throw new InvalidSetting(message);
			}
			// 試行回数のチェック
			CommonValidator.validateInt(MessageConstant.JOB_RETRIES.getMessage(), waitRule.getJobRetry(), 1, DataRangeConstant.SMALLINT_HIGH);
			// 試行間隔のチェック
			CommonValidator.validateInt(MessageConstant.JOB_RETRY_INTERVAL.getMessage(), waitRule.getJobRetryInterval(), 0, JobInfoParameterConstant.JOB_RETRY_INTERVAL_HIGH);

			// 同時実行制御キュー設定をチェック
			if (waitRule.getQueueFlg() != null && waitRule.getQueueFlg().booleanValue()) {
				String queueId = waitRule.getQueueId();
				// 字句チェック
				CommonValidator.validateId(MessageConstant.JOB_QUEUE.getMessage(), queueId, JobQueueConstant.ID_MAXLEN);
				// データ存在チェック
				try {
					Singletons.get(JobQueueContainer.class).get(queueId);
					List<JobQueueSetting> jobQueueList = Singletons.get(JobQueueContainer.class).findSettingsByRole(item.getData().getOwnerRoleId());
					List<String> queueList = new ArrayList<String>();
					for (JobQueueSetting jobQueue : jobQueueList) {
						queueList.add(jobQueue.getQueueId());
					}
					if (!queueList.contains(queueId)) {
						InvalidRole e = new InvalidRole("targetClass=" +JobQueueEntity.class.getSimpleName()+ ", pk=" + queueId+ ", ownerRoleId=" + item.getData().getOwnerRoleId());
						m_log.warn("validateWaitRule() :" + e.getClass().getSimpleName() + ", " + e.getMessage());
						throw e;
					}
				} catch (JobQueueNotFoundException e) {
					throw new InvalidSetting(
							MessageConstant.MESSAGE_JOBQUEUE_NOT_FOUND.getMessage(new String[] { queueId }), e);
				}
			}
			// 多重度設定をチェック
			if(waitRule.isMultiplicityNotify()){
				CommonValidator.validateInt(MessageConstant.END_VALUE.getMessage(), waitRule.getMultiplicityEndValue(), DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
			}
		} // waitRule != null
		//子JobTreeItemを取得
		for(JobTreeItem child : item.getChildren()){
			validateWaitRule(child);
		}
		return;
	}

	/**
	 * 待ち条件の判定対象チェック
	 * 
	 * @param item
	 *            ジョブ待ち条件情報をチェックするジョブツリーアイテム
	 * @throws InvalidSetting,
	 *             JobInvalid
	 * @throws HinemosUnknown 
	 */
	private static void validateWaitRuleObject(JobTreeItem item) throws InvalidSetting, JobInvalid, HinemosUnknown {
		// ジョブID取得
		String jobId = item.getData().getId();
		// 待ち条件情報を取得する
		JobWaitRuleInfo waitRule = item.getData().getWaitRule();
		// 終了値で指定できる判定条件
		Integer[] endValueConditions = { DecisionObjectConstant.EQUAL_NUMERIC, DecisionObjectConstant.NOT_EQUAL_NUMERIC,
				DecisionObjectConstant.IN_NUMERIC, DecisionObjectConstant.NOT_IN_NUMERIC };
		// ジョブ変数で指定できる判定条件
		Integer[] decisionConditions = { DecisionObjectConstant.EQUAL_NUMERIC, DecisionObjectConstant.NOT_EQUAL_NUMERIC,
				DecisionObjectConstant.GREATER_THAN, DecisionObjectConstant.GREATER_THAN_OR_EQUAL_TO,
				DecisionObjectConstant.LESS_THAN, DecisionObjectConstant.LESS_THAN_OR_EQUAL_TO,
				DecisionObjectConstant.EQUAL_STRING, DecisionObjectConstant.NOT_EQUAL_STRING,
				DecisionObjectConstant.IN_NUMERIC, DecisionObjectConstant.NOT_IN_NUMERIC };
		// ジョブ(戻り値)で指定できる判定条件
		Integer[] returnValueConditions = { DecisionObjectConstant.EQUAL_NUMERIC, DecisionObjectConstant.NOT_EQUAL_NUMERIC,
				DecisionObjectConstant.GREATER_THAN, DecisionObjectConstant.GREATER_THAN_OR_EQUAL_TO,
				DecisionObjectConstant.LESS_THAN, DecisionObjectConstant.LESS_THAN_OR_EQUAL_TO,
				DecisionObjectConstant.IN_NUMERIC, DecisionObjectConstant.NOT_IN_NUMERIC };
		if (waitRule.getObjectGroup() == null || waitRule.getObjectGroup().isEmpty()) {
			return;
		}
		for (JobObjectGroupInfo objectGroupInfo : waitRule.getObjectGroup()) {
			if (objectGroupInfo == null) {
				continue;
			}
			// 時刻は待ち条件として複数設定できない
			if (!typeUniqueCheck(objectGroupInfo.getJobObjectList(), JudgmentObjectConstant.TYPE_TIME)) {
				JobInvalid ji = new JobInvalid(MessageConstant.MESSAGE_WAIT_RULE_NOT_UNIQUE_TIME.getMessage());
				m_log.info("validateWaitRuleObject() : " + ji.getClass().getSimpleName() + ", " + ji.getMessage());
				throw ji;
			}
			// セッション開始後の時間は待ち条件として複数設定できない
			if (!typeUniqueCheck(objectGroupInfo.getJobObjectList(), JudgmentObjectConstant.TYPE_START_MINUTE)) {
				JobInvalid ji = new JobInvalid(MessageConstant.MESSAGE_WAIT_RULE_NOT_UNIQUE_START_MINUTE.getMessage());
				m_log.info("validateWaitRuleObject() : " + ji.getClass().getSimpleName() + ", " + ji.getMessage());
				throw ji;
			}

			for (JobObjectInfo objectInfo : objectGroupInfo.getJobObjectList()) {
				m_log.debug("objectInfo=" + objectInfo);

				switch (objectInfo.getType()) {
				case JudgmentObjectConstant.TYPE_JOB_END_STATUS:
					m_log.debug("Not Time and Not Delay");
					// 判定対象のジョブIDが同一階層に存在するかチェック
					validateJobId(item, jobId, objectInfo);
					break;
				case JudgmentObjectConstant.TYPE_JOB_END_VALUE:
					m_log.debug("Not Time and Not Delay");
					// 判定対象のジョブIDが同一階層に存在するかチェック
					validateJobId(item, jobId, objectInfo);
					// 終了値で指定できる判定条件以外は許容しない
					validateCondition(objectInfo.getDecisionCondition(), endValueConditions);
					CommonValidator.validateString(MessageConstant.COMPARISON_VALUE.getMessage(), objectInfo.getValue(),
							true, 1, 128);
					validateWaitComparisonValue(objectInfo.getValue(), objectInfo.getDecisionCondition());
					break;

				case JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS:
					// セッション横断ジョブ待ち条件の場合は判定対象が同一ジョブユニットであることをチェック
					validateCrossJobId(item, jobId, objectInfo);
					CommonValidator.validateInt(MessageConstant.CROSS_SESSION_RANGE.getMessage(),
							objectInfo.getCrossSessionRange(), 1, DataRangeConstant.SMALLINT_HIGH);
					break;

				case JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE:
					// セッション横断ジョブ待ち条件の場合は判定対象が同一ジョブユニットであることをチェック
					validateCrossJobId(item, jobId, objectInfo);
					// 終了値で指定できる判定条件以外は許容しない
					validateCondition(objectInfo.getDecisionCondition(), endValueConditions);
					CommonValidator.validateInt(MessageConstant.CROSS_SESSION_RANGE.getMessage(),
							objectInfo.getCrossSessionRange(), 1, DataRangeConstant.SMALLINT_HIGH);
					CommonValidator.validateString(MessageConstant.COMPARISON_VALUE.getMessage(), objectInfo.getValue(),
							true, 1, 128);
					validateWaitComparisonValue(objectInfo.getValue(), objectInfo.getDecisionCondition());
					break;

				case JudgmentObjectConstant.TYPE_JOB_PARAMETER:
					// ジョブ変数で指定できる判定条件以外は許容しない
					validateCondition(objectInfo.getDecisionCondition(), decisionConditions);
					// 条件判定の場合、設定値の妥当性チェックを行う
					CommonValidator.validateString(MessageConstant.WAIT_RULE_DECISION_VALUE_1.getMessage(),
							objectInfo.getDecisionValue(), true, 1, 128);
					validateParamValue(objectInfo.getDecisionValue(), objectInfo.getDecisionCondition(), false);
					CommonValidator.validateString(MessageConstant.WAIT_RULE_DECISION_VALUE_2.getMessage(),
							objectInfo.getValue(), true, 1, 128);
					validateParamValue(objectInfo.getValue(), objectInfo.getDecisionCondition(), true);
					break;

				case JudgmentObjectConstant.TYPE_TIME:
					if (objectInfo.getTime() < DATETIME_VALUE_MIN || DATETIME_VALUE_MAX < objectInfo.getTime()) {
						String[] args = { DATETIME_STRING_MIN, DATETIME_STRING_MAX, item.getData().getJobunitId(),
								jobId };
						InvalidSetting e = new InvalidSetting(
								MessageConstant.MESSAGE_INPUT_RANGE_OVER_JOB_SETTINGTIME.getMessage(args));
						m_log.info(
								"validateWaitRuleObject() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
						throw e;
					}
					break;

				case JudgmentObjectConstant.TYPE_START_MINUTE:
					CommonValidator.validateInt(MessageConstant.TIME_AFTER_SESSION_START.getMessage(),
							objectInfo.getStartMinute(), 0, DataRangeConstant.SMALLINT_HIGH);
					break;

				case JudgmentObjectConstant.TYPE_JOB_RETURN_VALUE:
					m_log.debug("Not Time and Not Delay");
					// 判定対象のジョブIDが同一階層に存在するかチェック
					validateJobId(item, jobId, objectInfo);
					// ジョブ(戻り値)で指定できる判定条件以外は許容しない
					validateCondition(objectInfo.getDecisionCondition(), returnValueConditions);
					CommonValidator.validateString(MessageConstant.COMPARISON_VALUE.getMessage(), objectInfo.getValue(), true,
							1, 128);
					validateWaitComparisonValue(objectInfo.getValue(), objectInfo.getDecisionCondition());
					break;

				default:
					HinemosUnknown e = new HinemosUnknown("validateWaitRuleObject(): unknown JudgmentObject.");
					m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage() + " jobId=" + jobId);
					throw e;
				}
				// 待ち条件の重複チェック
				if (objectGroupInfo.getJobObjectList().size() > 1) {
					validateUniqueJobObjectList(objectGroupInfo);
				}
			}
		}
		// 待ち条件群の重複チェック
		if (waitRule.getObjectGroup().size() > 1) {
			validateUniqueObjectGroup(waitRule);
		}
	}

	/**
	 * 待ち条件リストから選択したタイプの重複があるかチェックします
	 * 
	 * @param infoList
	 * @param type
	 * @return
	 */
	private static boolean typeUniqueCheck(List<JobObjectInfo> infoList, int type){
		int count = 0;
		for (JobObjectInfo i : infoList) {
			if (i.getType() == type) {
				count++;
			}
		}
		if(count > 1){
			return false;
		}
		return true;
	}

	/**
	 * @param item
	 * @param jobId
	 * @param objectInfo
	 * @throws JobInvalid
	 */
	private static void validateJobId(JobTreeItem item, String jobId, JobObjectInfo objectInfo) throws JobInvalid {
		boolean find = false;
		String targetJobId = objectInfo.getJobId();
		for (JobTreeItem child : item.getParent().getChildren()) {
			// ジョブIDをチェック
			JobInfo childInfo = child.getData();
			if (childInfo == null) {
				continue;
			}
			if (jobId.equals(childInfo.getId())) {
				continue;
			}
			if(objectInfo.getType() == JudgmentObjectConstant.TYPE_JOB_RETURN_VALUE){
				// ジョブ(戻り値)の場合はコマンドジョブかも含めてチェック
				if (targetJobId.equals(childInfo.getId()) && childInfo.getType() == JobConstant.TYPE_JOB) {
					find = true;
					break;
				}
			} else {
				if (targetJobId.equals(childInfo.getId())) {
					find = true;
					break;
				}
			}
		}
		if (!find) {
			String args[] = { jobId, targetJobId };
			JobInvalid ji = new JobInvalid(
					MessageConstant.MESSAGE_WAIT_JOBID_NG_INVALID_JOBID.getMessage(args));
			m_log.info("validateWaitRuleObject() : " + ji.getClass().getSimpleName() + ", " + ji.getMessage());
			throw ji;
		}
	}

	/**
	 * @param decisionCondition
	 * @param conditions
	 * @throws JobInvalid 
	 */
	private static void validateCondition(Integer decisionCondition, Integer[] conditions) throws JobInvalid {
		if (Arrays.asList(conditions).contains(decisionCondition)) {
			return;
		}
		ArrayList<String>messages = new ArrayList<String>();
		for(Integer type : conditions){
			messages.add(DecisionObjectConstant.typeToMessage(type));
		}
			
		JobInvalid ji = new JobInvalid(MessageConstant.MESSAGE_INPUT_NON_EXISTENT_MEMBER.getMessage(
				MessageConstant.WAIT_RULE_DECISION_CONDITION.getMessage(),
				messages.toString()));
		m_log.info("validateWaitRuleObject() : "
				+ ji.getClass().getSimpleName() + ", " + ji.getMessage());
		throw ji;
	}

	/**
	 * @param item
	 * @param jobId
	 * @param objectInfo
	 * @throws JobInvalid
	 */
	private static void validateCrossJobId(JobTreeItem item, String jobId, JobObjectInfo objectInfo) throws JobInvalid {
		JobTreeItem jobunitItem = item;
		//親ジョブユニットまでツリーを遡る
		while (jobunitItem.getData().getType() != JobConstant.TYPE_JOBUNIT) {
			jobunitItem = jobunitItem.getParent();
		}
		//親ジョブユニットに所属するジョブ要素を全件取得する
		List<String> jobIdList = getJobIdList(jobunitItem);
		//親ジョブユニットIDと自身のジョブIDは除外する
		jobIdList.remove(jobunitItem.getData().getId());
		jobIdList.remove(jobId);
		String targetCrossJobId = objectInfo.getJobId();
		if (!jobIdList.contains(targetCrossJobId)) {
			String args[] = { jobId, targetCrossJobId };
			JobInvalid ji = new JobInvalid(
					MessageConstant.MESSAGE_WAIT_CROSS_SESSION_JOBID_NG_INVALID_JOBID.getMessage(args));
			m_log.info(
					"validateWaitRuleObject() : " + ji.getClass().getSimpleName() + ", " + ji.getMessage());
			throw ji;
		}
	}

	/**
	 * 判定条件が数値でジョブ変数でないときは実数値チェックを行う ジョブ変数では数値範囲はDoubleを許容しているが実数値以外は許容しない
	 * 
	 * @param condition
	 * @param value1
	 * @return
	 */
	static final String regex = "^#\\[[a-zA-Z0-9-_:]+\\]$";
	static final Pattern pattern = Pattern.compile(regex);;
	private static void validateParamValue(String value, Integer condition, boolean isMulti)
			throws InvalidSetting {
		boolean conditionIsString = condition == DecisionObjectConstant.EQUAL_STRING
				|| condition == DecisionObjectConstant.NOT_EQUAL_STRING;
		// 判定条件が文字列のときはチェックを行わない
		if (conditionIsString) {
			return;
		}
		// 判定条件がIN(数値)またはNOT IN(数値)の場合のみカンマ、コロンで指定可能
		if ((condition == DecisionObjectConstant.IN_NUMERIC || condition == DecisionObjectConstant.NOT_IN_NUMERIC)
				&& isMulti) {
			// カンマ区切りで値を分割する
			String[] separatedValueArray = value.split(ValueSeparatorConstant.MULTIPLE, -1);
			try{
				for (String sepVal : separatedValueArray) {
					// Double.parseDoubleだとスペースを含んでいてもパースしてしまうため
					// ※ジョブ変数は仕様からスペースを含むことはない NG→#[TEST A]
					if(sepVal.contains(" ")){
						throw new NumberFormatException("Cannot contain spaces");
					}
					if (sepVal.contains(ValueSeparatorConstant.RANGE)) {
						// コロン区切りの範囲指定の場合、さらにコロンで分割する
						String[] valueRange = sepVal.split(ValueSeparatorConstant.RANGE);
						// (最小:最大)の2つの値の表現であるかチェック
						if (valueRange.length != 2) {
							throw new NumberFormatException("Only two numbers can be specified");
						}
						Double min = null;
						Double max = null;
						// 最小値がジョブ変数ではない時、数値チェックを行う
						if (!pattern.matcher(valueRange[0]).find()) {
							min = Double.parseDouble(valueRange[0]);
						}
						// 最大値がジョブ変数ではない時、数値チェックを行う
						if (!pattern.matcher(valueRange[1]).find()) {
							max = Double.parseDouble(valueRange[1]);
						}
						// 範囲指定が両方ともジョブ変数ではなかった場合、最小:最大のように表現されているかチェックを行う
						if (min != null && max != null && min >= max) {
							throw new NumberFormatException("Set the minimum value to exceed the maximum value");
						}
					} else {
						// 判定値がジョブ変数ではない時、数値チェックを行う
						if (!pattern.matcher(sepVal).find()) {
							Double.parseDouble(sepVal);
						}
					}
				}
			} catch (NumberFormatException e) {
				m_log.debug("validateParamValue() : " + e.getMessage());
				InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_WAIT_VALUE_NG_PARAM_FORMAT.getMessage());
				m_log.info("validateParamValue() : "
						+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
				throw e1;
			}
		} else {
			// 判定値がジョブ変数ではない時、数値チェックを行う
			try{
				// Double.parseDoubleだとスペースを含んでいてもパースしてしまうため
				// ※ジョブ変数は仕様からスペースを含むことはない NG→#[TEST A]
				if(value.contains(" ")){
					throw new NumberFormatException("Cannot contain spaces");
				}
				if (!pattern.matcher(value).find()) {
					Double.parseDouble(value);
				}
			} catch (NumberFormatException e) {
				m_log.debug("validateParamValue() : " + e.getMessage());
				InvalidSetting e1 = new InvalidSetting(
						MessageConstant.MESSAGE_WAIT_VALUE_NG_PARAM.getMessage());
				m_log.info("validateParamValue() : " + e1.getClass().getSimpleName() + ", " + e1.getMessage());
				throw e1;
			}
		}
	}

	/**
	 * カンマ、コロンで指定可能の数値についてのバリデーション
	 * 例 1,2,3,4-5
	 * 
	 * @param objectValue
	 * @throws InvalidSetting
	 * @throws JobInvalid 
	 */
	private static void validateWaitComparisonValue(String objectInfoValue, int decisionCondition)
			throws InvalidSetting {
		// 判定条件がIN(数値)またはNOT IN(数値)の場合のみカンマ、コロンで指定可能
		if (decisionCondition == DecisionObjectConstant.IN_NUMERIC
				|| decisionCondition == DecisionObjectConstant.NOT_IN_NUMERIC) {
			try {
				String[] valueArray = objectInfoValue.split(ValueSeparatorConstant.MULTIPLE, -1);
				for (String value : valueArray) {
					if (value.contains(ValueSeparatorConstant.RANGE)) {
						// 範囲指定の場合
						String[] valueRange = value.split(ValueSeparatorConstant.RANGE);
						// 最小、最大の2つの値の表現であるかチェック
						if (valueRange.length != 2) {
							throw new NumberFormatException("Only two numbers can be specified");
						}
						// 最小:最大のように表現されているかチェック
						int min = Integer.parseInt(valueRange[0]);
						int max = Integer.parseInt(valueRange[1]);
						if (min >= max) {
							throw new NumberFormatException("Set the minimum value to exceed the maximum value");
						}
						// 最小値の範囲チェック
						CommonValidator.validateInt(MessageConstant.COMPARISON_VALUE.getMessage(), min,
								DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
						// 最大値の範囲チェック
						CommonValidator.validateInt(MessageConstant.COMPARISON_VALUE.getMessage(), max,
								DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
					} else {
						CommonValidator.validateInt(MessageConstant.COMPARISON_VALUE.getMessage(),
								Integer.parseInt(value), DataRangeConstant.SMALLINT_LOW,
								DataRangeConstant.SMALLINT_HIGH);
					}
				}
			} catch (NumberFormatException e) {
				m_log.debug("validateWaitComparisonValue() : " + e.getMessage());
				InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_WAIT_VALUE_NG_NUMERIC_FORMAT.getMessage());
				m_log.info("validateWaitComparisonValue() : "
						+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
				throw e1;
			}
		} else {
			CommonValidator.validateIntegerString(MessageConstant.COMPARISON_VALUE.getMessage(), objectInfoValue, true,
					0, DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH);
		}
	}

	/**
	 * 待ち条件の重複チェック
	 * 
	 * @param objectGroupInfo
	 *            ジョブ待ち条件群
	 * @throws JobInvalid
	 */
	private static void validateUniqueJobObjectList(JobObjectGroupInfo objectGroupInfo) throws JobInvalid {
		Set<JobObjectInfo> decisionSet = new HashSet<JobObjectInfo>();

		// 重複の判定に説明は含まない
		String[] cList = new String[objectGroupInfo.getJobObjectList().size()];
		for (int i = 0; i < objectGroupInfo.getJobObjectList().size(); i++) {
			cList[i] = objectGroupInfo.getJobObjectList().get(i).getDescription();
			objectGroupInfo.getJobObjectList().get(i).setDescription(null);
		}

		boolean notUnique = false;
		for (JobObjectInfo info : objectGroupInfo.getJobObjectList()) {
			if (decisionSet.contains(info)) {
				notUnique = true;
				break;
			} else {
				decisionSet.add(info);
			}
		}

		for (int i = 0; i < objectGroupInfo.getJobObjectList().size(); i++) {
			objectGroupInfo.getJobObjectList().get(i).setDescription(cList[i]);
		}

		if (notUnique) {
			JobInvalid ji = new JobInvalid(MessageConstant.MESSAGE_WAIT_RULE_NOT_UNIQUE.getMessage());
			m_log.info("validateWaitRule() : " + ji.getClass().getSimpleName() + ", " + ji.getMessage());
			throw ji;
		}
		return;
	}

	/**
	 * 待ち条件群の重複チェック
	 * 
	 * @param waitRule
	 *            待ち条件情報
	 * @throws JobInvalid
	 */
	private static void validateUniqueObjectGroup(JobWaitRuleInfo waitRule) throws JobInvalid {
		Set<JobObjectGroupInfo> decisionSet = new HashSet<JobObjectGroupInfo>();

		// waitGroup 比較対象の待ち条件群
		for (JobObjectGroupInfo waitGroup : waitRule.getObjectGroup()) {
			boolean notUnique = false;
			int targetSize = waitGroup.getJobObjectList().size();

			// 待ち条件群の重複チェック(待ち条件の順不同対応)
			for (JobObjectGroupInfo decisions : decisionSet) {
				// 比較対象が同じ要素数の場合のみ比較
				if (targetSize != decisions.getJobObjectList().size()) {
					continue;
				}

				// 重複の判定に説明とジョブ名は含まない。
				String[] cDescList = new String[decisions.getJobObjectList().size()];

				// 待ち条件群リストの説明のコピーを控える
				for (int i = 0; i < decisions.getJobObjectList().size(); i++) {
					cDescList[i] = decisions.getJobObjectList().get(i).getDescription();
					decisions.getJobObjectList().get(i).setDescription(null);
				}

				int matchCount = 0;
				for (JobObjectInfo wait : waitGroup.getJobObjectList()) {
					// 判定対象の説明のコピーを控える
					String cDesc = wait.getDescription();
					wait.setDescription(null);

					if (decisions.getJobObjectList().contains(wait)) {
						matchCount++;
					}
					wait.setDescription(cDesc);
				}
				// 要素数分一致した場合はユニークではない
				notUnique = targetSize == matchCount;
				for (int i = 0; i < decisions.getJobObjectList().size(); i++) {
					decisions.getJobObjectList().get(i).setDescription(cDescList[i]);
				}
				if (notUnique) {
					break;
				}
			}
			if (notUnique) {
				if (targetSize == 1) {
					// 要素数が1の場合は単体なので「待ち条件」表記
					JobInvalid ji = new JobInvalid(MessageConstant.MESSAGE_WAIT_RULE_NOT_UNIQUE.getMessage());
					m_log.info("validateWaitRule() : " + ji.getClass().getSimpleName() + ", " + ji.getMessage());
					throw ji;
				} else {
					JobInvalid ji = new JobInvalid(MessageConstant.MESSAGE_WAIT_RULE_GROUP_NOT_UNIQUE.getMessage());
					m_log.info("validateWaitRule() : " + ji.getClass().getSimpleName() + ", " + ji.getMessage());
					throw ji;
				}
			} else {
				decisionSet.add(waitGroup);
			}
		}
		return;
	}

	/**
	 * 参照ジョブにて指定された参照先のジョブ情報をチェックする
	 * 
	 * @param item
	 *            参照先のジョブ情報をチェックするジョブツリーアイテム
	 */
	private static void validateReferJob(JobTreeItem item) throws JobInvalid {
		if (item == null || item.getData() == null) {
			return;
		}

		// 配下に存在する参照ジョブのみを取得する
		ArrayList<JobInfo> referJobList = JobUtil.findReferJob(item);
		m_log.trace("ReferJob count : " + referJobList.size());
		for (JobInfo referJob : referJobList) {
			String referJobId = referJob.getReferJobId();
			m_log.trace("ReferJobID : " + referJobId);
			// 参照先に有効なジョブが存在しているか調べる
			int ret = JobUtil.checkValidJob(item, referJobId, referJob.getReferJobSelectType());
			if (ret != 0) {
				// 有効なジョブが存在しないため、メッセージ出力
				String args[] = { referJob.getId(), referJobId };
				if (ret == 1) {
					// モジュール登録設定不一致
					throw new JobInvalid(MessageConstant.MESSAGE_REFERENCE_JOBID_NG_INVALID_SETTING.getMessage(args));
				} else {
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
	 * @param item
	 *            参照先のジョブネット情報をチェックするジョブツリーアイテム
	 */
	private static void validateReferJobNet(JobTreeItem item) throws JobInvalid {
		if (item == null || item.getData() == null) {
			return;
		}

		// 配下に存在する参照ジョブネットのみを取得する
		ArrayList<JobInfo> referJobNetList = JobUtil.findReferJobNet(item);
		m_log.trace("ReferJobNet count : " + referJobNetList.size());
		for (JobInfo referJobNet : referJobNetList) {
			String referJobNetId = referJobNet.getReferJobId();
			m_log.trace("ReferJobNetID : " + referJobNetId);
			// 参照先に有効なジョブネットが存在しているか調べる
			int ret = JobUtil.checkValidJobNet(item, referJobNetId, referJobNet);
			if (ret != 0) {
				// 有効なジョブネットが存在しないため、メッセージ出力
				String args[] = { referJobNet.getId(), referJobNetId };
				if (ret == 1) {
					// モジュール登録設定不一致
					throw new JobInvalid(MessageConstant.MESSAGE_REFERENCE_JOBID_NG_INVALID_SETTING.getMessage(args));
				} else if (ret == 2) {
					// 参照先に参照ジョブネットが含まれる
					throw new JobInvalid(
							MessageConstant.MESSAGE_REFERENCE_JOBID_NG_INVALID_SUBORDINATE_JOB.getMessage(args));
				} else {
					// 参照先が存在しない
					throw new JobInvalid(MessageConstant.MESSAGE_REFERENCE_JOBID_NG_INVALID_JOBID.getMessage(args));
				}

			}
		}
		return;
	}

	/**
	 * 予約済み（利用不可）のジョブIDでないかチェックする
	 * 
	 * @param jobId
	 *            ジョブID
	 */
	public static boolean isReservedJobId(String jobId) throws InvalidSetting {
		if (CreateJobSession.TOP_JOB_ID.equals(jobId)) {
			return true;
		}
		return false;
	}
	/**
	 * 予約済み（利用不可）のジョブユニットIDでないかチェックする
	 * 
	 * @param jobId
	 *            ジョブID
	 */
	public static boolean isReservedJobUnitId(String jobUnitId) throws InvalidSetting {
		if (CreateJobSession.TOP_JOB_ID.equals(jobUnitId)) {
			return true;
		}
		if (CreateJobSession.TOP_JOBUNIT_ID.equals(jobUnitId)) {
			return true;
		}
		return false;
	}
}
