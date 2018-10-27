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

import com.clustercontrol.hub.util.HubEndpointWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.hub.LogFormat;
import com.clustercontrol.ws.hub.TransferInfo;

public class GetLog {
	
	public GetLog(){
		
	}
	
	/**
	 * ログ[フォーマット]情報を返します<BR>
	 * @param managerName
	 * @param formatId
	 * @return
	 */
	public LogFormat getLogFormat(String managerName, String formatId){
		try {
			HubEndpointWrapper wrapper = HubEndpointWrapper.getWrapper(managerName);
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
	public TransferInfo getLogTransfer(String managerName, String transferId){
		try {
			HubEndpointWrapper wrapper = HubEndpointWrapper.getWrapper(managerName);
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