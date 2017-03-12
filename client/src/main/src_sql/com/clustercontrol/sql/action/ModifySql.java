/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.sql.action;

import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;

/**
 * SQL監視設定情報を更新するクライアント側アクションクラス<BR>
 *
 * @version 2.2.0
 * @since 2.0.0
 */
public class ModifySql {

	/**
	 * SQL監視情報を更新します。
	 *
	 * @param managerName マネージャ名
	 * @param info SQL監視情報
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
