/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
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
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * 構成情報収集設定を削除するクライアント側アクションクラス<BR>
 *
 * @version 6.2.0
 * @since 6.2.0
 */
public class DeleteNodeConfigSetting {

	/**
	 * 指定したノードを削除します。
	 *
	 * @param managerName マネージャ名
	 * @param settingIdList
	 */
	public void delete(String managerName, List<String> settingIdList) {

		try {
			RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(managerName);
			//wrapper.deleteNode(facilityIdList);
			wrapper.deleteNodeConfigSettingInfo(String.join(",", settingIdList));

			// リポジトリキャッシュの更新
			ClientSession.doCheck();

			// 成功報告ダイアログを生成
			String[] arg = {managerName};
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.repository.60", arg));

		} catch (Exception e) {
			String errMessage = "";
			if (e instanceof InvalidRole) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));

			} else {
				errMessage = ", " + HinemosMessage.replace(e.getMessage());
			}

			// 失敗報告ダイアログを生成
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.repository.61") + errMessage);
		}

		return;
	}
}
