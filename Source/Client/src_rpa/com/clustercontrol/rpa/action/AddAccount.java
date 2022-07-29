/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.AddRpaManagementToolAccountRequest;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.RpaManagementToolAccountDuplicate;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * 
 * RPA管理ツールアカウント情報を登録するクライアント側アクションクラス
 * 
 */
public class AddAccount {

	/**
	 * RPA管理ツールアカウント情報を追加します。
	 * 
	 * @return 成功時 true 失敗時 false
	 */
	public boolean add(String managerName, AddRpaManagementToolAccountRequest request) {
		boolean ret = false;

		String[] args = { request.getRpaScopeId(), managerName };
		try {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(managerName);
			wrapper.addRpaManagementToolAccount(request);
			ret = true;
			
			MessageDialog.openInformation(null,
					Messages.getString("successful"),
					Messages.getString("message.rpa.account.create", args));
		} catch (RpaManagementToolAccountDuplicate e) {
			// タグIDが重複している場合、エラーダイアログを表示する
			MessageDialog.openInformation(null, 
					Messages.getString("message"), 
					Messages.getString("message.rpa.account.create.duplicate", args));
		} catch (InvalidRole e) {
			MessageDialog.openInformation(null,
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			String errMessage = HinemosMessage.replace(e.getMessage());
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.rpa.account.create.fail", args)
					 + errMessage);
		}

		return ret;
	}

}
