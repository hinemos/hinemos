/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.sql.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.JdbcDriverInfoResponse;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

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
			MonitorsettingRestClientWrapper wrapper = MonitorsettingRestClientWrapper.getWrapper(managerName);
			List<JdbcDriverInfoResponse> driverList = wrapper.getJdbcDriverList();
			for (JdbcDriverInfoResponse driver : driverList) {
				ArrayList<String> a = new ArrayList<String>();
				a.add(driver.getJdbcDriverName());
				a.add(driver.getJdbcDriverClass());
				list.add(a);
			}
		} catch (InvalidRole e) {
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
