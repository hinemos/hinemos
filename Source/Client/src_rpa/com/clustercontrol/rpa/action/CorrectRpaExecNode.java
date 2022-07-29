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
import org.openapitools.client.model.CorrectExecNodeRequest;
import org.openapitools.client.model.UpdateRpaScenarioOperationResultRequest;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * 実行ノード訂正クライアント側アクションクラス
 */
public class CorrectRpaExecNode {

	// ログ
	private static Log m_log = LogFactory.getLog( CorrectRpaExecNode.class );

	/**
	 * 実行ノード訂正を行います。
	 *
	 * @param managerName マネージャ名
	 * @param request リクエストDTO
	 */
	public void correctRpaExecNode(String managerName, CorrectExecNodeRequest request) {
		m_log.debug("request = " + request);
		
		// 確認ダイアログの表示
		if (!MessageDialog.openConfirm(
				null,
				Messages.getString("confirmed"),
				Messages.getString("message.rpa.scenario.correct.execution.node.confirm"))) {
			// OKが押されない場合は終了
			return;
		}
		
		try {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(managerName);
			wrapper.correctExecNode(request);
			MessageDialog.openInformation(null, Messages.getString("successful"), 
					Messages.getString("message.rpa.scenario.correct.execution.node.finish", new String[]{managerName}));
		} catch (InvalidUserPass | InvalidRole | RestConnectFailed | HinemosUnknown | InvalidSetting e) {
			String errMessage = HinemosMessage.replace(e.getMessage());
			m_log.warn("correctRpaExecNode(), " + errMessage, e);
			MessageDialog.openError(null, Messages.getString("failed"),	errMessage);
		}
	}

	/**
	 * シナリオ実績更新を行います。
	 *
	 * @param managerName マネージャ名
	 * @param request リクエストDTO
	 */
	public void updateExecNode(String managerName, UpdateRpaScenarioOperationResultRequest request) {
		m_log.debug("request = " + request);
		
		// 確認ダイアログの表示
		if (!MessageDialog.openConfirm(
				null,
				Messages.getString("confirmed"),
				Messages.getString("message.rpa.scenario.update.operation.result.confirm", new String[]{
						request.getScenarioOperationResultCreateSettingId(),
						request.getScenarioIdentifyString(),
						request.getFromDate(),
						request.getToDate()
				}))) {
			// OKが押されない場合は終了
			return;
		}

		
		try {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(managerName);
			wrapper.updateOperationResult(request);
			MessageDialog.openInformation(null, Messages.getString("successful"), 
					Messages.getString("message.rpa.scenario.update.operation.result.finish", new String[]{managerName}));
		} catch (InvalidUserPass | InvalidRole | RestConnectFailed | HinemosUnknown | InvalidSetting e) {
			String errMessage = HinemosMessage.replace(e.getMessage());
			m_log.warn("updateExecNode(), " + errMessage, e);
			MessageDialog.openError(null, Messages.getString("failed"),	errMessage);
		}
	}
}
