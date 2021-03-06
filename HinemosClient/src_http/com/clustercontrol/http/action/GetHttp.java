/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.http.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;

/**
 * HTTP監視設定情報を取得するクライアント側アクションクラス<BR>
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class GetHttp {

	// ログ
	private static Log m_log = LogFactory.getLog( GetHttp.class );

	/**
	 * HTTP監視情報をマネージャから取得します。<BR>
	 *
	 * @param monitorId 監視項目ID
	 * @return HTTP監視情報
	 */
	public MonitorInfo getHttp(String managerName, String monitorId) {

		MonitorInfo info = null;

		try {
			MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
			info = wrapper.getMonitor(monitorId);
		} catch (InvalidRole_Exception e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));

		} catch (Exception e) {
			m_log.warn("getHttp() getMonitor, " + HinemosMessage.replace(e.getMessage()), e);
			// 上記以外の例外
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		return info;
	}
}
