/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.jobmanagement.bean.JobTriggerInfo;
import com.clustercontrol.jobmanagement.bean.JobTriggerTypeConstant;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.jobmanagement.util.JobValidator;
import com.clustercontrol.notify.bean.ExecFacilityConstant;
import com.clustercontrol.notify.bean.NotifyRequestMessage;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.entity.NotifyJobInfoData;
import com.clustercontrol.notify.model.NotifyJobInfo;
import com.clustercontrol.util.MessageConstant;
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

		exectuteJob(message.getOutputInfo(), message.getNotifyId());
	}

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
	private void exectuteJob(
			OutputBasicInfo outputInfo,
			String notifyId) {

		if(m_log.isDebugEnabled()){
			m_log.debug("notify() " + outputInfo);
		}

		/*
		 * 実行
		 */
		NotifyJobInfo jobInfo = null;
		try {
			jobInfo = QueryUtil.getNotifyJobInfoPK(notifyId);

			
			
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
				AplLogger.put(failurePriority, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_008_NOTIFY, args, null);
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
		}
		catch (Exception e) {
			if (!(e instanceof InvalidSetting
					|| e instanceof FacilityNotFound
					|| e instanceof HinemosUnknown
					|| e instanceof JobInfoNotFound
					|| e instanceof JobMasterNotFound)) {
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
			internalErrorNotify(-1, notifyId, null, e.getMessage() + " : " + m_jobInfo);
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

	/**
	 * 通知失敗時の内部エラー通知を定義します
	 */
	@Override
	public void internalErrorNotify(int priority, String notifyId, MessageConstant msgCode, String detailMsg) {
		//ジョブ失敗時の重要度を設定
		String args;
		if(m_jobInfo != null){
			priority = m_jobInfo.getJobFailurePriority();
			args = m_jobInfo.getJobId();
		} else {
			priority = PriorityConstant.TYPE_CRITICAL;
			args = "unknown job id.";
		}
		AplLogger.put(priority, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_FAILED_TO_START_JOB, new Object[]{args}, detailMsg);

		m_jobInfo = null;
	}
}

