/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.action;

import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.reporting.util.ReportingEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.reporting.HinemosUnknown_Exception;
import com.clustercontrol.ws.reporting.InvalidRole_Exception;
import com.clustercontrol.ws.reporting.ReportingInfo;

/**
 * 
 * レポーティング情報を修正するクライアント側アクションクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 * 
 */
public class ModifyReporting {

	/**
	 * レポーティング情報を修正します。
	 * 
	 * @param レポーティング情報
	 * @return 成功時 true 失敗時 false
	 */
	public boolean modify(String managerName, ReportingInfo info) {
		boolean ret = false;

		String[] args = { info.getReportScheduleId(), managerName };
		try {
			ReportingEndpointWrapper wrapper = ReportingEndpointWrapper.getWrapper(managerName);
			ret = wrapper.modifyReporting(info);

			MessageDialog.openInformation(null,
					Messages.getString("successful"),
					Messages.getString("message.reporting.3", args));

			ret = true;

		} catch (HinemosUnknown_Exception e) {
			String errMessage = HinemosMessage.replace(e.getMessage());
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.reporting.4", args)
							+ ", " + errMessage);
		} catch (Exception e) {
			String errMessage = "";
			if (e instanceof InvalidRole_Exception) {
				MessageDialog.openInformation(null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} else {
				errMessage = ", " + HinemosMessage.replace(e.getMessage());
			}

			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.reporting.4", args)
							+ errMessage);
		}

		return ret;
	}

}
