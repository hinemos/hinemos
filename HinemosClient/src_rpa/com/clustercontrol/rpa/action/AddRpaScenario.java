/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.AddRpaScenarioRequest;
import org.openapitools.client.model.RpaScenarioResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.RpaScenarioDuplicate;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * シナリオ情報を登録するクライアント側アクションクラス
 */
public class AddRpaScenario {

	/**
	 * シナリオ情報を追加します。
	 * 
	 * @param シナリオ情報
	 * @return 成功時 true 失敗時 false
	 */
	public boolean add(String managerName, AddRpaScenarioRequest info) {
		boolean ret = false;

		String[] args = { "", managerName };
		try {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(managerName);
			RpaScenarioResponse response = wrapper.addRpaScenario(info);
			args[0] = response.getScenarioId();
			ret = true;
			
			MessageDialog.openInformation(null,
					Messages.getString("successful"),
					Messages.getString("message.rpa.scenario.1", args));
		} catch (RpaScenarioDuplicate e) {
			// シナリオIDが重複している場合、エラーダイアログを表示する
			MessageDialog.openInformation(null, 
					Messages.getString("message"), 
					Messages.getString("message.rpa.scenario.3", args));
		} catch (HinemosUnknown e) {
			String errMessage = HinemosMessage.replace(e.getMessage());
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.rpa.scenario.2", args)
							+ ", " + errMessage);
		} catch (Exception e) {
			String errMessage = "";
			if (e instanceof InvalidRole) {
				MessageDialog.openInformation(null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} else {
				errMessage = ", " + HinemosMessage.replace(e.getMessage());
			}

			MessageDialog.openError(null, 
					Messages.getString("failed"),
					Messages.getString("message.rpa.scenario.2", args) + errMessage);
		}

		return ret;
	}

}
