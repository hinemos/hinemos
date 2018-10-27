/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.action;

import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.notify.util.NotifyEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.notify.InvalidRole_Exception;
import com.clustercontrol.ws.notify.NotifyDuplicate_Exception;
import com.clustercontrol.ws.notify.NotifyInfo;

/**
 * 通知情報を作成するクライアント側アクションクラス<BR>
 *
 * @version 2.2.0
 * @since 1.0.0
 */
public class AddNotify {

	/**
	 * 通知情報を作成します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName マネージャ名
	 * @param info 作成対象の通知情報
	 * @return 作成に成功した場合、<code> true </code>
	 *
	 */
	public boolean add(String managerName, NotifyInfo info) {

		boolean result = false;
		String[] args = { info.getNotifyId(), managerName };
		try {
			NotifyEndpointWrapper wrapper = NotifyEndpointWrapper.getWrapper(managerName);
			result = wrapper.addNotify(info);

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.notify.1", args));

		} catch (NotifyDuplicate_Exception e) {
			// 通知IDが重複している場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.notify.19", args));

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
					Messages.getString("message.notify.2", args) + errMessage);
		}

		return result;
	}
}
