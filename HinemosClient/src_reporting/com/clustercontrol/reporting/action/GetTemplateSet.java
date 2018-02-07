/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.reporting.util.ReportingEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.reporting.TemplateSetInfo;

/**
 * テンプレートセット情報を取得するクライアント側アクションクラス<BR>
 *
 * @version 5.0.a
 * @since 5.0.a
 */
public class GetTemplateSet {

	// ログ
	private static Log m_log = LogFactory.getLog( GetTemplateSet.class );

	/**
	 * テンプレートセット情報を返します。
	 *
	 * @param managerName マネージャ名
	 * @param templateSetId テンプレートセットID
	 * @return テンプレートセット情報
	 */
	public TemplateSetInfo getTemplateSetInfo(String managerName, String templateSetId) {

		TemplateSetInfo info = null;
		
		m_log.info("templateSetId = " + templateSetId);
		
		try {
			ReportingEndpointWrapper wrapper = ReportingEndpointWrapper.getWrapper(managerName);
			info = wrapper.getTemplateSetInfo(templateSetId);
		} catch (Exception e) {
			// 上記以外の例外
			String errMessage = HinemosMessage.replace(e.getMessage());
			m_log.warn("getTemplateSetInfo(), " + errMessage, e);
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + errMessage);
		}

		return info;
	}
}
