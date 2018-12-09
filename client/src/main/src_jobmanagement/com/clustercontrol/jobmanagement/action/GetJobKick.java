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

import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.jobmanagement.JobFileCheck;
import com.clustercontrol.ws.jobmanagement.JobKick;
import com.clustercontrol.ws.jobmanagement.JobSchedule;

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
	public static JobSchedule getJobSchedule(String managerName, String jobkickId) {
		JobSchedule jobSchedule  = null;
		try {
			JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(managerName);
			jobSchedule  = wrapper.getJobSchedule(jobkickId);
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
	public static JobFileCheck getJobFileCheck(String managerName, String jobkickId) {
		JobFileCheck jobFileCheck = null;
		try {
			JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(managerName);
			jobFileCheck = wrapper.getJobFileCheck(jobkickId);
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
	 * 実行契機[マニュアル実行契機]情報を返します。<BR>
	 *
	 * @param jobkickId 実行契機ID
	 * @return 実行契機[マニュアル実行契機]情報
	 */
	public static JobKick getJobManual(String managerName, String jobkickId) {
		JobKick jobKick = null;
		try {
			JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(managerName);
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
	public static JobKick getJobKick(String managerName, String jobkickId) {
		JobKick jobKick = null;
		try {
			JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(managerName);
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
