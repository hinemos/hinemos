/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.ModifyRpaManagementToolAccountRequest;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * 
 * RPA管理ツールアカウント情報を変更するクライアント側アクションクラス
 * 
 */
public class ModifyAccount {

	/**
	 * RPA管理ツールアカウント情報を変更します。
	 * 
	 * @return 成功時 true 失敗時 false
	 */
	public boolean modify(String managerName, String rpaScopeId, ModifyRpaManagementToolAccountRequest request) {
		boolean ret = false;

		String[] args = { rpaScopeId, managerName };
		try {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(managerName);
			wrapper.modifyRpaManagementToolAccount(rpaScopeId, request);

			MessageDialog.openInformation(null,
					Messages.getString("successful"),
					Messages.getString("message.rpa.account.modify", args));

			ret = true;
		} catch (InvalidRole e) {
			MessageDialog.openInformation(null,
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			String errMessage = HinemosMessage.replace(e.getMessage());
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.rpa.account.modify.fail", args)
							+ ", " + errMessage);
		}
		return ret;
	}

}
