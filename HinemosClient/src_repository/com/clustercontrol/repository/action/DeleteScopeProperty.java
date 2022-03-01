/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.action;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.UsedFacility;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * スコープを削除するクライアント側アクションクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class DeleteScopeProperty {

	/**
	 * 指定したスコープを削除します。
	 *
	 * @param facilityIdList
	 *            ファシリティID
	 * @return 削除に成功した場合、true
	 */
	public void delete(String managerName, List<String> facilityIdList) {

		try {
			RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(managerName);
			wrapper.deleteScope(String.join(",", facilityIdList));

			// リポジトリキャッシュの更新
			ClientSession.doCheck();

			// 成功報告ダイアログを生成
			Object[] arg = {managerName};
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.repository.16", arg));

		} catch (Exception e) {
			String errMessage = "";
			if (e instanceof InvalidRole) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.16") + "(" + managerName + ")");

			} else if (e instanceof UsedFacility) {
				// ファシリティが使用されている場合のエラーダイアログを表示する
				Object[] args ={facilityIdList + "(" + managerName + ")",
						HinemosMessage.replace(((UsedFacility)e).getMessage())};
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.repository.29", args));
			} else {
				errMessage = ", " + HinemosMessage.replace(e.getMessage());
			}

			// 失敗報告ダイアログを生成
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.repository.17") + "(" + managerName + ") " + errMessage);
		}

		return;
	}
}
