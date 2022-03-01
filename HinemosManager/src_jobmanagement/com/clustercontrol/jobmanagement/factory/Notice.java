/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobLinkMessageId;
import com.clustercontrol.jobmanagement.bean.OperationConstant;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.rpa.bean.RpaJobErrorTypeConstant;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.jobmanagement.util.ParameterUtil;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.notify.bean.NotifyTriggerType;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.util.NotifyCallback;
import com.clustercontrol.notify.util.NotifyUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Messages;

/**
 * 監視管理に通知するクラスです。
 *
 * @version 3.0.0
 * @since 2.0.0
 */
public class Notice {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( Notice.class );

	/**
	 * セッションID、ジョブユニットID、ジョブIDからジョブ通知情報を取得し、<BR>
	 * ジョブ通知情報と終了状態を基に、ログ出力情報作成し、監視管理に通知します。
	 * 
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param type 終了状態
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * 
	 * @see com.clustercontrol.bean.EndStatusConstant
	 * @see com.clustercontrol.bean.JobConstant
	 * @see com.clustercontrol.monitor.message.LogOutputNotifyInfo
	 */
	public void notify(String sessionId, String jobunitId, String jobId, Integer type) throws JobInfoNotFound, InvalidRole {
		m_log.debug("notify() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId + ", type=" + type);

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {

			JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
			JobInfoEntity job = sessionJob.getJobInfoEntity();
			Integer priority = getPriority(type, job);

			// 通知先の指定において以下の場合は通知しない。
			// 1.重要度がnull、もしくはPriorityConstant.TYPE_NONE（空欄）
			// 2.通知IDがnull、もしくは0文字
			if (priority == null || priority == PriorityConstant.TYPE_NONE
					|| job.getNotifyGroupId() == null
					|| job.getNotifyGroupId().isEmpty()) {
				return;
			}

			//通知する
			Locale locale = NotifyUtil.getNotifyLocale();
			
			//通知情報作成
			OutputBasicInfo info = new OutputBasicInfo();
			// 通知グループID
			info.setNotifyGroupId(job.getNotifyGroupId());
			// ジョブ連携メッセージID
			info.setJoblinkMessageId(JobLinkMessageId.getIdForJob(NotifyTriggerType.JOB_END, jobunitId, jobId));
			//プラグインID
			info.setPluginId(HinemosModuleConstant.JOB);
			//アプリケーション
			info.setApplication(HinemosMessage.replace(MessageConstant.JOB_MANAGEMENT.getMessage(), locale));
			//監視項目ID
			info.setMonitorId(sessionId);
			//ジョブユニットID
			info.setJobunitId(jobunitId);
			//ジョブID
			info.setJobId(jobId);
			
			//メッセージID、メッセージ、オリジナルメッセージ
			if(type == EndStatusConstant.TYPE_BEGINNING){
				// ジョブ連携メッセージID
				info.setJoblinkMessageId(JobLinkMessageId.getIdForJob(NotifyTriggerType.JOB_START, jobunitId, jobId));
				// メッセージ
				String jobType = Messages.getString(JobConstant.typeToMessageCode(job.getJobType()), locale);
				String jobName = job.getJobName();
				String[] args1 = {jobType,jobName,jobId,sessionId};
				info.setMessage(MessageConstant.MESSAGE_STARTED_SUCCESSFULLY.getMessage(args1));
			} else {
				// ジョブ連携メッセージID
				info.setJoblinkMessageId(JobLinkMessageId.getIdForJob(NotifyTriggerType.JOB_END, jobunitId, jobId));
				// メッセージ
				if(type == EndStatusConstant.TYPE_NORMAL){
					String jobType = Messages.getString(JobConstant.typeToMessageCode(job.getJobType()), locale);
					String jobName = job.getJobName();
					String[] args1 = {jobType,jobName,jobId,sessionId};
					info.setMessage(MessageConstant.MESSAGE_STOPPED_STATUS_NORMAL.getMessage(args1));
				} else if(type == EndStatusConstant.TYPE_WARNING){
					String jobType = Messages.getString(JobConstant.typeToMessageCode(job.getJobType()), locale);
					String jobName = job.getJobName();
					String[] args1 = {jobType,jobName,jobId,sessionId};
					info.setMessage(MessageConstant.MESSAGE_STOPPED_STATUS_WARNING.getMessage(args1));
				} else if(type == EndStatusConstant.TYPE_ABNORMAL){
					String jobType = Messages.getString(JobConstant.typeToMessageCode(job.getJobType()), locale);
					String jobName = job.getJobName();
					String[] args1 = {jobType,jobName,jobId,sessionId};
					info.setMessage(MessageConstant.MESSAGE_STOPPED_STATUS_ERROR.getMessage(args1));
				}
			}
			if(job.getJobType() == JobConstant.TYPE_JOB
					|| job.getJobType() == JobConstant.TYPE_APPROVALJOB
					|| job.getJobType() == JobConstant.TYPE_MONITORJOB
					|| job.getJobType() == JobConstant.TYPE_JOBLINKSENDJOB
					|| job.getJobType() == JobConstant.TYPE_JOBLINKRCVJOB
					|| job.getJobType() == JobConstant.TYPE_FILECHECKJOB
					|| job.getJobType() == JobConstant.TYPE_RESOURCEJOB
					|| job.getJobType() == JobConstant.TYPE_RPAJOB){
				//ファシリティID
				// "#[...]"形式の場合はジョブ変数の置換を試みる。
				String facilityId = ParameterUtil.replaceFacilityId(sessionId, job.getFacilityId());
				info.setFacilityId(facilityId);
				//スコープ
				info.setScopeText(sessionJob.getScopeText());
				if(m_log.isDebugEnabled()){
					m_log.debug("Notice.notify  >>>info.setFacilityId() = : " + facilityId);
					m_log.debug("Notice.notify  >>>info.setScopeText() = : " + info.getScopeText());
				}
			} else {
				//ファシリティID
				info.setFacilityId("");
				//スコープ
				info.setScopeText("");
			}
			
			// 承認ジョブに関する情報設定
			if(job.getJobType() == JobConstant.TYPE_APPROVALJOB){
				info.setJobApprovalText(job.getApprovalReqSentence());
				if(job.isUseApprovalReqSentence()){
					String jobApprovalMail = job.getApprovalReqSentence();
					// 承認依頼文を利用する場合リンクアドレスを付与する。取得に失敗しても処理は継続する。
					try {
						jobApprovalMail += "\r\n\r\n" + new JobControllerBean().getApprovalPageLink();
					} catch (HinemosUnknown e) {
						// 処理しない
					}
					info.setJobApprovalMail(jobApprovalMail);
				}else{
					info.setJobApprovalMail(job.getApprovalReqMailBody());
				}
			}
			
			//重要度
			m_log.debug("priority = " + priority);
			info.setPriority(priority);
			//発生日時
			info.setGenerationDate(HinemosTime.getDateInstance().getTime());
			//JOB_MESSAGE
			setJobMessage(sessionJob, info);

			//メッセージ送信
			if (m_log.isDebugEnabled()) {
				m_log.debug("sending message"
						+ " : priority=" + info.getPriority()
						+ " pluginId=" + info.getPluginId()
						+ " monitorId=" + info.getMonitorId()
						+ " facilityId=" + info.getFacilityId() + ")");
			}
			// 通知設定
			jtm.addCallback(new NotifyCallback(info));
		}
	}

	private Integer getPriority(Integer type, JobInfoEntity job) {
		Integer priority = null;
		switch (type) {
		case EndStatusConstant.TYPE_BEGINNING:
			priority = job.getBeginPriority();
			break;
		case EndStatusConstant.TYPE_NORMAL:
			priority = job.getNormalPriority();
			break;
		case EndStatusConstant.TYPE_WARNING:
			priority = job.getWarnPriority();
			break;
		case EndStatusConstant.TYPE_ABNORMAL:
			priority = job.getAbnormalPriority();
			break;
		default:
			break;
		}
		return priority;
	}

	/**
	 * 遅延通知
	 * セッションID、ジョブユニットID、ジョブIDからジョブ通知情報を取得し、<BR>
	 * ジョブ通知情報と開始遅延フラグを基に、ログ出力情報作成し、監視管理に通知します。
	 * 
	 * @param sessionId セッションID
	 * @param jobId ジョブID
	 * @param startDelay 開始遅延フラグ（true：開始遅延、false：終了遅延）
	 * @param reason 遅延判定で使われた条件
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * 
	 * @see com.clustercontrol.bean.EndStatusConstant
	 * @see com.clustercontrol.bean.JobConstant
	 * @see com.clustercontrol.monitor.message.LogOutputNotifyInfo
	 */
	public void delayNotify(String sessionId, String jobunitId, String jobId, boolean startDelay, String reason) throws JobInfoNotFound, InvalidRole {
		m_log.debug("delayNotify() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId + ", startDelay=" + startDelay);

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {

			JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
			JobInfoEntity job = sessionJob.getJobInfoEntity();

			// 通知先の指定において以下の場合は通知しない
			// 1.通知IDがnull、もしくは0文字
			if(job.getNotifyGroupId() == null || job.getNotifyGroupId().isEmpty()){
				return;
			}

			//通知する

			//通知情報作成
			OutputBasicInfo info = new OutputBasicInfo();
			// 通知グループID
			info.setNotifyGroupId(job.getNotifyGroupId());
			// ジョブ連携メッセージID
			if (startDelay) {
				// 開始遅延
				info.setJoblinkMessageId(JobLinkMessageId.getIdForJob(NotifyTriggerType.JOB_START_DELAY,
						jobunitId, jobId));
			} else {
				// 終了遅延
				info.setJoblinkMessageId(JobLinkMessageId.getIdForJob(NotifyTriggerType.JOB_END_DELAY,
						jobunitId, jobId));
			}
			//プラグインID
			info.setPluginId(HinemosModuleConstant.JOB);
			//アプリケーション
			info.setApplication(HinemosMessage.replace(MessageConstant.JOB_MANAGEMENT.getMessage(), NotifyUtil.getNotifyLocale()));
			//監視項目ID
			info.setMonitorId(sessionId);

			Locale locale = NotifyUtil.getNotifyLocale();

			//メッセージID、メッセージ、オリジナルメッセージ
			StringBuilder message = new StringBuilder();
			StringBuilder orgMessage = new StringBuilder();
			if(startDelay){
				String jobType = Messages.getString(JobConstant.typeToMessageCode(job.getJobType()), locale);
				String jobName = job.getJobName();
				String[] args1 = {jobType,jobName,jobId,sessionId};
				message.append(MessageConstant.MESSAGE_DELAY_OF_START_OCCURRED.getMessage(args1));
				orgMessage.append(MessageConstant.MESSAGE_DELAY_OF_START_OCCURRED.getMessage(args1)).append("\n");
				orgMessage.append(reason);

				//操作
				if(job.getStartDelayOperation().booleanValue()){
					int type = job.getStartDelayOperationType();

					String[] args2 = {Messages.getString(OperationConstant.typeToMessageCode(type), locale)};
					message.append(" " + MessageConstant.MESSAGE_OPERATION_IS_ABOUT_TO_BE_RUN.getMessage(args2));
					orgMessage.append(MessageConstant.MESSAGE_OPERATION_IS_ABOUT_TO_BE_RUN.getMessage(args2));

					if(type == OperationConstant.TYPE_STOP_SKIP){
						String[] args3 = {job.getStartDelayOperationEndValue().toString()};
						message.append(" " + MessageConstant.MESSAGE_END_VALUE.getMessage(args3));
						orgMessage.append(MessageConstant.MESSAGE_END_VALUE.getMessage(args3));
					}
				}
				info.setMessage(message.toString());
				info.setMessageOrg(orgMessage.toString());
			} else {
				String jobType = Messages.getString(JobConstant.typeToMessageCode(job.getJobType()), locale);
				String jobName = job.getJobName();
				String[] args1 = {jobType,jobName,jobId,sessionId};
				message.append(MessageConstant.MESSAGE_DELAY_OF_END_OCCURRED.getMessage(args1));
				orgMessage.append(MessageConstant.MESSAGE_DELAY_OF_END_OCCURRED.getMessage(args1)).append("\n");
				orgMessage.append(reason);

				//操作
				if(job.getEndDelayOperation().booleanValue()){
					int type = job.getEndDelayOperationType();

					String[] args2 = {Messages.getString(OperationConstant.typeToMessageCode(type), locale)};
					message.append(" " + MessageConstant.MESSAGE_OPERATION_IS_ABOUT_TO_BE_RUN.getMessage(args2));
					orgMessage.append(MessageConstant.MESSAGE_OPERATION_IS_ABOUT_TO_BE_RUN.getMessage(args2));
				}
				info.setMessage(message.toString());
				info.setMessageOrg(orgMessage.toString());
			}

			if(job.getJobType() == JobConstant.TYPE_JOB
					|| job.getJobType() == JobConstant.TYPE_APPROVALJOB
					|| job.getJobType() == JobConstant.TYPE_MONITORJOB
					|| job.getJobType() == JobConstant.TYPE_JOBLINKSENDJOB
					|| job.getJobType() == JobConstant.TYPE_JOBLINKRCVJOB
					|| job.getJobType() == JobConstant.TYPE_FILECHECKJOB
					|| job.getJobType() == JobConstant.TYPE_RESOURCEJOB
					|| job.getJobType() == JobConstant.TYPE_RPAJOB){
				//ファシリティID
				info.setFacilityId(job.getFacilityId());
				//スコープ
				info.setScopeText(sessionJob.getScopeText());
			} else {
				//ファシリティID
				info.setFacilityId("");
				//スコープ
				info.setScopeText("");
			}
			//重要度
			if(startDelay) {
				info.setPriority(job.getStartDelayNotifyPriority());
			} else {
				info.setPriority(job.getEndDelayNotifyPriority());
			}
			//発生日時
			info.setGenerationDate(HinemosTime.getDateInstance().getTime());
			//JOB_MESSAGE
			setJobMessage(sessionJob, info);

			// 通知設定
			jtm.addCallback(new NotifyCallback(info));
		}
	}

	/**
	 * 多重度通知
	 * セッションID、ジョブユニットID、ジョブIDからジョブ通知情報を取得し、<BR>
	 * ジョブ通知情報を基に、ログ出力情報作成し、監視管理に通知します。
	 * 
	 * @param sessionId セッションID
	 * @param jobId ジョブID
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * 
	 * @see com.clustercontrol.bean.EndStatusConstant
	 * @see com.clustercontrol.bean.JobConstant
	 * @see com.clustercontrol.monitor.message.LogOutputNotifyInfo
	 */
	public void multiplicityNotify(String sessionId, String jobunitId, String jobId, int operationType) throws JobInfoNotFound, InvalidRole {
		m_log.debug("multiplicityNotify() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId + ", type=" + operationType);

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {

			JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
			JobInfoEntity job = sessionJob.getJobInfoEntity();

			// 通知先の指定において以下の場合は通知しない
			// 1.通知IDがnull、もしくは0文字
			if(job.getNotifyGroupId() == null || job.getNotifyGroupId().length() == 0){
				return;
			}

			//通知する

			//通知情報作成
			OutputBasicInfo info = new OutputBasicInfo();
			// 通知グループID
			info.setNotifyGroupId(job.getNotifyGroupId());
			// ジョブ連携メッセージID
			info.setJoblinkMessageId(JobLinkMessageId.getIdForJob(NotifyTriggerType.JOB_EXCEEDED_MULTIPLICITY,
					jobunitId, jobId));
			//プラグインID
			info.setPluginId(HinemosModuleConstant.JOB);
			//アプリケーション
			info.setApplication(HinemosMessage.replace(MessageConstant.JOB_MANAGEMENT.getMessage(), NotifyUtil.getNotifyLocale()));
			//監視項目ID
			info.setMonitorId(sessionId);
			
			Locale locale = NotifyUtil.getNotifyLocale();
			info.setMessage(MessageConstant.MESSAGE_EXCEEDED_MULTIPLICITY_OF_JOBS.getMessage() + "(" 
			+ Messages.getString(StatusConstant.typeToMessageCode(operationType), locale) + ")");

			if(job.getJobType() == JobConstant.TYPE_JOB
					|| job.getJobType() == JobConstant.TYPE_APPROVALJOB
					|| job.getJobType() == JobConstant.TYPE_MONITORJOB
					|| job.getJobType() == JobConstant.TYPE_JOBLINKSENDJOB
					|| job.getJobType() == JobConstant.TYPE_JOBLINKRCVJOB
					|| job.getJobType() == JobConstant.TYPE_FILECHECKJOB
					|| job.getJobType() == JobConstant.TYPE_RESOURCEJOB
					|| job.getJobType() == JobConstant.TYPE_RPAJOB){
				//ファシリティID
				info.setFacilityId(job.getFacilityId());
				//スコープ
				info.setScopeText(sessionJob.getScopeText());
			} else {
				//ファシリティID
				info.setFacilityId("");
				//スコープ
				info.setScopeText("");
			}
			//重要度
			info.setPriority(job.getMultiplicityNotifyPriority());
			//発生日時
			info.setGenerationDate(HinemosTime.getDateInstance().getTime());
			//JOB_MESSAGE
			setJobMessage(sessionJob, info);

			// 通知設定
			jtm.addCallback(new NotifyCallback(info));
		}
	}

	/**
	 * 通知基本情報のJOB_MESSAGEについて設定します。
	 *
	 * @param entity
	 * @param info 通知基本情報
	 */
	private void setJobMessage(JobSessionJobEntity entity, OutputBasicInfo info) {

		List<JobSessionNodeEntity> nodes = entity.getJobSessionNodeEntities();
		List<String> facilityIds = new ArrayList<String>();
		List<String> jobMessages = new ArrayList<String>();
		for (JobSessionNodeEntity node : nodes) {
			facilityIds.add(node.getId().getFacilityId());
			jobMessages.add(node.getMessage());
			m_log.debug("Notice.notify  >>>info.setJobFacilityId() = : " + node.getId().getFacilityId());
			m_log.debug("Notice.notify  >>>info.setJobMessage() = : " + node.getMessage());
		}
		info.setJobFacilityId(facilityIds);
		info.setJobMessage(jobMessages);
	}
	
	public void notify(String sessionId, String jobunitId, String jobId, Integer priority, String message, String org) throws JobInfoNotFound, InvalidRole {
		m_log.debug("notify() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId + ", priority=" + priority);

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
			JobInfoEntity job = sessionJob.getJobInfoEntity();

			//通知する
			Locale locale = NotifyUtil.getNotifyLocale();
			
			//通知情報作成
			OutputBasicInfo info = new OutputBasicInfo();
			// 通知グループID
			info.setNotifyGroupId(job.getNotifyGroupId());
			//プラグインID
			info.setPluginId(HinemosModuleConstant.JOB);
			//アプリケーション
			info.setApplication(HinemosMessage.replace(MessageConstant.JOB_MANAGEMENT.getMessage(), locale));
			//監視項目ID
			info.setMonitorId(sessionId);
			//ジョブユニットID
			info.setJobunitId(jobunitId);
			//ジョブID
			info.setJobId(jobId);

			//ファシリティID
			String facilityId = ParameterUtil.replaceFacilityId(sessionId, job.getFacilityId());
			info.setFacilityId(facilityId);
			//スコープ
			info.setScopeText(sessionJob.getScopeText());
			if(m_log.isDebugEnabled()){
				m_log.debug("Notice.notify  >>>info.setFacilityId() = : " + facilityId);
				m_log.debug("Notice.notify  >>>info.setScopeText() = : " + info.getScopeText());
			}
			
			//重要度
			info.setPriority(priority);
			//発生日時
			info.setGenerationDate(HinemosTime.getDateInstance().getTime());
			//JOB_MESSAGE
			setJobMessage(sessionJob, info);

			info.setMessage(message);
			info.setMessageOrg(org);

			//メッセージ送信
			if (m_log.isDebugEnabled()) {
				m_log.debug("sending message"
						+ " : priority=" + info.getPriority()
						+ " pluginId=" + info.getPluginId()
						+ " monitorId=" + info.getMonitorId()
						+ " facilityId=" + info.getFacilityId() + ")");
			}
			// 通知設定
			jtm.addCallback(new NotifyCallback(info));
		}
	}

	public void rpaErrorNotify(String sessionId, String jobunitId, String jobId, Integer errorType) throws JobInfoNotFound, InvalidRole {
		m_log.debug("rpaErrorNotify() : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId + ", type=" + errorType);
		
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {

			JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
			JobInfoEntity job = sessionJob.getJobInfoEntity();

			// 通知先の指定において以下の場合は通知しない
			// 1.通知IDがnull、もしくは0文字
			if(job.getNotifyGroupId() == null || job.getNotifyGroupId().isEmpty()){
				return;
			}

			//通知する

			//通知情報作成
			OutputBasicInfo info = new OutputBasicInfo();
			// 通知グループID
			info.setNotifyGroupId(job.getNotifyGroupId());
			//プラグインID
			info.setPluginId(HinemosModuleConstant.JOB);
			//アプリケーション
			info.setApplication(HinemosMessage.replace(MessageConstant.JOB_MANAGEMENT.getMessage(), NotifyUtil.getNotifyLocale()));
			//監視項目ID
			info.setMonitorId(sessionId);
			//ファシリティID
			info.setFacilityId(job.getFacilityId());
			//スコープ
			info.setScopeText(sessionJob.getScopeText());

			Locale locale = NotifyUtil.getNotifyLocale();
			//メッセージID、メッセージ、オリジナルメッセージ
			StringBuilder message = new StringBuilder();
			StringBuilder orgMessage = new StringBuilder();
			String jobType = Messages.getString(JobConstant.typeToMessageCode(job.getJobType()), locale);
			String jobName = job.getJobName();
			int priority = PriorityConstant.TYPE_UNKNOWN;
			String[] messageArgs = {jobType,jobName,jobId,sessionId};
			switch(errorType) {
				case RpaJobErrorTypeConstant.NOT_LOGIN:
					priority = job.getRpaNotLoginNotifyPriority(); 
					message.append(MessageConstant.MESSAGE_RPA_NOT_LOGIN_NOTIFY_MSG.getMessage(messageArgs));
					orgMessage.append(MessageConstant.MESSAGE_RPA_NOT_LOGIN_NOTIFY_MSG.getMessage(messageArgs));
					break;
				case RpaJobErrorTypeConstant.ALREADY_RUNNING:
					priority = job.getRpaAlreadyRunningNotifyPriority(); 
					message.append(MessageConstant.MESSAGE_RPA_ALREADY_RUNNING_NOTIFY_MSG.getMessage(messageArgs));
					orgMessage.append(MessageConstant.MESSAGE_RPA_ALREADY_RUNNING_NOTIFY_MSG.getMessage(messageArgs));
					break;
				case RpaJobErrorTypeConstant.ABNORMAL_EXIT:
					priority = job.getRpaAbnormalExitNotifyPriority(); 
					message.append(MessageConstant.MESSAGE_RPA_ABNORMAL_EXIT_NOTIFY_MSG.getMessage(messageArgs));
					orgMessage.append(MessageConstant.MESSAGE_RPA_ABNORMAL_EXIT_NOTIFY_MSG.getMessage(messageArgs));
					break;
				default:
					// NOT_LOGIN, ALREADY_RUNNING, ABNORMAL_EXIT以外で呼ばれることはない想定。
					m_log.warn("rpaErrorNotify() : invalid errorType=" + errorType);
					return;
			}
			info.setMessage(message.toString());
			info.setMessageOrg(orgMessage.toString());
			//重要度
			info.setPriority(priority);
			//発生日時
			info.setGenerationDate(HinemosTime.getDateInstance().getTime());
			// 通知設定
			jtm.addCallback(new NotifyCallback(info));
		}
	}
}
