/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.snmp.action;

import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;

/**
 * SNMP監視情報を更新するクライアント側アクションクラス<BR>
 *
 * @version 2.2.0
 * @since 2.0.0
 */
public class ModifySnmp {

	/**
	 * SNMP監視情報を更新します。
	 *
	 * @param info SNMP監視情報
	 * @return 更新に成功した場合、true
	 */
	public boolean modify(String managerName, MonitorInfo info) {

		boolean result = false;
		String[] args = { info.getMonitorId(), managerName };
		String errMessage = "";
		try {
			MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
			result = wrapper.modifyMonitor(info);
		} catch (InvalidRole_Exception e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			errMessage = ", " + HinemosMessage.replace(e.getMessage());
		}

		if(result){
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.monitor.35", args));
		} else {
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.monitor.36", args) + errMessage);
		}

		return result;
	}
}
