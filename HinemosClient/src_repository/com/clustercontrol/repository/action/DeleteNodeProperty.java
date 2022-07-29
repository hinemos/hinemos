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
 * ノードを削除するクライアント側アクションクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class DeleteNodeProperty {

	/**
	 * 指定したノードを削除します。
	 *
	 * @param managerName マネージャ名
	 * @param facilityIdList
	 */
	public void delete(String managerName, List<String> facilityIdList) {

		try {
			RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(managerName);
			wrapper.deleteNode(String.join(",", facilityIdList));

			// リポジトリキャッシュの更新
			ClientSession.doCheck();

			// 成功報告ダイアログを生成
			String[] arg = {managerName};
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.repository.8", arg));

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
						Messages.getString("message.repository.28", args));
			} else {
				errMessage = ", " + HinemosMessage.replace(e.getMessage());
			}

			// 失敗報告ダイアログを生成
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.repository.9") + "(" + managerName + ") " + errMessage);
		}

		return;
	}
}
