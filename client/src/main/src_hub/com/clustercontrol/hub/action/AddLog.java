/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.hub.action;

import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.hub.util.HubEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.hub.InvalidRole_Exception;
import com.clustercontrol.ws.hub.InvalidSetting_Exception;
import com.clustercontrol.ws.hub.LogFormat;
import com.clustercontrol.ws.hub.LogFormatDuplicate_Exception;
import com.clustercontrol.ws.hub.LogTransferDuplicate_Exception;
import com.clustercontrol.ws.hub.TransferInfo;

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
	public boolean addLogFormat(String managerName, LogFormat format) {
		try {
			HubEndpointWrapper wrapper = HubEndpointWrapper.getWrapper(managerName);
			wrapper.addLogFormat(format);
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.hub.log.format.create.finish",
							new Object[] { format.getLogFormatId(), managerName}));
			return true;
		} catch (Exception e) {
			String errMessage = "";
			if (e instanceof InvalidRole_Exception) {
				MessageDialog.openInformation(
						null,
						Messages.getString("failed"),
						Messages.getString("message.accesscontrol.16"));
			}else if (e instanceof InvalidSetting_Exception) {
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString(HinemosMessage.replace(e.getMessage())));
			}else if (e instanceof LogFormatDuplicate_Exception) {
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
	public boolean addLogTransfer(String managerName, TransferInfo transfer) {
		try {
			HubEndpointWrapper wrapper = HubEndpointWrapper.getWrapper(managerName);
			wrapper.addTransferInfo(transfer);
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.hub.log.transfer.create.finish",
							new Object[] { transfer.getTransferId(), managerName}));
			return true;
		} catch (Exception e) {
			String errMessage = "";
			if (e instanceof InvalidRole_Exception) {
				MessageDialog.openInformation(
						null,
						Messages.getString("failed"),
						Messages.getString("message.accesscontrol.16"));
			}else if (e instanceof InvalidSetting_Exception) {
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString(HinemosMessage.replace(e.getMessage())));
			}else if (e instanceof LogTransferDuplicate_Exception) {
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