/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.action;

import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.maintenance.util.HinemosPropertyEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.maintenance.HinemosPropertyInfo;
import com.clustercontrol.ws.maintenance.HinemosUnknown_Exception;
import com.clustercontrol.ws.maintenance.InvalidRole_Exception;

/**
 *
 * 共通設定情報を修正するクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 *
 */
public class ModifyHinemosProperty {

	/**
	 * 指定された共通設定情報でDB更新を行います。
	 * @param managerName マネージャ名
	 * @param info 更新する共通設定情報
	 * @return 処理成否
	 */
	public boolean modify(String managerName, HinemosPropertyInfo info) {
		boolean ret = false;

		String[] args = { info.getKey(), managerName };
		try {
			HinemosPropertyEndpointWrapper wrapper = HinemosPropertyEndpointWrapper.getWrapper(managerName);
			wrapper.modifyHinemosProperty(info);

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.hinemos.property.4", args));

			ret = true;

		} catch (HinemosUnknown_Exception e) {
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.property.5", args) + ", " + HinemosMessage.replace(e.getMessage()));
		} catch (Exception e) {
			String errMessage = "";
			if (e instanceof InvalidRole_Exception) {
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} else {
				errMessage = ", " + HinemosMessage.replace(e.getMessage());
			}

			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.property.5", args) + errMessage);
		}

		return ret;
	}

}
