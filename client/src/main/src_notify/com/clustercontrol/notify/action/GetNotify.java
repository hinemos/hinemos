/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.action;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.notify.util.NotifyEndpointWrapper;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.ws.notify.InvalidRole_Exception;
import com.clustercontrol.ws.notify.NotifyInfo;

/**
 * 通知情報を取得するクライアント側アクションクラス<BR>
 *
 * @version 2.2.0
 * @since 1.0.0
 */
public class GetNotify {

	// ログ
	private static Log m_log = LogFactory.getLog( GetNotify.class );

	/**
	 * 通知情報を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param manegerName マネージャ名
	 * @param notifyId 取得対象の通知ID
	 * @return 通知情報
	 *
	 */
	public NotifyInfo getNotify(String managerName, String notifyId) {

		NotifyInfo info = null;

		try {
			NotifyEndpointWrapper wrapper = NotifyEndpointWrapper.getWrapper(managerName);
			info = wrapper.getNotify(notifyId);
		} catch (InvalidRole_Exception e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			m_log.warn("useCheck(), " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
		return info;
	}

	/**
	 * 通知情報一覧を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @return 通知情報一覧
	 */
	private Map<String, List<NotifyInfo>> getNotifyList(){

		Map<String, List<NotifyInfo>> dispDataMap= new ConcurrentHashMap<>();
		List<NotifyInfo> records = null;
		for (String managerName : EndpointManager.getActiveManagerSet()) {
			try {
				NotifyEndpointWrapper wrapper = NotifyEndpointWrapper.getWrapper(managerName);
				records = wrapper.getNotifyList();
				dispDataMap.put(managerName, records);
			} catch (InvalidRole_Exception e) {
				MessageDialog.openInformation(
						null, 
						Messages.getString("message"), 
						Messages.getString("message.accesscontrol.16"));
			} catch (Exception e) {
				m_log.warn("getNotifyList(), " + HinemosMessage.replace(e.getMessage()), e);
				MessageDialog.openError(
						null, 
						Messages.getString("error"), 
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
				
			}
		}
		return dispDataMap;
	}

	/**
	 * オーナーロールIDを条件として通知情報一覧を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @return 通知情報一覧
	 */
	private Map<String, List<NotifyInfo>> getNotifyListByOwnerRole(String ownerRoleId) throws InvalidRole_Exception{

		Map<String, List<NotifyInfo>> dispDataMap= new ConcurrentHashMap<>();
		List<NotifyInfo> records = null;
		Map<String, String> errMsgs = new ConcurrentHashMap<>();
		for (String managerName : EndpointManager.getActiveManagerSet()) {
			try {
				NotifyEndpointWrapper wrapper = NotifyEndpointWrapper.getWrapper(managerName);
				records = wrapper.getNotifyListByOwnerRole(ownerRoleId);
				dispDataMap.put(managerName, records);
			} catch (InvalidRole_Exception e) {
				errMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
			} catch (Exception e) {
				m_log.warn("getNotifyListByOwnerRole(), " + HinemosMessage.replace(e.getMessage()), e);
				errMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}
		}

		//メッセージ表示
		if( 0 < errMsgs.size() ){
			UIManager.showMessageBox(errMsgs, true);
		}
		return dispDataMap;
	}

	/**
	 * 通知情報一覧を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param マネージャ名
	 * @return 通知情報一覧
	 */
	public Map<String, List<NotifyInfo>> getNotifyList(String managerName){

		if(managerName == null) {
			return getNotifyList();
		}

		Map<String, List<NotifyInfo>> dispDataMap= new ConcurrentHashMap<>();
		List<NotifyInfo> records = null;
		Map<String, String> errMsgs = new ConcurrentHashMap<>();

		try {
			NotifyEndpointWrapper wrapper = NotifyEndpointWrapper.getWrapper(managerName);
			records = wrapper.getNotifyList();
			dispDataMap.put(managerName, records);
		} catch (InvalidRole_Exception e) {
			errMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
		} catch (Exception e) {
			m_log.warn("getNotifyList(), " + HinemosMessage.replace(e.getMessage()), e);
			errMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		//メッセージ表示
		if( 0 < errMsgs.size() ){
			UIManager.showMessageBox(errMsgs, true);
		}
		return dispDataMap;
	}

	/**
	 * オーナーロールIDを条件として通知情報一覧を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param マネージャ名
	 * @return 通知情報一覧
	 */
	public Map<String, List<NotifyInfo>> getNotifyListByOwnerRole(String managerName, String ownerRoleId) throws InvalidRole_Exception{

		if(managerName == null) {
			return getNotifyListByOwnerRole(ownerRoleId);
		}

		Map<String, List<NotifyInfo>> dispDataMap= new ConcurrentHashMap<>();
		List<NotifyInfo> records = null;
		Map<String, String> errMsgs = new ConcurrentHashMap<>();

		try {
			NotifyEndpointWrapper wrapper = NotifyEndpointWrapper.getWrapper(managerName);
			records = wrapper.getNotifyListByOwnerRole(ownerRoleId);
			dispDataMap.put(managerName, records);
		} catch (InvalidRole_Exception e) {
			errMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
		} catch (Exception e) {
			m_log.warn("getNotifyListByOwnerRole(), " + HinemosMessage.replace(e.getMessage()), e);
			errMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		//メッセージ表示
		if( 0 < errMsgs.size() ){
			UIManager.showMessageBox(errMsgs, false);
		}
		return dispDataMap;
	}
}
