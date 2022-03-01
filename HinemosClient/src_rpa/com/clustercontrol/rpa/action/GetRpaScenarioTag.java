/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.RpaScenarioTagResponse;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * シナリオタグ情報を取得するクライアント側アクションクラス
 */
public class GetRpaScenarioTag {

	// ログ
	private static Log m_log = LogFactory.getLog( GetRpaScenarioTag.class );

	/**
	 * シナリオタグ情報を返します。
	 *
	 * @param managerName マネージャ名
	 * @param tagId タグID
	 * @return シナリオタグ情報
	 */
	public RpaScenarioTagResponse getRpaScenarioTag(String managerName, String tagId) {

		RpaScenarioTagResponse info = null;
		
		m_log.info("tagId = " + tagId);
		
		try {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(managerName);
			info = wrapper.getRpaScenarioTag(tagId);
		} catch (Exception e) {
			// 上記以外の例外
			String errMessage = HinemosMessage.replace(e.getMessage());
			m_log.warn("getRpaScenarioTag(), " + errMessage, e);
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + errMessage);
		}

		return info;
	}
}
