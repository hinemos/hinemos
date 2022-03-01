/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.ModifyRpaScenarioRequest;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * シナリオ情報を修正するクライアント側アクションクラス
 */
public class ModifyRpaScenario {

	/**
	 * シナリオ情報を修正します。
	 * 
	 * @param シナリオ情報
	 * @return 成功時 true 失敗時 false
	 */
	public boolean modify(String managerName, String scenarioId, ModifyRpaScenarioRequest info) {
		boolean ret = false;

		String[] args = { scenarioId, managerName };
		try {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(managerName);
			wrapper.modifyRpaScenario(scenarioId, info);

			MessageDialog.openInformation(null,
					Messages.getString("successful"),
					Messages.getString("message.rpa.scenario.5", args));

			ret = true;

		} catch (HinemosUnknown e) {
			String errMessage = HinemosMessage.replace(e.getMessage());
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.rpa.scenario.6", args)
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

			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.rpa.scenario.6", args)
							+ errMessage);
		}

		return ret;
	}

}
