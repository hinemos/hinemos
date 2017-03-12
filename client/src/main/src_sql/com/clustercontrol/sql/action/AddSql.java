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
import com.clustercontrol.ws.monitor.MonitorDuplicate_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;

/**
 * SQL監視情報を登録するクライアント側アクションクラス<BR>
 *
 * @version 2.2.0
 * @since 2.0.0
 */
public class AddSql {

	/**
	 * SQL監視情報を追加します。
	 *
	 * @param managerName マネージャ名
	 * @param info SQL監視情報
	 * @return 登録に成功した場合、true
	 */
	public boolean add(String managerName, MonitorInfo info) {

		boolean result = false;
		String[] args = { info.getMonitorId(), managerName };
		try {
			MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
			result = wrapper.addMonitor(info);

			if(result){
				MessageDialog.openInformation(
						null,
						Messages.getString("successful"),
						Messages.getString("message.monitor.33", args));
			} else {
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.monitor.34", args));
			}
		} catch (MonitorDuplicate_Exception e) {
			// 監視項目IDが重複している場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.monitor.53", args));

		} catch (Exception e) {
			String errMessage = "";
			if (e instanceof InvalidRole_Exception) {
				// アクセス権なしの場合、エラーダイアログを表示する
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
					Messages.getString("message.monitor.34", args) + errMessage);
		}

		return result;
	}
}
