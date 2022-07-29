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
import org.openapitools.client.model.CloudNotifyInfoResponse;
import org.openapitools.client.model.CommandNotifyInfoResponse;
import org.openapitools.client.model.EventNotifyInfoResponse;
import org.openapitools.client.model.InfraNotifyInfoResponse;
import org.openapitools.client.model.JobNotifyInfoResponse;
import org.openapitools.client.model.LogEscalateNotifyInfoResponse;
import org.openapitools.client.model.MailNotifyInfoResponse;
import org.openapitools.client.model.MessageNotifyInfoResponse;
import org.openapitools.client.model.NotifyInfoResponse;
import org.openapitools.client.model.RestNotifyInfoResponse;
import org.openapitools.client.model.StatusNotifyInfoResponse;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.notify.dialog.bean.NotifyInfoInputData;
import com.clustercontrol.notify.util.NotifyConvertUtil;
import com.clustercontrol.notify.util.NotifyRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.UIManager;

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
	 * ステータス通知情報を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param manegerName マネージャ名
	 * @param notifyId 取得対象の通知ID
	 * @return 通知情報
	 *
	 */
	public NotifyInfoInputData getStatusNotify(String managerName, String notifyId) {
		NotifyInfoInputData info = null;

		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			StatusNotifyInfoResponse dto = wrapper.getStatusNotify(notifyId);
			info = new NotifyInfoInputData();
			NotifyConvertUtil.convertStatusNotifyToInputData(dto, info);
		} catch (Exception e) {
			handlingExceptionWithGetNotify(e);
		}
		return info;
	}

	/**
	 * イベント通知情報を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param manegerName マネージャ名
	 * @param notifyId 取得対象の通知ID
	 * @return 通知情報
	 *
	 */
	public NotifyInfoInputData getEventNotify(String managerName, String notifyId) {
		NotifyInfoInputData info = null;

		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			EventNotifyInfoResponse dto = wrapper.getEventNotify(notifyId);
			info = new NotifyInfoInputData();
			NotifyConvertUtil.convertEventNotifyToInputData(dto, info);
		} catch (Exception e) {
			handlingExceptionWithGetNotify(e);
		}
		return info;
	}

	/**
	 * メール通知情報を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param manegerName マネージャ名
	 * @param notifyId 取得対象の通知ID
	 * @return 通知情報
	 *
	 */
	public NotifyInfoInputData getMailNotify(String managerName, String notifyId) {
		NotifyInfoInputData info = null;

		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			MailNotifyInfoResponse dto = wrapper.getMailNotify(notifyId);
			info = new NotifyInfoInputData();
			NotifyConvertUtil.convertMailNotifyToInputData(dto, info);
		} catch (Exception e) {
			handlingExceptionWithGetNotify(e);
		}
		return info;
	}

	/**
	 * ジョブ通知情報を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param manegerName マネージャ名
	 * @param notifyId 取得対象の通知ID
	 * @return 通知情報
	 *
	 */
	public NotifyInfoInputData getJobNotify(String managerName, String notifyId) {
		NotifyInfoInputData info = null;

		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			JobNotifyInfoResponse dto = wrapper.getJobNotify(notifyId);
			info = new NotifyInfoInputData();
			NotifyConvertUtil.convertJobNotifyToInputData(dto, info);
		} catch (Exception e) {
			handlingExceptionWithGetNotify(e);
		}
		return info;
	}

	/**
	 * ログエスカレーション通知情報を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param manegerName マネージャ名
	 * @param notifyId 取得対象の通知ID
	 * @return 通知情報
	 *
	 */
	public NotifyInfoInputData getLogEscalateNotify(String managerName, String notifyId) {
		NotifyInfoInputData info = null;

		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			LogEscalateNotifyInfoResponse dto = wrapper.getLogEscalateNotify(notifyId);
			info = new NotifyInfoInputData();
			NotifyConvertUtil.convertLogEscalateNotifyToInputData(dto, info);
		} catch (Exception e) {
			handlingExceptionWithGetNotify(e);
		}
		return info;
	}

	/**
	 * コマンド通知情報を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param manegerName マネージャ名
	 * @param notifyId 取得対象の通知ID
	 * @return 通知情報
	 *
	 */
	public NotifyInfoInputData getCommandNotify(String managerName, String notifyId) {
		NotifyInfoInputData info = null;

		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			CommandNotifyInfoResponse dto = wrapper.getCommandNotify(notifyId);
			info = new NotifyInfoInputData();
			NotifyConvertUtil.convertCommandNotifyToInputData(dto, info);
		} catch (Exception e) {
			handlingExceptionWithGetNotify(e);
		}
		return info;
	}

	/**
	 * 環境構築通知情報を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param manegerName マネージャ名
	 * @param notifyId 取得対象の通知ID
	 * @return 通知情報
	 *
	 */
	public NotifyInfoInputData getInfraNotify(String managerName, String notifyId) {
		NotifyInfoInputData info = null;

		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			InfraNotifyInfoResponse dto = wrapper.getInfraNotify(notifyId);
			info = new NotifyInfoInputData();
			NotifyConvertUtil.convertInfraNotifyToInputData(dto, info);
		} catch (Exception e) {
			handlingExceptionWithGetNotify(e);
		}
		return info;
	}
	/**
	 * REST通知情報を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param manegerName マネージャ名
	 * @param notifyId 取得対象の通知ID
	 * @return 通知情報
	 *
	 */
	public NotifyInfoInputData getRestNotify(String managerName, String notifyId) {
		NotifyInfoInputData info = null;

		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			RestNotifyInfoResponse dto = wrapper.getRestNotify(notifyId);
			info = new NotifyInfoInputData();
			NotifyConvertUtil.convertRestNotifyToInputData(dto, info);
		} catch (Exception e) {
			handlingExceptionWithGetNotify(e);
		}
		return info;
	}
	
		/**
	 * クラウド通知情報を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param manegerName マネージャ名
	 * @param notifyId 取得対象の通知ID
	 * @return 通知情報
	 *
	 */
	public NotifyInfoInputData getCloudNotify(String managerName, String notifyId) {
		NotifyInfoInputData info = null;

		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			CloudNotifyInfoResponse dto = wrapper.getCloudNotify(notifyId);
			info = new NotifyInfoInputData();
			NotifyConvertUtil.convertCloudNotifyToInputData(dto, info);
		} catch (Exception e) {
			handlingExceptionWithGetNotify(e);
		}
		return info;
	}


	/**
	 * メッセージ通知情報を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param manegerName マネージャ名
	 * @param notifyId 取得対象の通知ID
	 * @return 通知情報
	 *
	 */
	public NotifyInfoInputData getMessageNotify(String managerName, String notifyId) {
		NotifyInfoInputData info = null;

		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			MessageNotifyInfoResponse dto = wrapper.getMessageNotify(notifyId);
			info = new NotifyInfoInputData();
			NotifyConvertUtil.convertMessageNotifyToInputData(dto, info);
		} catch (Exception e) {
			handlingExceptionWithGetNotify(e);
		}
		return info;
	}

	/**
	 * 単一の情報を返すgetNotifyのエラー共通処理
	 * @param e
	 */
	private void handlingExceptionWithGetNotify(Exception e) {
		if (e instanceof InvalidRole) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} else {
			m_log.warn("getNotify(), " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
	}

	/**
	 * 通知情報一覧を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @return 通知情報一覧
	 */
	private Map<String, List<NotifyInfoResponse>> getNotifyList(){

		Map<String, List<NotifyInfoResponse>> dispDataMap= new ConcurrentHashMap<>();
		List<NotifyInfoResponse> records = null;
		for (String managerName : RestConnectManager.getActiveManagerSet()) {
			try {
				NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
				records = wrapper.getNotifyList(null);
				dispDataMap.put(managerName, records);
			} catch (InvalidRole e) {
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
	private Map<String, List<NotifyInfoResponse>> getNotifyListByOwnerRole(String ownerRoleId) throws InvalidRole{

		Map<String, List<NotifyInfoResponse>> dispDataMap= new ConcurrentHashMap<>();
		List<NotifyInfoResponse> records = null;
		Map<String, String> errMsgs = new ConcurrentHashMap<>();
		String managerName = "";
		try {
			for (String activeManager : RestConnectManager.getActiveManagerSet()) {
				managerName = activeManager;
				NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
				records = wrapper.getNotifyList(ownerRoleId);
				dispDataMap.put(managerName, records);
			}
		} catch (InvalidRole e) {
			errMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
			m_log.debug("getNotifyListByOwnerRole(), invalid role, managerName=" + managerName + ", ownerRoleId=" + ownerRoleId);
			throw e;
		} catch (Exception e) {
			m_log.warn("getNotifyListByOwnerRole(), " + HinemosMessage.replace(e.getMessage()), e);
			errMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		} finally { 
			//メッセージ表示
			if( 0 < errMsgs.size() ){
				UIManager.showMessageBox(errMsgs, true);
			}
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
	public Map<String, List<NotifyInfoResponse>> getNotifyList(String managerName){

		if(managerName == null) {
			return getNotifyList();
		}

		Map<String, List<NotifyInfoResponse>> dispDataMap= new ConcurrentHashMap<>();
		List<NotifyInfoResponse> records = null;
		Map<String, String> errMsgs = new ConcurrentHashMap<>();

		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			records = wrapper.getNotifyList(null);
			dispDataMap.put(managerName, records);
		} catch (InvalidRole e) {
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
	public Map<String, List<NotifyInfoResponse>> getNotifyListByOwnerRole(String managerName, String ownerRoleId) throws InvalidRole{

		if(managerName == null) {
			m_log.debug("getNotifyListByOwnerRole(), managerName is null");
			return getNotifyListByOwnerRole(ownerRoleId);
		}

		Map<String, List<NotifyInfoResponse>> dispDataMap= new ConcurrentHashMap<>();
		List<NotifyInfoResponse> records = null;
		Map<String, String> errMsgs = new ConcurrentHashMap<>();

		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			records = wrapper.getNotifyList(ownerRoleId);
			dispDataMap.put(managerName, records);
		} catch (InvalidRole e) {
			m_log.debug("getNotifyListByOwnerRole(), invalid role, managerName=" + managerName + ", ownerRoleId=" + ownerRoleId);
			errMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
			throw e;
		} catch (Exception e) {
			m_log.warn("getNotifyListByOwnerRole(), " + HinemosMessage.replace(e.getMessage()), e);
			errMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		} finally {
			//メッセージ表示
			if( 0 < errMsgs.size() ){
				UIManager.showMessageBox(errMsgs, false);
			}
		}
		return dispDataMap;
	}
}
