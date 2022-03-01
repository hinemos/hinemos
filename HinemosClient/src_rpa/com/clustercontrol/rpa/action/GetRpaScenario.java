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
import org.openapitools.client.model.GetRpaScenarioCorrectExecNodeResponse;
import org.openapitools.client.model.GetRpaScenarioResponse;

import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * RPAシナリオ情報を取得するクライアント側アクションクラス
 */
public class GetRpaScenario {

	// ログ
	private static Log m_log = LogFactory.getLog( GetRpaScenario.class );

	/**
	 * RPAシナリオ情報を返します。
	 *
	 * @param managerName マネージャ名
	 * @param scenarioId シナリオID
	 * @return RPAシナリオ情報
	 */
	public GetRpaScenarioResponse getRpaScenario(String managerName, String scenarioId) {

		GetRpaScenarioResponse info = null;
		
		m_log.info("scenarioId = " + scenarioId);
		
		try {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(managerName);
			info = wrapper.getRpaScenario(scenarioId);
		} catch (Exception e) {
			// 上記以外の例外
			String errMessage = HinemosMessage.replace(e.getMessage());
			m_log.warn("getRpaScenario(), " + errMessage, e);
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + errMessage);
		}

		return info;
	}
	
	/**
	 * 実行ノード訂正ダイアログ用のRPAシナリオ情報を返します。
	 *
	 * @param managerName マネージャ名
	 * @param scenarioId シナリオID
	 * @return RPAシナリオ情報
	 */
	public GetRpaScenarioCorrectExecNodeResponse getRpaScenarioCorrectExecNode(String managerName, String scenarioId) {

		GetRpaScenarioCorrectExecNodeResponse info = null;
		
		m_log.info("scenarioId = " + scenarioId);
		
		try {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(managerName);
			info = wrapper.getRpaScenarioCorrectExecNode(scenarioId);
		} catch (Exception e) {
			// 上記以外の例外
			String errMessage = HinemosMessage.replace(e.getMessage());
			m_log.warn("getRpaScenario(), " + errMessage, e);
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + errMessage);
		}

		return info;
	}
}
