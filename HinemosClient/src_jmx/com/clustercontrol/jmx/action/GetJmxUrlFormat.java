/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jmx.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.JmxUrlFormatInfoResponse;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * JMX監視に使用するURLフォーマットを取得するクライアント側アクションクラス
 * 
 * @version 7.0.0
 * @since 7.0.0
 */
public class GetJmxUrlFormat {
	
	private static Log m_log = LogFactory.getLog( GetJmxUrlFormat.class );
	
	/**
	 * JMX監視に使用するURLフォーマットを返す。
	 */
	public static List<List<String>> getJmxUrlFormatList(String managerName) {
		List<List<String>> list = new ArrayList<>();

		try {
			MonitorsettingRestClientWrapper wrapper = MonitorsettingRestClientWrapper.getWrapper(managerName);
			List<JmxUrlFormatInfoResponse> urlFormatList = wrapper.getJmxUrlFormatList();
			for (JmxUrlFormatInfoResponse urlFormat : urlFormatList) {
				ArrayList<String> a = new ArrayList<>();
				a.add(urlFormat.getJmxUrlFormatName());
				a.add(urlFormat.getJmxUrlFormat());
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
