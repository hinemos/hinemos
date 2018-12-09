/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.action;

import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.hub.util.HubEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.hub.InvalidRole_Exception;
import com.clustercontrol.ws.hub.InvalidSetting_Exception;
import com.clustercontrol.ws.hub.LogFormat;
import com.clustercontrol.ws.hub.TransferInfo;

/**
 * 収集蓄積機能の情報を変更するクライアント側アクションクラス<BR>
 *
 */
public class ModifyLog {
	/**
	 * ログフォーマット情報を変更します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param format 変更対象のログ[フォーマット]情報
	 * @return 変更に成功した場合、<code> true </code>
	 */
	public boolean modifyLogFormat(String managerName, LogFormat format){

		boolean result = false;
		try {
			HubEndpointWrapper wrapper = HubEndpointWrapper.getWrapper(managerName);
			wrapper.modifyLogFormat(format);
			result = true;
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.hub.log.format.modify.finish",
							new Object[] { format.getLogFormatId(), managerName}));
		} catch (Exception e) {
			String errMessage = "";
			if (e instanceof InvalidRole_Exception) {
				MessageDialog.openInformation(
						null,
						Messages.getString("failed"),
						Messages.getString("message.accesscontrol.16"));
			} 
			else if (e instanceof InvalidSetting_Exception) {
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString(HinemosMessage.replace(e.getMessage())));
			}
			else {
				errMessage = ", " + e.getMessage();
			}
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hub.log.format.modify.failed") + errMessage);
		}
		return result;
	}
	/**
	 * 収集蓄積[転送]情報を変更します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param transfer 変更対象の収集蓄積[転送]情報
	 * @return 変更に成功した場合、<code> true </code>
	 */
	public boolean modifyLogTransfer(String managerName, TransferInfo transfer){

		boolean result = false;
		try {
			HubEndpointWrapper wrapper = HubEndpointWrapper.getWrapper(managerName);
			wrapper.modifyTransferInfo(transfer);
			result = true;
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.hub.log.transfer.modify.finish",
							new Object[] { transfer.getTransferId(), managerName}));
		} catch (Exception e) {
			String errMessage = "";
			if (e instanceof InvalidRole_Exception) {
				MessageDialog.openInformation(
						null,
						Messages.getString("failed"),
						Messages.getString("message.accesscontrol.16"));
			} 
			else if (e instanceof InvalidSetting_Exception) {
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString(HinemosMessage.replace(e.getMessage())));
			}
			else {
				errMessage = ", " + e.getMessage();
			}
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hub.log.transfer.modify.failed") + errMessage);
		}
		return result;
	}
}
