/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.RpaManagementToolAccountResponse;

import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * RPA管理ツールアカウント情報を取得するクライアント側アクションクラス
 */
public class GetAccount {

	// ログ
	private static Log m_log = LogFactory.getLog( GetAccount.class );

	/**
	 * RPA管理ツールアカウント情報を返します。
	 *
	 * @param managerName マネージャ名
	 * @param rpaScopeId RPAスコープID
	 * @return RPA管理ツールアカウント情報
	 */
	public RpaManagementToolAccountResponse getRpaManagementToolAccount(String managerName, String rpaScopeId) {

		RpaManagementToolAccountResponse info = null;
		
		m_log.info("rpaScopeId = " + rpaScopeId);
		
		try {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(managerName);
			info = wrapper.getRpaManagementToolAccount(rpaScopeId);
		} catch (Exception e) {
			String errMessage = HinemosMessage.replace(e.getMessage());
			m_log.warn("getRpaManagementToolAccount(), " + errMessage, e);
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + errMessage);
		}

		return info;
	}
}
