/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.restaccess.action;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.common.util.CommonRestClientWrapper;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * RESTアクセス情報を削除するクライアント側アクションクラス<BR>
 *
 */
public class DeleteRestAccessInfo {

	// ログ
	private static Log m_log = LogFactory.getLog(DeleteRestAccessInfo.class);

	/**
	 * RESTアクセス情報を削除します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName
	 *            マネージャ名
	 * @param RestAccessInfoId
	 *            削除対象のRESTアクセス情報ID
	 * @return 削除に成功した場合、<code> true </code>
	 *
	 */
	public boolean delete(String managerName, List<String> RestAccessInfoIdList) {

		boolean result = false;
		String[] args = { String.join(",", RestAccessInfoIdList), managerName };

		if (RestAccessInfoIdList.isEmpty()) {
			return result;
		}

		try {
			CommonRestClientWrapper wrapper = CommonRestClientWrapper.getWrapper(managerName);
			wrapper.deleteRestAccessInfo(String.join(",", RestAccessInfoIdList));
			result = true;
			MessageDialog.openInformation(null, Messages.getString("successful"),
					Messages.getString("message.restaccess.5", args));

		} catch (InvalidRole e) {
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.restaccess.11", args));

		} catch (Exception e) {
			m_log.warn("delete(), " + e.getMessage());
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.restaccess.6", args) + " " + HinemosMessage.replace(e.getMessage()));
		}

		return result;
	}
}
