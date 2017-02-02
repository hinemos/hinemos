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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.JdbcDriverInfo;

/**
 * JDBC情報を取得するクライアント側アクションクラス<BR>
 *
 * @version 2.1.0
 * @since 2.0.0
 */
public class GetJdbc {

	// ログ
	private static Log m_log = LogFactory.getLog( GetJdbc.class );

	/**
	 * SQL監視情報を返します。
	 *
	 * @param monitorId 監視項目ID
	 * @param monitorType 監視判定タイプ
	 * @return SQL監視情報
	 */
	public static List<List<String>> getJdbcDriverList(String managerName) {

		List<List<String>> list = new ArrayList<List<String>>();

		try {
			MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
			List<JdbcDriverInfo> driverList = wrapper.getJdbcDriverList();
			for (JdbcDriverInfo driver : driverList) {
				ArrayList<String> a = new ArrayList<String>();
				a.add(driver.getJdbcDriverName());
				a.add(driver.getJdbcDriverClass());
				list.add(a);
			}
		} catch (InvalidRole_Exception e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));

		} catch (Exception e) {
			// 上記以外の例外
			m_log.warn("getJdbcDriverList(), " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
		return list;
	}

}
