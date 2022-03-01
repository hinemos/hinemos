/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.JobFileCheckResponse;
import org.openapitools.client.model.JobKickResponse;
import org.openapitools.client.model.JobLinkRcvResponse;
import org.openapitools.client.model.JobManualResponse;
import org.openapitools.client.model.JobScheduleResponse;

import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * ジョブ[実行契機]情報を取得するクライアント側アクションクラス<BR>
 *
 * @version 4.1.0
 * @since 4.1.0
 */
public class GetJobKick {

	// ログ
	private static Log m_log = LogFactory.getLog( GetJobKick.class );

	/**
	 * 実行契機[スケジュール]情報を返します。<BR>
	 *
	 * @param managerName マネージャ名
	 * @param jobkickId 実行契機ID
	 * @return 実行契機[スケジュール]情報
	 */
	public static JobScheduleResponse getJobSchedule(String managerName, String jobkickId) {
		JobScheduleResponse jobSchedule = null;
		try {
			JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
			jobSchedule = wrapper.getJobSchedule(jobkickId);
		} catch (Exception e) {
			// 上記以外の例外
			m_log.warn("getJobSchedule(), " + e.getMessage(), e);
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
		return jobSchedule ;
	}

	/**
	 * 実行契機[ファイルチェック]情報を返します。<BR>
	 *
	 * @param jobkickId 実行契機ID
	 * @return 実行契機[ファイルチェック]情報
	 */
	public static JobFileCheckResponse getJobFileCheck(String managerName, String jobkickId) {
		JobFileCheckResponse jobFileCheck = null;
		try {
			JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
			jobFileCheck = wrapper.getFileCheck(jobkickId);
		} catch (Exception e) {
			// 上記以外の例外
			m_log.warn("getJobFileCheck(), " + e.getMessage(), e);
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
		return jobFileCheck ;
	}

	/**
	 * 実行契機[ジョブ連携受信実行契機]情報を返します。<BR>
	 *
	 * @param jobkickId 実行契機ID
	 * @return 実行契機[ジョブ連携受信実行契機]情報
	 */
	public static JobLinkRcvResponse getJobLinkRcv(String managerName, String jobkickId) {
		JobLinkRcvResponse jobLinkRcv = null;
		try {
			JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
			jobLinkRcv = wrapper.getJobLinkRcv(jobkickId);
		} catch (Exception e) {
			// 上記以外の例外
			m_log.warn("getJobLinkRcv(), " + e.getMessage(), e);
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
		return jobLinkRcv ;
	}

	/**
	 * 実行契機[マニュアル実行契機]情報を返します。<BR>
	 *
	 * @param jobkickId 実行契機ID
	 * @return 実行契機[マニュアル実行契機]情報
	 */
	public static JobManualResponse getJobManual(String managerName, String jobkickId) {
		JobManualResponse jobKick = null;
		try {
			JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
			jobKick = wrapper.getJobManual(jobkickId);
		} catch (Exception e) {
			// 上記以外の例外
			m_log.warn("getJobManual(), " + e.getMessage(), e);
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
		return jobKick ;
	}

	/**
	 * 実行契機情報を返します。<BR>
	 *
	 * @param jobkickId 実行契機ID
	 * @return 実行契機情報
	 */
	public static JobKickResponse getJobKick(String managerName, String jobkickId) {
		JobKickResponse jobKick = null;
		try {
			JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
			jobKick = wrapper.getJobKick(jobkickId);
		} catch (Exception e) {
			// 上記以外の例外
			m_log.warn("getJobKick(), " + e.getMessage(), e);
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
		return jobKick ;
	}
}
