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
import org.openapitools.client.model.RpaScenarioOperationResultCreateSettingResponse;

import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.util.Messages;

/**
 * シナリオ実績作成設定情報を取得するクライアント側アクションクラス
 */
public class GetRpaScenarioCreateSetting {

	// ログ
	private static Log m_log = LogFactory.getLog( GetRpaScenarioCreateSetting.class );

	/**
	 * シナリオ実績作成設定情報を返します。
	 *
	 * @param managerName マネージャ名
	 * @param settingId 実績作成設定ID
	 * @return シナリオ実績作成設定情報
	 */
	public RpaScenarioOperationResultCreateSettingResponse getSetting(String managerName, String settingId) {

		RpaScenarioOperationResultCreateSettingResponse info = null;
		
		m_log.info("RpaScenarioOperationResultCreateSettingId = " + settingId);
		
		try {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(managerName);
			info = wrapper.getRpascenarioOperationResultCreateSetting(settingId);
		} catch (Exception e) {
			m_log.warn("getSetting(), " + e.getMessage(), e);
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					e.getMessage());
		}

		return info;
	}
}
