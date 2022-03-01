/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.LogFormatResponse;
import org.openapitools.client.model.ModifyLogFormatRequest;
import org.openapitools.client.model.ModifyTransferInfoRequest;
import org.openapitools.client.model.TransferInfoResponse;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.hub.util.HubRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;

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
	public boolean modifyLogFormat(String managerName, LogFormatResponse format){

		boolean result = false;
		try {
			ModifyLogFormatRequest request = new ModifyLogFormatRequest();
			RestClientBeanUtil.convertBean(format, request);
			HubRestClientWrapper wrapper = HubRestClientWrapper.getWrapper(managerName);
			wrapper.modifyLogFormat(format.getLogFormatId(), request);
			result = true;
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.hub.log.format.modify.finish",
							new Object[] { format.getLogFormatId(), managerName}));
		} catch (Exception e) {
			String errMessage = "";
			if (e instanceof InvalidRole) {
				MessageDialog.openInformation(
						null,
						Messages.getString("failed"),
						Messages.getString("message.accesscontrol.16"));
			} 
			else if (e instanceof InvalidSetting) {
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
	public boolean modifyLogTransfer(String managerName, TransferInfoResponse transfer){

		boolean result = false;
		try {
			ModifyTransferInfoRequest request = new ModifyTransferInfoRequest();
			RestClientBeanUtil.convertBean(transfer, request);
			HubRestClientWrapper wrapper = HubRestClientWrapper.getWrapper(managerName);
			wrapper.modifyTransferInfo(transfer.getTransferId(), request);
			result = true;
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.hub.log.transfer.modify.finish",
							new Object[] { transfer.getTransferId(), managerName}));
		} catch (Exception e) {
			String errMessage = "";
			if (e instanceof InvalidRole) {
				MessageDialog.openInformation(
						null,
						Messages.getString("failed"),
						Messages.getString("message.accesscontrol.16"));
			} 
			else if (e instanceof InvalidSetting) {
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
