/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.action;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.sdml.util.SdmlRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * SDML制御設定情報を削除するクライアント側アクションクラス
 *
 */
public class DeleteSdmlControlSetting {
	private static Log logger = LogFactory.getLog(DeleteSdmlControlSetting.class);

	/**
	 * 指定したアプリケーションIDのSDML制御設定を削除します
	 * 
	 * @param managerName
	 * @param applicationIdList
	 */
	public void delete(String managerName, List<String> applicationIdList) {
		if (applicationIdList == null || applicationIdList.isEmpty()) {
			return;
		}

		String[] args = null;
		String msg = null;
		if (applicationIdList.size() == 1) {
			args = new String[] { applicationIdList.get(0), Messages.getString("delete"), managerName };
			msg = "message.sdml.control.action.finished";
		} else {
			args = new String[] { Integer.toString(applicationIdList.size()), Messages.getString("delete") };
			msg = "message.sdml.control.count.finished";
		}
		try {
			SdmlRestClientWrapper wrapper = SdmlRestClientWrapper.getWrapper(managerName);
			wrapper.deleteSdmlControlSettingV1(String.join(",", applicationIdList));

			// 成功
			MessageDialog.openInformation(null, Messages.getString("successful"), Messages.getString(msg, args));

		} catch (InvalidRole e) {
			// アクセス権なし
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16") + "(" + managerName + ")");

		} catch (Exception e) {
			String errMessage = HinemosMessage.replace(e.getMessage());
			logger.warn("delete(), " + errMessage, e);
			args = new String[] { String.join(",", applicationIdList), 
					Messages.getString("delete"), 
					"(" + managerName + ") " + errMessage };
			// 失敗
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.sdml.control.action.failed", args));
		}
	}
}
