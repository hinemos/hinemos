/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.restaccess.action;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.RestAccessInfoResponse;

import com.clustercontrol.common.util.CommonRestClientWrapper;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
/**
 * RESTアクセス情報情報を取得するクライアント側アクションクラス<BR>
 *
 */
public class GetRestAccessInfo {

	// ログ
	private static Log m_log = LogFactory.getLog( GetRestAccessInfo.class );

	/**
	 * RESTアクセス情報情報一覧を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @return RESTアクセス情報情報一覧
	 *
	 */
	public Map<String, List<RestAccessInfoResponse>> getRestAccessInfoList() {

		Map<String, List<RestAccessInfoResponse>> dispDataMap= new ConcurrentHashMap<>();
		List<RestAccessInfoResponse> records = null;
		for (String managerName : RestConnectManager.getActiveManagerSet()) {
			try {
				CommonRestClientWrapper wrapper = CommonRestClientWrapper.getWrapper(managerName);
				records = wrapper.getRestAccessInfoList(null);
				dispDataMap.put(managerName, records);
			} catch (InvalidRole e) {
				MessageDialog.openInformation(
						null, 
						Messages.getString("message"), 
						Messages.getString("message.accesscontrol.16"));
			} catch (Exception e) {
				m_log.warn("getRestAccessInfoList(), " + e.getMessage(), e);
				MessageDialog.openError(
						null, 
						Messages.getString("failed"), 
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}
		}
		return dispDataMap;
	}

	/**
	 * オーナーロールIDを条件としてRESTアクセス情報情報一覧を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName
	 * @param ownerRoleId
	 * @return RESTアクセス情報情報一覧
	 *
	 */
	public List<RestAccessInfoResponse> getRestAccessInfoListByOwnerRole(String managerName, String ownerRoleId) {

		List<RestAccessInfoResponse> records = null;
		try {
			CommonRestClientWrapper wrapper = CommonRestClientWrapper.getWrapper(managerName);
			records = wrapper.getRestAccessInfoList(ownerRoleId);
		} catch (InvalidRole e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			m_log.warn("getRestAccessInfoListByOwnerRole(), " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		return records;
	}
}
