/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.action;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.ModifyCloudNotifyRequest;
import org.openapitools.client.model.ModifyCommandNotifyRequest;
import org.openapitools.client.model.ModifyEventNotifyRequest;
import org.openapitools.client.model.ModifyInfraNotifyRequest;
import org.openapitools.client.model.ModifyJobNotifyRequest;
import org.openapitools.client.model.ModifyLogEscalateNotifyRequest;
import org.openapitools.client.model.ModifyMailNotifyRequest;
import org.openapitools.client.model.ModifyMessageNotifyRequest;
import org.openapitools.client.model.ModifyRestNotifyRequest;
import org.openapitools.client.model.ModifyStatusNotifyRequest;
import org.openapitools.client.model.SetNotifyValidRequest;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.notify.dialog.bean.NotifyInfoInputData;
import com.clustercontrol.notify.util.NotifyConvertUtil;
import com.clustercontrol.notify.util.NotifyRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * 通知情報を変更するクライアント側アクションクラス<BR>
 *
 * @version 2.2.0
 * @since 1.0.0
 */
public class ModifyNotify {

	/**
	 * ステータス通知情報を変更します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName マネージャ名
	 * @param info 変更対象の通知情報
	 * @return 変更に成功した場合、<code> true </code>
	 */
	public boolean modifyStatusNotify(String managerName, NotifyInfoInputData info){

		boolean result = false;
		String[] args = { info.getNotifyId(), managerName };
		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			ModifyStatusNotifyRequest request = new ModifyStatusNotifyRequest();
			NotifyConvertUtil.convertStatusNotifyToRequest(info, request);
			wrapper.modifyStatusNotify(info.getNotifyId(), request);
			result = true;

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.notify.3", args));

		} catch (Exception e) {
			handlingException(e, args);
		}
		return result;
	}

	/**
	 * イベント通知情報を変更します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName マネージャ名
	 * @param info 変更対象の通知情報
	 * @return 変更に成功した場合、<code> true </code>
	 */
	public boolean modifyEventNotify(String managerName, NotifyInfoInputData info){

		boolean result = false;
		String[] args = { info.getNotifyId(), managerName };
		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			ModifyEventNotifyRequest request = new ModifyEventNotifyRequest();
			NotifyConvertUtil.convertEventNotifyToRequest(info, request);
			wrapper.modifyEventNotify(info.getNotifyId(), request);
			result = true;

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.notify.3", args));

		} catch (Exception e) {
			handlingException(e, args);
		}
		return result;
	}

	/**
	 * メール通知情報を変更します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName マネージャ名
	 * @param info 変更対象の通知情報
	 * @return 変更に成功した場合、<code> true </code>
	 */
	public boolean modifyMailNotify(String managerName, NotifyInfoInputData info){

		boolean result = false;
		String[] args = { info.getNotifyId(), managerName };
		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			ModifyMailNotifyRequest request = new ModifyMailNotifyRequest();
			NotifyConvertUtil.convertMailNotifyToRequest(info, request);
			wrapper.modifyMailNotify(info.getNotifyId(), request);
			result = true;

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.notify.3", args));

		} catch (Exception e) {
			handlingException(e, args);
		}
		return result;
	}

	/**
	 * ジョブ通知情報を変更します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName マネージャ名
	 * @param info 変更対象の通知情報
	 * @return 変更に成功した場合、<code> true </code>
	 */
	public boolean modifyJobNotify(String managerName, NotifyInfoInputData info){

		boolean result = false;
		String[] args = { info.getNotifyId(), managerName };
		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			ModifyJobNotifyRequest request = new ModifyJobNotifyRequest();
			NotifyConvertUtil.convertJobNotifyToRequest(info, request);
			wrapper.modifyJobNotify(info.getNotifyId(), request);
			result = true;

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.notify.3", args));

		} catch (Exception e) {
			handlingException(e, args);
		}
		return result;
	}

	/**
	 * ログエスカレーション通知情報を変更します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName マネージャ名
	 * @param info 変更対象の通知情報
	 * @return 変更に成功した場合、<code> true </code>
	 */
	public boolean modifyLogEscalateNotify(String managerName, NotifyInfoInputData info){

		boolean result = false;
		String[] args = { info.getNotifyId(), managerName };
		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			ModifyLogEscalateNotifyRequest request = new ModifyLogEscalateNotifyRequest();
			NotifyConvertUtil.convertLogEscalateNotifyToRequest(info, request);
			wrapper.modifyLogEscalateNotify(info.getNotifyId(), request);
			result = true;

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.notify.3", args));

		} catch (Exception e) {
			handlingException(e, args);
		}
		return result;
	}

	/**
	 * コマンド通知情報を変更します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName マネージャ名
	 * @param info 変更対象の通知情報
	 * @return 変更に成功した場合、<code> true </code>
	 */
	public boolean modifyCommandNotify(String managerName, NotifyInfoInputData info){

		boolean result = false;
		String[] args = { info.getNotifyId(), managerName };
		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			ModifyCommandNotifyRequest request = new ModifyCommandNotifyRequest();
			NotifyConvertUtil.convertCommandNotifyToRequest(info, request);
			wrapper.modifyCommandNotify(info.getNotifyId(), request);
			result = true;

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.notify.3", args));

		} catch (Exception e) {
			handlingException(e, args);
		}
		return result;
	}

	/**
	 * 環境構築通知情報を変更します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName マネージャ名
	 * @param info 変更対象の通知情報
	 * @return 変更に成功した場合、<code> true </code>
	 */
	public boolean modifyInfraNotify(String managerName, NotifyInfoInputData info){

		boolean result = false;
		String[] args = { info.getNotifyId(), managerName };
		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			ModifyInfraNotifyRequest request = new ModifyInfraNotifyRequest();
			NotifyConvertUtil.convertInfraNotifyToRequest(info, request);
			wrapper.modifyInfraNotify(info.getNotifyId(), request);
			result = true;

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.notify.3", args));

		} catch (Exception e) {
			handlingException(e, args);
		}
		return result;
	}
	
	/**
	 * クラウド通知情報を変更します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName マネージャ名
	 * @param info 変更対象の通知情報
	 * @return 変更に成功した場合、<code> true </code>
	 */
	public boolean modifyCloudNotify(String managerName, NotifyInfoInputData info){

		boolean result = false;
		String[] args = { info.getNotifyId(), managerName };
		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			ModifyCloudNotifyRequest request = new ModifyCloudNotifyRequest();
			NotifyConvertUtil.convertCloudNotifyToRequest(info, request);
			wrapper.modifyCloudNotify(info.getNotifyId(), request);
			result = true;

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.notify.3", args));

		} catch (Exception e) {
			handlingException(e, args);
		}
		return result;
	}


	/**
	 * REST通知情報を変更します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName マネージャ名
	 * @param info 変更対象の通知情報
	 * @return 変更に成功した場合、<code> true </code>
	 */
	public boolean modifyRestNotify(String managerName, NotifyInfoInputData info){

		boolean result = false;
		String[] args = { info.getNotifyId(), managerName };
		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			ModifyRestNotifyRequest request = new ModifyRestNotifyRequest();
			NotifyConvertUtil.convertRestNotifyToRequest(info, request);
			wrapper.modifyRestNotify(info.getNotifyId(), request);
			result = true;

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.notify.3", args));

		} catch (Exception e) {
			handlingException(e, args);
		}
		return result;
	}

	/**
	 * 通知情報の有効/無効を変更します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName マネージャ名
	 * @param notifyIds 変更対象の通知ID
	 * @param valid true 有効 / false 無効 
	 * @return 変更に成功した場合、<code> true </code>
	 */
	public boolean setNotifyValid(String managerName, List<String> notifyIds, boolean valid) {

		boolean result = false;
		String[] args = { String.join(",", notifyIds), managerName };
		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			SetNotifyValidRequest request = new SetNotifyValidRequest();
			request.setNotifyIds(notifyIds);
			request.setValidFlg(valid);
			wrapper.setNotifyValid(request);
			result = true;

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.notify.3", args));

		} catch (Exception e) {
			handlingException(e, args);
		}
		return result;
	}

	/**
	 * メッセージ通知情報を変更します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName マネージャ名
	 * @param info 変更対象の通知情報
	 * @return 変更に成功した場合、<code> true </code>
	 */
	public boolean modifyMessageNotify(String managerName, NotifyInfoInputData info){

		boolean result = false;
		String[] args = { info.getNotifyId(), managerName };
		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			ModifyMessageNotifyRequest request = new ModifyMessageNotifyRequest();
			NotifyConvertUtil.convertMessageNotifyToRequest(info, request);
			wrapper.modifyMessageNotify(info.getNotifyId(), request);
			result = true;

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.notify.3", args));

		} catch (Exception e) {
			handlingException(e, args);
		}
		return result;
	}

	/**
	 * modify時のエラー共通処理
	 * @param e
	 * @param args
	 */
	private void handlingException(Exception e, String[] args) {
		String errMessage = "";
		if (e instanceof InvalidRole) {
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} else {
			errMessage = ", " + HinemosMessage.replace(e.getMessage());
		}
		MessageDialog.openError(
				null,
				Messages.getString("failed"),
				Messages.getString("message.notify.4", args) + errMessage);
	}
}
