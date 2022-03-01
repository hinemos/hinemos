/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.AddRpaScenarioOperationResultCreateSettingRequest;

import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.util.Messages;

/**
 * シナリオ実績作成設定情報を登録するクライアント側アクションクラス
 */
public class AddRpaScenarioCreateSetting {

	/**
	 * シナリオ実績作成設定情報を追加します。
	 * 
	 * @param シナリオ実績作成設定情報
	 * @return 成功時 true 失敗時 false
	 */
	public boolean add(String managerName, AddRpaScenarioOperationResultCreateSettingRequest info) {
		boolean ret = false;

		String[] args = { info.getScenarioOperationResultCreateSettingId(), managerName };
		try {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(managerName);
			wrapper.addRpascenarioOperationResultCreateSetting(info);
			ret = true;
			
			MessageDialog.openInformation(null,
					Messages.getString("successful"),
					Messages.getString("message.rpa.scenario.create.setting.create.finish", args));
		} catch (Exception e) {
			MessageDialog.openError(null, 
					Messages.getString("failed"),
					e.getMessage());
		}

		return ret;
	}

}
