/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.reporting.util.ReportingEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.reporting.InvalidRole_Exception;
import com.clustercontrol.ws.reporting.ReportingInfo;

/**
 * レポーティング情報を取得するクライアント側アクションクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class GetReporting {

	// ログ
	private static Log m_log = LogFactory.getLog(GetReporting.class);

	/**
	 * マネージャにアクセスしレポーティング情報を返します。
	 * 
	 * @param レポートID
	 * @return レポーティング情報
	 */
	public ReportingInfo getReportingInfo(String managerName, String reportId) {

		ReportingInfo info = null;
		try {
			ReportingEndpointWrapper wrapper = ReportingEndpointWrapper.getWrapper(managerName);
			info = wrapper.getReportingInfo(reportId);
		} catch (InvalidRole_Exception e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			String errMessage = HinemosMessage.replace(e.getMessage());
			m_log.warn("getReportingInfo(), " + errMessage, e);
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected")
							+ ", " + errMessage);
		}
		return info;
	}
}