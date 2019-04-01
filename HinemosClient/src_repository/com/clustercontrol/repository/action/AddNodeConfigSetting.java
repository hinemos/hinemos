/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.action;

import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.repository.util.RepositoryEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.notify.InvalidRole_Exception;
import com.clustercontrol.ws.repository.NodeConfigSettingDuplicate_Exception;
import com.clustercontrol.ws.repository.NodeConfigSettingInfo;

/**
 * 構成情報収集設定を作成するクライアント側アクションクラス<BR>
 *
 * @version 6.2.0
 * @since 6.2.0
 */
public class AddNodeConfigSetting {

	/**
	 * 構成情報収集設定を作成します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName マネージャ名
	 * @param info 作成対象の構成情報収集設定
	 *
	 */
	public void add(String managerName, NodeConfigSettingInfo info) {

		String[] args = { info.getSettingId(), managerName };
		try {
			RepositoryEndpointWrapper wrapper = RepositoryEndpointWrapper.getWrapper(managerName);
			wrapper.addNodeConfigSetting(info);

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.repository.57", args));

		} catch (NodeConfigSettingDuplicate_Exception e) {
			// 構成情報収集設定IDが重複している場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.repository.55", args));

		} catch (Exception e) {
			String errMessage = "";
			if (e instanceof InvalidRole_Exception) {
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} else {
				errMessage = ", " + HinemosMessage.replace(e.getMessage());
			}

			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.repository.58", args) + errMessage);
		}
	}
}
