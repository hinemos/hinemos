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
import com.clustercontrol.ws.reporting.TemplateSetInfo;

/**
 * 
 * テンプレートセット情報を修正するクライアント側アクションクラス<BR>
 * 
 * @version 5.0.a
 * @since 5.0.a
 * 
 */
public class ModifyTemplateSet {

	/**
	 * テンプレートセット情報を修正します。
	 * 
	 * @param テンプレートセット情報
	 * @return 成功時 true 失敗時 false
	 */
	public boolean modify(String managerName, TemplateSetInfo info) {
		boolean ret = false;

		String[] args = { info.getTemplateSetId(), managerName };
		try {
			ReportingEndpointWrapper wrapper = ReportingEndpointWrapper.getWrapper(managerName);
			ret = wrapper.modifyTemplateSet(info);

			MessageDialog.openInformation(null,
					Messages.getString("successful"),
					Messages.getString("message.reporting.37", args));

			ret = true;

		} catch (HinemosUnknown_Exception e) {
			String errMessage = HinemosMessage.replace(e.getMessage());
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.reporting.38", args)
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
					Messages.getString("message.reporting.38", args)
							+ errMessage);
		}

		return ret;
	}

}
