/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.monitor.run.bean.MonitorNumericType;
import com.clustercontrol.notify.bean.NotifyTriggerType;

/**
 * ジョブ連携メッセージIDを作成するクラスです。
 *
 */
public class JobLinkMessageId {

	/**
	 * 
	 * ジョブ連携メッセージID取得（ジョブ以外）
	 * 
	 * @param notifyTriggerType
	 *            通知契機情報
	 * @param pluginId
	 *            プラグインID
	 * @param id
	 *            ID
	 * @return ジョブ連携メッセージID
	 */
	public static String getId(NotifyTriggerType notifyTriggerType, String pluginId, String id) {
		if (notifyTriggerType == NotifyTriggerType.MONITOR_CHANGE) {
			return String.format("%s_%s_%s", pluginId, MonitorNumericType.TYPE_CHANGE.getType(), id);

		} else if (notifyTriggerType == NotifyTriggerType.MONITOR_PREDICTION) {
			return String.format("%s_%s_%s", pluginId, MonitorNumericType.TYPE_PREDICTION.getType(), id);

		} else if (notifyTriggerType == NotifyTriggerType.INFRA_RUN_START) {
			return String.format("%s_%s_%s", pluginId, HinemosModuleConstant.INFRA_RUN_START, id);

		} else if (notifyTriggerType == NotifyTriggerType.INFRA_RUN_END) {
			return String.format("%s_%s_%s", pluginId, HinemosModuleConstant.INFRA_RUN_END, id);

		} else if (notifyTriggerType == NotifyTriggerType.INFRA_CHECK_START) {
			return String.format("%s_%s_%s", pluginId, HinemosModuleConstant.INFRA_CHECK_START, id);

		} else if (notifyTriggerType == NotifyTriggerType.INFRA_CHECK_END) {
			return String.format("%s_%s_%s", pluginId, HinemosModuleConstant.INFRA_CHECK_END, id);

		} else {
			return String.format("%s_%s", pluginId, id);
		}
	}

	/**
	 * ジョブ連携メッセージID取得（ジョブ連携送信ジョブ）
	 * 
	 * @param name
	 *            任意情報
	 * @return ジョブ連携メッセージID
	 */
	public static String getIdForJobLinkSendJob(String name) {
		return String.format("%s_%s", HinemosModuleConstant.JOB, name);
	}

	/**
	 * 
	 * ジョブ連携メッセージID取得（ジョブ）
	 * 
	 * @param notifyTriggerType
	 *            通知契機情報
	 * @param jobunitId
	 *            ジョブユニットID
	 * @param jobId
	 *            ジョブID
	 * @return ジョブ連携メッセージID
	 */
	public static String getIdForJob(NotifyTriggerType notifyTriggerType, String jobunitId, String jobId) {
		if (notifyTriggerType == NotifyTriggerType.JOB_START) {
			return String.format("%s_%s:%s", HinemosModuleConstant.JOB_START, jobunitId, jobId);

		} else if (notifyTriggerType == NotifyTriggerType.JOB_END) {
			return String.format("%s_%s:%s", HinemosModuleConstant.JOB_END, jobunitId, jobId);

		} else if (notifyTriggerType == NotifyTriggerType.JOB_START_DELAY) {
			return String.format("%s_%s:%s", HinemosModuleConstant.JOB_START_DELAY, jobunitId, jobId);

		} else if (notifyTriggerType == NotifyTriggerType.JOB_END_DELAY) {
			return String.format("%s_%s:%s", HinemosModuleConstant.JOB_END_DELAY, jobunitId, jobId);

		} else if (notifyTriggerType == NotifyTriggerType.JOB_QUEUE_START) {
			return String.format("%s_%s:%s", HinemosModuleConstant.JOB_QUEUE_START, jobunitId, jobId);

		} else if (notifyTriggerType == NotifyTriggerType.JOB_QUEUE_END) {
			return String.format("%s_%s:%s", HinemosModuleConstant.JOB_QUEUE_END, jobunitId, jobId);

		} else if (notifyTriggerType == NotifyTriggerType.JOB_EXCEEDED_MULTIPLICITY) {
			return String.format("%s_%s:%s", HinemosModuleConstant.JOB_EXCEEDED_MULTIPLICITY, jobunitId, jobId);

		} else {
			return null;
		}
	}

	/**
	 * 
	 * ジョブ連携メッセージID取得（INTERNALイベント）
	 * 
	 * @return ジョブ連携メッセージID
	 */
	public static String getIdForInternal() {
		return "INTERNAL";
	}
}
