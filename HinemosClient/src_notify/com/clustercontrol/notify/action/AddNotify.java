/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.AddCloudNotifyRequest;
import org.openapitools.client.model.AddCommandNotifyRequest;
import org.openapitools.client.model.AddEventNotifyRequest;
import org.openapitools.client.model.AddInfraNotifyRequest;
import org.openapitools.client.model.AddJobNotifyRequest;
import org.openapitools.client.model.AddLogEscalateNotifyRequest;
import org.openapitools.client.model.AddMailNotifyRequest;
import org.openapitools.client.model.AddMessageNotifyRequest;
import org.openapitools.client.model.AddRestNotifyRequest;
import org.openapitools.client.model.AddStatusNotifyRequest;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.NotifyDuplicate;
import com.clustercontrol.notify.dialog.bean.NotifyInfoInputData;
import com.clustercontrol.notify.util.NotifyConvertUtil;
import com.clustercontrol.notify.util.NotifyRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * 通知情報を作成するクライアント側アクションクラス<BR>
 *
 * @version 2.2.0
 * @since 1.0.0
 */
public class AddNotify {

	/**
	 * ステータス通知情報を作成します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName マネージャ名
	 * @param info 作成対象の通知情報
	 * @return 作成に成功した場合、<code> true </code>
	 *
	 */
	public boolean addStatusNotify(String managerName, NotifyInfoInputData info) {

		boolean result = false;
		String[] args = { info.getNotifyId(), managerName };
		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			AddStatusNotifyRequest request = new AddStatusNotifyRequest();
			NotifyConvertUtil.convertStatusNotifyToRequest(info, request);
			wrapper.addStatusNotify(request);
			result = true;

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.notify.1", args));

		} catch (Exception e) {
			handlingException(e, args);
		}
		return result;
	}

	/**
	 * イベント通知情報を作成します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName マネージャ名
	 * @param info 作成対象の通知情報
	 * @return 作成に成功した場合、<code> true </code>
	 *
	 */
	public boolean addEventNotify(String managerName, NotifyInfoInputData info) {

		boolean result = false;
		String[] args = { info.getNotifyId(), managerName };
		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			AddEventNotifyRequest request = new AddEventNotifyRequest();
			NotifyConvertUtil.convertEventNotifyToRequest(info, request);
			wrapper.addEventNotify(request);
			result = true;

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.notify.1", args));

		} catch (Exception e) {
			handlingException(e, args);
		}
		return result;
	}

	/**
	 * メール通知情報を作成します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName マネージャ名
	 * @param info 作成対象の通知情報
	 * @return 作成に成功した場合、<code> true </code>
	 *
	 */
	public boolean addMailNotify(String managerName, NotifyInfoInputData info) {

		boolean result = false;
		String[] args = { info.getNotifyId(), managerName };
		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			AddMailNotifyRequest request = new AddMailNotifyRequest();
			NotifyConvertUtil.convertMailNotifyToRequest(info, request);
			wrapper.addMailNotify(request);
			result = true;

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.notify.1", args));

		} catch (Exception e) {
			handlingException(e, args);
		}
		return result;
	}

	/**
	 * ジョブ通知情報を作成します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName マネージャ名
	 * @param info 作成対象の通知情報
	 * @return 作成に成功した場合、<code> true </code>
	 *
	 */
	public boolean addJobNotify(String managerName, NotifyInfoInputData info) {

		boolean result = false;
		String[] args = { info.getNotifyId(), managerName };
		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			AddJobNotifyRequest request = new AddJobNotifyRequest();
			NotifyConvertUtil.convertJobNotifyToRequest(info, request);
			wrapper.addJobNotify(request);
			result = true;

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.notify.1", args));

		} catch (Exception e) {
			handlingException(e, args);
		}
		return result;
	}

	/**
	 * ログエスカレーション通知情報を作成します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName マネージャ名
	 * @param info 作成対象の通知情報
	 * @return 作成に成功した場合、<code> true </code>
	 *
	 */
	public boolean addLogEscalateNotify(String managerName, NotifyInfoInputData info) {

		boolean result = false;
		String[] args = { info.getNotifyId(), managerName };
		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			AddLogEscalateNotifyRequest request = new AddLogEscalateNotifyRequest();
			NotifyConvertUtil.convertLogEscalateNotifyToRequest(info, request);
			wrapper.addLogEscalateNotify(request);
			result = true;

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.notify.1", args));

		} catch (Exception e) {
			handlingException(e, args);
		}
		return result;
	}

	/**
	 * コマンド通知情報を作成します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName マネージャ名
	 * @param info 作成対象の通知情報
	 * @return 作成に成功した場合、<code> true </code>
	 *
	 */
	public boolean addCommandNotify(String managerName, NotifyInfoInputData info) {

		boolean result = false;
		String[] args = { info.getNotifyId(), managerName };
		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			AddCommandNotifyRequest request = new AddCommandNotifyRequest();
			NotifyConvertUtil.convertCommandNotifyToRequest(info, request);
			wrapper.addCommandNotify(request);
			result = true;

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.notify.1", args));

		} catch (Exception e) {
			handlingException(e, args);
		}
		return result;
	}

	/**
	 * 環境構築通知情報を作成します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName マネージャ名
	 * @param info 作成対象の通知情報
	 * @return 作成に成功した場合、<code> true </code>
	 *
	 */
	public boolean addInfraNotify(String managerName, NotifyInfoInputData info) {

		boolean result = false;
		String[] args = { info.getNotifyId(), managerName };
		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			AddInfraNotifyRequest request = new AddInfraNotifyRequest();
			NotifyConvertUtil.convertInfraNotifyToRequest(info, request);
			wrapper.addInfraNotify(request);
			result = true;

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.notify.1", args));

		} catch (Exception e) {
			handlingException(e, args);
		}
		return result;
	}
	
	/**
	 * クラウド通知情報を作成します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName マネージャ名
	 * @param info 作成対象の通知情報
	 * @return 作成に成功した場合、<code> true </code>
	 *
	 */
	public boolean addCloudNotify(String managerName, NotifyInfoInputData info) {

		boolean result = false;
		String[] args = { info.getNotifyId(), managerName };
		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			AddCloudNotifyRequest request = new AddCloudNotifyRequest();
			NotifyConvertUtil.convertCloudNotifyToRequest(info, request);
			wrapper.addCloudNotify(request);
			result = true;

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.notify.1", args));

		} catch (Exception e) {
			handlingException(e, args);
		}
		return result;
	}

	/**
	 * REST通知情報を作成します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName マネージャ名
	 * @param info 作成対象の通知情報
	 * @return 作成に成功した場合、<code> true </code>
	 *
	 */
	public boolean addRestNotify(String managerName, NotifyInfoInputData info) {

		boolean result = false;
		String[] args = { info.getNotifyId(), managerName };
		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			AddRestNotifyRequest request = new AddRestNotifyRequest();
			NotifyConvertUtil.convertRestNotifyToRequest(info, request);
			wrapper.addRestNotify(request);
			result = true;

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.notify.1", args));

		} catch (Exception e) {
			handlingException(e, args);
		}
		return result;
	}

	/**
	 * メッセージ通知情報を作成します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName マネージャ名
	 * @param info 作成対象の通知情報
	 * @return 作成に成功した場合、<code> true </code>
	 *
	 */
	public boolean addMessageNotify(String managerName, NotifyInfoInputData info) {

		boolean result = false;
		String[] args = { info.getNotifyId(), managerName };
		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			AddMessageNotifyRequest request = new AddMessageNotifyRequest();
			NotifyConvertUtil.convertMessageNotifyToRequest(info, request);
			wrapper.addMessageNotify(request);
			result = true;

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.notify.1", args));

		} catch (Exception e) {
			handlingException(e, args);
		}
		return result;
	}

	/**
	 * add時のエラー共通処理
	 * @param e
	 * @param args
	 */
	private void handlingException(Exception e, String[] args) {
		if (e instanceof NotifyDuplicate) {
			// 通知IDが重複している場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.notify.19", args));

		} else {
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
					Messages.getString("message.notify.2", args) + errMessage);
		}
	}
}
