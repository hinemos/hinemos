/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.CommandTemplateResponse;

import com.clustercontrol.common.util.CommonRestClientWrapper;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;

/**
 * コマンド通知テンプレート情報を取得するクライアント側アクションクラス<BR>
 */
public class GetCommandTemplate {
	// ログ
	private static Log m_log = LogFactory.getLog( GetCommandTemplate.class );

	/**
	 * コマンド通知テンプレート情報を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param manegerName マネージャ名
	 * @param commandTemplateId 取得対象のコマンド通知テンプレートID
	 * @return コマンド通知テンプレート情報
	 *
	 */
	public CommandTemplateResponse getCommandTemplate(String managerName, String commandTemplateId) {
		m_log.debug("getCommandTemplate(), " + commandTemplateId);
		CommandTemplateResponse info = null;

		try {
			CommonRestClientWrapper wrapper = CommonRestClientWrapper.getWrapper(managerName);
			info = wrapper.getCommandTemplate(commandTemplateId);
		} catch (InvalidRole e) {
			MessageDialog.openInformation(
					null, 
					Messages.getString("message"), 
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			m_log.warn("getCommandTemplate(), " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openError(
					null, 
					Messages.getString("error"), 
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
		return info;
	}

	/**
	 * コマンド通知テンプレート情報を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param manegerName マネージャ名
	 * @return コマンド通知テンプレート情報
	 *
	 */
	public List<CommandTemplateResponse> getCommandTemplateList(String managerName) {
		m_log.debug("getCommandTemplateList(), " + managerName);
		List<CommandTemplateResponse> info = new ArrayList<>();

		try {
			CommonRestClientWrapper wrapper = CommonRestClientWrapper.getWrapper(managerName);
			info = wrapper.getCommandTemplateList();
		} catch (Exception e) {
			m_log.warn("getCommandTemplateList(), " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openError(
					null, 
					Messages.getString("error"), 
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));

		}
		return info;
	}

	/**
	 * オーナーロールIDを条件としてコマンド通知テンプレート情報一覧を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @return コマンド通知テンプレート情報一覧
	 */
	public List<CommandTemplateResponse> getCommandTemplateListByOwnerRole(String managerName, String ownerRoleId) {
		m_log.debug("getCommandTemplateListByOwnerRole(), " + ownerRoleId);
		List<CommandTemplateResponse> records = new ArrayList<>();
		Map<String, String> errMsgs = new ConcurrentHashMap<>();
		try {
			CommonRestClientWrapper wrapper = CommonRestClientWrapper.getWrapper(managerName);
			records = wrapper.getCommandTemplateList(ownerRoleId);
		} catch (InvalidRole e) {
			errMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
		} catch (Exception e) {
			m_log.warn("getCommandTemplateListByOwnerRole(), " + HinemosMessage.replace(e.getMessage()), e);
			errMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		//メッセージ表示
		if( 0 < errMsgs.size() ){
			UIManager.showMessageBox(errMsgs, true);
		}
		return records;
	}
}
