/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.AddLogFormatRequest;
import org.openapitools.client.model.AddTransferInfoRequest;
import org.openapitools.client.model.LogFormatResponse;
import org.openapitools.client.model.TransferInfoResponse;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.LogFormatDuplicate;
import com.clustercontrol.fault.LogTransferDuplicate;
import com.clustercontrol.hub.util.HubRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;

/**
 * 
 *
 */
public class AddLog {
	/**
	 * 監視設定[ログフォーマット]情報を追加します。<BR>
	 *
	 * @param managerName マネージャ名
	 * @param format 監視設定[ログフォーマット]情報
	 * @return 登録に成功した場合、true
	 */
	public boolean addLogFormat(String managerName, LogFormatResponse format) {
		try {
			AddLogFormatRequest request = new AddLogFormatRequest();
			RestClientBeanUtil.convertBean(format, request);
			HubRestClientWrapper wrapper = HubRestClientWrapper.getWrapper(managerName);
			wrapper.addLogFormat(request);
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.hub.log.format.create.finish",
							new Object[] { format.getLogFormatId(), managerName}));
			return true;
		} catch (Exception e) {
			String errMessage = "";
			if (e instanceof InvalidRole) {
				MessageDialog.openInformation(
						null,
						Messages.getString("failed"),
						Messages.getString("message.accesscontrol.16"));
			}else if (e instanceof InvalidSetting) {
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString(HinemosMessage.replace(e.getMessage())));
			}else if (e instanceof LogFormatDuplicate) {
				MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString(HinemosMessage.replace(e.getMessage())));
			}else {
				errMessage = ", " + e.getMessage();
			}
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hub.log.format.create.failed") + errMessage);
			return false;
		}
	}
	/**
	 * 収集蓄積[転送]情報を追加します。<BR>
	 *
	 * @param managerName マネージャ名
	 * @param transfer 収集蓄積[転送]情報
	 * @return 登録に成功した場合、true
	 */
	public boolean addLogTransfer(String managerName, TransferInfoResponse transfer) {
		try {
			AddTransferInfoRequest request = new AddTransferInfoRequest();
			RestClientBeanUtil.convertBean(transfer, request);
			HubRestClientWrapper wrapper = HubRestClientWrapper.getWrapper(managerName);
			wrapper.addTransferInfo(request);
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.hub.log.transfer.create.finish",
							new Object[] { transfer.getTransferId(), managerName}));
			return true;
		} catch (Exception e) {
			String errMessage = "";
			if (e instanceof InvalidRole) {
				MessageDialog.openInformation(
						null,
						Messages.getString("failed"),
						Messages.getString("message.accesscontrol.16"));
			}else if (e instanceof InvalidSetting) {
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString(HinemosMessage.replace(e.getMessage())));
			}else if (e instanceof LogTransferDuplicate) {
				MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString(HinemosMessage.replace(e.getMessage())));
			}else {
				errMessage = ", " + e.getMessage();
			}
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hub.log.transfer.create.failed") + errMessage);
			return false;
		}
	}
}