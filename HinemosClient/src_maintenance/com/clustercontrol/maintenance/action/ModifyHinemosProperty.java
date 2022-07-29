/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.HinemosPropertyResponse;
import org.openapitools.client.model.ModifyHinemosPropertyRequest;

import com.clustercontrol.common.util.CommonRestClientWrapper;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.maintenance.util.HinemosPropertyBeanUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

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
	 * 
	 * @param managerName
	 *            マネージャ名
	 * @param info
	 *            更新する共通設定情報
	 * @return 処理成否
	 */
	public boolean modify(String managerName, String key, HinemosPropertyResponse info) {
		boolean ret = false;

		String[] args = { key, managerName };
		try {
			CommonRestClientWrapper wrapper = CommonRestClientWrapper.getWrapper(managerName);
			ModifyHinemosPropertyRequest request = HinemosPropertyBeanUtil.convertToModifyHinemosPropertyRequest(info);
			wrapper.modifyHinemosProperty(key, request);

			MessageDialog.openInformation(null, Messages.getString("successful"),
					Messages.getString("message.hinemos.property.4", args));

			ret = true;

		} catch (HinemosUnknown e) {
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.hinemos.property.5", args) + ", "
							+ HinemosMessage.replace(e.getMessage()));
		} catch (Exception e) {
			String errMessage = "";
			if (e instanceof InvalidRole) {
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} else {
				errMessage = ", " + HinemosMessage.replace(e.getMessage());
			}

			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.hinemos.property.5", args) + errMessage);
		}

		return ret;
	}

}
