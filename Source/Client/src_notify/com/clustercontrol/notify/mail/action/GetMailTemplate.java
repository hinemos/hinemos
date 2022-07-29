/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.mail.action;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.MailTemplateInfoResponse;

import com.clustercontrol.common.util.CommonRestClientWrapper;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
/**
 * メールテンプレート情報を取得するクライアント側アクションクラス<BR>
 *
 * @version 2.4.0
 * @since 2.4.0
 */
public class GetMailTemplate {

	// ログ
	private static Log m_log = LogFactory.getLog( GetMailTemplate.class );

	/**
	 * メールテンプレート情報一覧を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @return メールテンプレート情報一覧
	 *
	 */
	public Map<String, List<MailTemplateInfoResponse>> getMailTemplateList() {

		Map<String, List<MailTemplateInfoResponse>> dispDataMap= new ConcurrentHashMap<>();
		List<MailTemplateInfoResponse> records = null;
		for (String managerName : RestConnectManager.getActiveManagerSet()) {
			try {
				CommonRestClientWrapper wrapper = CommonRestClientWrapper.getWrapper(managerName);
				records = wrapper.getMailTemplateList(null);
				dispDataMap.put(managerName, records);
			} catch (InvalidRole e) {
				MessageDialog.openInformation(
						null, 
						Messages.getString("message"), 
						Messages.getString("message.accesscontrol.16"));
			} catch (Exception e) {
				m_log.warn("getMailTemplateList(), " + e.getMessage(), e);
				MessageDialog.openError(
						null, 
						Messages.getString("failed"), 
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}
		}
		return dispDataMap;
	}

	/**
	 * オーナーロールIDを条件としてメールテンプレート情報一覧を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName
	 * @param ownerRoleId
	 * @return メールテンプレート情報一覧
	 *
	 */
	public List<MailTemplateInfoResponse> getMailTemplateListByOwnerRole(String managerName, String ownerRoleId) {

		List<MailTemplateInfoResponse> records = null;
		try {
			CommonRestClientWrapper wrapper = CommonRestClientWrapper.getWrapper(managerName);
			records = wrapper.getMailTemplateList(ownerRoleId);
		} catch (InvalidRole e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			m_log.warn("getMailTemplateListByOwnerRole(), " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		return records;
	}
}
