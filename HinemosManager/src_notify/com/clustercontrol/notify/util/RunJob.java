/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.LocalInfoUtil;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.JobSessionDuplicate;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.jobmanagement.bean.JobLinkExpInfo;
import com.clustercontrol.jobmanagement.bean.JobTriggerInfo;
import com.clustercontrol.jobmanagement.bean.JobTriggerTypeConstant;
import com.clustercontrol.jobmanagement.bean.ProcessingMethodConstant;
import com.clustercontrol.jobmanagement.model.JobLinkSendSettingEntity;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.jobmanagement.util.JobLinkUtil;
import com.clustercontrol.jobmanagement.util.JobValidator;
import com.clustercontrol.notify.bean.ExecFacilityConstant;
import com.clustercontrol.notify.bean.NotifyJobType;
import com.clustercontrol.notify.bean.NotifyRequestMessage;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.entity.NotifyJobInfoData;
import com.clustercontrol.notify.model.NotifyJobInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobLinkExpInfoRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.RegistJobLinkMessageRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.RegistJobLinkMessageResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.PriorityRequiredEnum;
import com.clustercontrol.rest.proxy.JobRestEndpointsProxyService;
import com.clustercontrol.rest.util.RestCommonConverter;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * ジョブを呼出すクラス<BR>
 *
 * @version 3.0.0
 * @since 3.0.0
 */
public class RunJob implements DependDbNotifier {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( RunJob.class );

	/** 実行失敗通知用 */
	private NotifyJobInfoData m_jobInfo;

	/**
	 * ジョブ管理機能の指定されたジョブを呼出します。
	 * <p>含まれているログ出力情報を基にジョブを呼出します。<BR>
	 * ジョブの呼出に失敗した場合は、ログ出力情報の呼出失敗時の重要度で、監視管理機能のイベントへ通知します。
	 * 
	 * @param outputInfo　通知・抑制情報
	 * 
	 * @see com.clustercontrol.jobmanagement.bean.JobTriggerInfo
	 * @see com.clustercontrol.monitor.factory.OutputEventLog#insertEventLog(LogOutputInfo, int)
	 */
	@Override
	public synchronized void notify(NotifyRequestMessage message) {
		if(m_log.isDebugEnabled()){
			m_log.debug("notify() " + message);
		}

		exectuteJob(message.getOutputInfo(), message.getNotifyId(), null);
	}

