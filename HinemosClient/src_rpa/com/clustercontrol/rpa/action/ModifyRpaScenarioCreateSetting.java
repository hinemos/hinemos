/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.ModifyRpaScenarioOperationResultCreateSettingRequest;

import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.util.Messages;

/**
 * シナリオタグ情報を修正するクライアント側アクションクラス
 */
public class ModifyRpaScenarioCreateSetting {

	/**
	 * シナリオタグ情報を修正します。
	 * 
	 * @param シナリオタグ情報
	 * @return 成功時 true 失敗時 false
	 */
	public boolean modify(String managerName, String settingId, ModifyRpaScenarioOperationResultCreateSettingRequest info) {
		boolean ret = false;

		String[] args = { settingId, managerName };
		try {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(managerName);
			wrapper.modifyRpascenarioOperationResultCreateSetting(settingId, info);

			MessageDialog.openInformation(null,
					Messages.getString("successful"),
					Messages.getString("message.rpa.scenario.create.setting.modify.finish", args));

			ret = true;

		} catch (Exception e) {
			MessageDialog.openError(null, Messages.getString("failed"), e.getMessage());
		}

		return ret;
	}

}
