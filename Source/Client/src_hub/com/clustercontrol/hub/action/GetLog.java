/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.action;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.LogFormatResponse;
import org.openapitools.client.model.TransferInfoResponse;

import com.clustercontrol.hub.util.HubRestClientWrapper;
import com.clustercontrol.util.Messages;

public class GetLog {
	
	public GetLog(){
		
	}
	
	/**
	 * ログ[フォーマット]情報を返します<BR>
	 * @param managerName
	 * @param formatId
	 * @return
	 */
	public LogFormatResponse getLogFormat(String managerName, String formatId){
		try {
			HubRestClientWrapper wrapper = HubRestClientWrapper.getWrapper(managerName);
			return wrapper.getLogFormat(formatId);
		} catch (Exception e) {
			// 上記以外の例外
			Logger.getLogger(this.getClass()).warn("getLogFormat(), " + e.getMessage(), e);
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + e.getMessage());
			return null;
		}
	}
	/**
	 * 収集蓄積[転送]情報を返します<BR>
	 * @param managerName
	 * @param transferId
	 * @return
	 */
	public TransferInfoResponse getLogTransfer(String managerName, String transferId){
		try {
			HubRestClientWrapper wrapper = HubRestClientWrapper.getWrapper(managerName);
			return wrapper.getTransferInfo(transferId);
		} catch (Exception e) {
			// 上記以外の例外
			Logger.getLogger(this.getClass()).warn("getLogTransfer(), " + e.getMessage(), e);
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + e.getMessage());
			return null;
		}
	}
}