	/**
	 * ジョブ管理機能の指定されたジョブを呼出します。
	 * <p>含まれているログ出力情報を基にジョブを呼出します。<BR>
	 * ジョブの呼出に失敗した場合は、ログ出力情報の呼出失敗時の重要度で、監視管理機能のイベントへ通知します。
	 * 
	 * @param outputInfo 通知・抑制情報
	 * @param notifyId 通知ID
	 * @param expList 拡張情報（ジョブ連携メッセージ送信で使用）
	 */
	public void exectuteJob(OutputBasicInfo outputInfo, String notifyId, List<JobLinkExpInfo> expList) {

		if(m_log.isDebugEnabled()){
			m_log.debug("notify() " + outputInfo);
		}

		/*
		 * 実行
		 */
		NotifyJobInfo jobInfo = null;
		Integer notifyJobType = null;
		String joblinkSendSettingId = null;
		Long joblinkSendRetryCount = 0L;

		try {
			if (notifyId != null && !notifyId.isEmpty()) {
				jobInfo = QueryUtil.getNotifyJobInfoPK(notifyId);
				notifyJobType = jobInfo.getNotifyJobType();
				if (notifyJobType == NotifyJobType.TYPE_JOB_LINK_SEND) {
					joblinkSendSettingId = jobInfo.getJoblinkSendSettingId();
					if (jobInfo.getRetryFlg()) {
						joblinkSendRetryCount = Long.valueOf(jobInfo.getRetryCount());
					}
				}
			} else {
				// INTERNALイベントの場合
				notifyJobType = NotifyJobType.TYPE_JOB_LINK_SEND;
				joblinkSendSettingId = HinemosPropertyCommon.internal_joblinkmes_forward_setting.getStringValue();
				joblinkSendRetryCount = HinemosPropertyCommon.internal_joblinkmes_forward_retry_count.getNumericValue();
			}

			if (notifyJobType == NotifyJobType.TYPE_DIRECT) {
				// 直接実行

				// 実行対象のジョブが存在するかのチェック(存在しない場合はInternalイベントを出力して終了)
				try{
					JobValidator.validateJobId(getJobunitId(jobInfo, outputInfo.getPriority()), getJobId(jobInfo, outputInfo.getPriority()),false);
				} catch (InvalidRole | InvalidSetting e) {
					// 参照権限がない場合
					// 実行対象のジョブが存在しない場合の処理
					int outputPriority = outputInfo.getPriority(); 
					int failurePriority = 0;
					if (outputPriority == PriorityConstant.TYPE_INFO) {
						failurePriority = jobInfo.getInfoJobFailurePriority();
					} else if (outputPriority == PriorityConstant.TYPE_WARNING) {
						failurePriority = jobInfo.getWarnJobFailurePriority();
					} else if (outputPriority == PriorityConstant.TYPE_CRITICAL) {
						failurePriority = jobInfo.getCriticalJobFailurePriority();
					} else if (outputPriority == PriorityConstant.TYPE_UNKNOWN) {
						failurePriority = jobInfo.getUnknownJobFailurePriority();
					} else {
						m_log.warn("unknown priority " + outputPriority);
					}
					
					String[] args = { notifyId, outputInfo.getMonitorId(), getJobunitId(jobInfo, outputInfo.getPriority()), getJobId(jobInfo, outputInfo.getPriority()) };
					AplLogger.put(InternalIdCommon.PLT_NTF_SYS_008, failurePriority, args, null);
					return;
				}

				// 通知設定が「固定スコープ」となっていた場合は、ジョブに渡すファシリティIDを変更する
				if(jobInfo.getJobExecFacilityFlg() == ExecFacilityConstant.TYPE_FIX) {
					outputInfo.setFacilityId(jobInfo.getJobExecFacility());
				}

				// ジョブの実行契機を作成
				JobTriggerInfo triggerInfo = new JobTriggerInfo();
				triggerInfo.setTrigger_type(JobTriggerTypeConstant.TYPE_MONITOR);
				triggerInfo.setTrigger_info(outputInfo.getMonitorId()+"-"+outputInfo.getPluginId()); // 「監視項目ID_プラグインID」形式で格納

				// ジョブ実行
				new JobControllerBean().runJob(
						getJobunitId(jobInfo, outputInfo.getPriority()),
						getJobId(jobInfo, outputInfo.getPriority()), outputInfo,
						triggerInfo);

			} else if (notifyJobType == NotifyJobType.TYPE_JOB_LINK_SEND) {
				// ジョブ連携メッセージ送信
				JobLinkSendSettingEntity settingEntity
					= new JobControllerBean().getJobLinkSendSetting(joblinkSendSettingId);
				// 対象ノードのファシリティID取得
				List<String> facilityIdList = new RepositoryControllerBean().getExecTargetFacilityIdList(
						settingEntity.getFacilityId(), settingEntity.getOwnerRoleId());
				if (facilityIdList != null) {
					// 外部へのジョブ連携メッセージ送信
					RegistJobLinkMessageResponse response = null;
					RegistJobLinkMessageRequest request = new RegistJobLinkMessageRequest();
					request.setJoblinkMessageId(outputInfo.getJoblinkMessageId());
					request.setSendDate(RestCommonConverter.convertHinemosTimeToDTString(JobLinkUtil.createSendDate()));
					request.setMonitorDetailId(outputInfo.getSubKey());
					request.setApplication(outputInfo.getApplication());
					if (outputInfo.getPriority() == PriorityConstant.TYPE_INFO) {
						request.setPriority(PriorityRequiredEnum.INFO);
					} else if (outputInfo.getPriority() == PriorityConstant.TYPE_WARNING) {
						request.setPriority(PriorityRequiredEnum.WARNING);
					} else if (outputInfo.getPriority() == PriorityConstant.TYPE_CRITICAL) {
						request.setPriority(PriorityRequiredEnum.CRITICAL);
					} else if (outputInfo.getPriority() == PriorityConstant.TYPE_UNKNOWN) {
						request.setPriority(PriorityRequiredEnum.UNKNOWN);
					}
					request.setMessage(JobLinkUtil.getMessageMaxString(
							HinemosMessage.replace(outputInfo.getMessage(), Locale.getDefault())));
					request.setMessageOrg(JobLinkUtil.getMessageOrgMaxString(
							HinemosMessage.replace(outputInfo.getMessageOrg(), Locale.getDefault())));
					request.setJobLinkExpInfoList(new ArrayList<>());
					if (expList != null && expList.size() > 0) {
						for (JobLinkExpInfo expInfo : expList) {
							JobLinkExpInfoRequest expRequest = new JobLinkExpInfoRequest();
							expRequest.setKey(expInfo.getKey());
							expRequest.setValue(expInfo.getValue());
							request.getJobLinkExpInfoList().add(expRequest);
						}
					}
					HashSet<String> failureFacilityIds = new HashSet<>(facilityIdList);
					for (int i = 0; i <= joblinkSendRetryCount; i++) {
						for (String nodeFacilityId : facilityIdList) {
							if (!failureFacilityIds.contains(nodeFacilityId)) {
								continue;
							}
							// 送信処理
							request.setJoblinkSendSettingId(joblinkSendSettingId);
							request.setFacilityId(nodeFacilityId);
							// 送信元IPアドレス
							request.setSourceIpAddressList(LocalInfoUtil.getInternalIpAddressList());
							ServiceLoader<JobRestEndpointsProxyService> serviceLoader 
								= ServiceLoader.load(JobRestEndpointsProxyService.class);
							response = serviceLoader.iterator().next().registJobLinkMessage(request);
							if (response.getResult()) {
								if (jobInfo == null) {
									// 何もしない
								} else if (jobInfo.getSuccessInternalFlg()) {
									// 送信成功（INTERNAL）
									SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
									sdf.setTimeZone(HinemosTime.getTimeZone());
									String sendDate = sdf.format(new Date(outputInfo.getGenerationDate()));
									String[] args = {jobInfo.getNotifyId(), outputInfo.getJoblinkMessageId(), nodeFacilityId, sendDate};
									AplLogger.put(InternalIdCommon.PLT_NTF_SYS_019, args);
								}
								if (settingEntity.getProcessMode() == ProcessingMethodConstant.TYPE_RETRY) {
									// 「いずれかのノード」の場合は処理終了
									failureFacilityIds.clear();
									break;
								} else {
									failureFacilityIds.remove(nodeFacilityId);
								}
							} else {
								if (response.getResultDetail() != null && !response.getResultDetail().isEmpty()) {
									m_log.warn(response.getResultDetail());
								}
							}
						}
						if (failureFacilityIds.size() == 0) {
							break;
						}
						if (i >= joblinkSendRetryCount) {
							break;
						}
						try {
							Thread.sleep(HinemosPropertyCommon.joblinkmes_transport_retry_interval.getIntegerValue());
						} catch (InterruptedException e) {
						}
					}
					if (jobInfo != null
						&& jobInfo.getFailureInternalFlg()
						&& failureFacilityIds.size() > 0) {
						for (String failureFacilityId : failureFacilityIds) {
							// 送信失敗（INTERNAL）
							String detail = "";
							if (response.getResultDetail() != null && !response.getResultDetail().isEmpty()) {
								detail = response.getResultDetail();
							}
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
							sdf.setTimeZone(HinemosTime.getTimeZone());
							String sendDate = sdf.format(new Date(outputInfo.getGenerationDate()));
							String[] args = {jobInfo.getNotifyId(), outputInfo.getJoblinkMessageId(), failureFacilityId, sendDate};
							AplLogger.put(InternalIdCommon.PLT_NTF_SYS_020, args, detail);
						}
					}
				}
			}
		}
		catch (NotifyNotFound | InvalidSetting | InvalidRole | JobSessionDuplicate
				| JobMasterNotFound | JobInfoNotFound | HinemosUnknown | FacilityNotFound | RuntimeException e) {
			if (e instanceof InvalidRole
					|| e instanceof RuntimeException) {
				m_log.warn("exectuteJob() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}
			if(jobInfo != null){
				m_jobInfo = new NotifyJobInfoData(
						jobInfo.getNotifyId(),
						outputInfo.getPriority(),
						getJobFailurePriority(jobInfo, outputInfo.getPriority()),
						getJobunitId(jobInfo, outputInfo.getPriority()),
						getJobId(jobInfo, outputInfo.getPriority()),
						getJobRun(jobInfo, outputInfo.getPriority()),
						jobInfo.getJobExecFacilityFlg(),
						jobInfo.getJobExecFacility());
			}
			//ジョブ失敗時の重要度を設定
			String args;
			Integer priority = null;
			if(m_jobInfo != null){
				priority = m_jobInfo.getJobFailurePriority();
				args = m_jobInfo.getJobId();
			} else {
				args = "unknown job id.";
			}
			AplLogger.put(InternalIdCommon.JOB_SYS_029, priority, new String[]{args}, e.getMessage() + " : " + m_jobInfo);

			// クリアする
			m_jobInfo = null;
		}
	}

	private Boolean getJobRun(NotifyJobInfo jobInfo, int priority) {
		switch (priority) {
		case PriorityConstant.TYPE_INFO:
			return jobInfo.getInfoValidFlg();
		case PriorityConstant.TYPE_WARNING:
			return jobInfo.getWarnValidFlg();
		case PriorityConstant.TYPE_CRITICAL:
			return jobInfo.getCriticalValidFlg();
		case PriorityConstant.TYPE_UNKNOWN:
			return jobInfo.getUnknownValidFlg();

		default:
			break;
		}
		return Boolean.FALSE;
	}

	private String getJobId(NotifyJobInfo jobInfo, int priority) {
		switch (priority) {
		case PriorityConstant.TYPE_INFO:
			return jobInfo.getInfoJobId();
		case PriorityConstant.TYPE_WARNING:
			return jobInfo.getWarnJobId();
		case PriorityConstant.TYPE_CRITICAL:
			return jobInfo.getCriticalJobId();
		case PriorityConstant.TYPE_UNKNOWN:
			return jobInfo.getUnknownJobId();

		default:
			break;
		}
		return null;
	}

	private String getJobunitId(NotifyJobInfo jobInfo, int priority) {
		switch (priority) {
		case PriorityConstant.TYPE_INFO:
			return jobInfo.getInfoJobunitId();
		case PriorityConstant.TYPE_WARNING:
			return jobInfo.getWarnJobunitId();
		case PriorityConstant.TYPE_CRITICAL:
			return jobInfo.getCriticalJobunitId();
		case PriorityConstant.TYPE_UNKNOWN:
			return jobInfo.getUnknownJobunitId();

		default:
			break;
		}
		return null;
	}

	private Integer getJobFailurePriority(NotifyJobInfo jobInfo,
			int priority) {
		switch (priority) {
		case PriorityConstant.TYPE_INFO:
			return jobInfo.getInfoJobFailurePriority();
		case PriorityConstant.TYPE_WARNING:
			return jobInfo.getWarnJobFailurePriority();
		case PriorityConstant.TYPE_CRITICAL:
			return jobInfo.getCriticalJobFailurePriority();
		case PriorityConstant.TYPE_UNKNOWN:
			return jobInfo.getUnknownJobFailurePriority();

		default:
			break;
		}
		return null;
	}
}
