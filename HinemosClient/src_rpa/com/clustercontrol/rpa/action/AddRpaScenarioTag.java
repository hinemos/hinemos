/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.AddRpaScenarioTagRequest;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.RpaScenarioTagDuplicate;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * タグ情報を登録するクライアント側アクションクラス
 */
public class AddRpaScenarioTag {

	/**
	 * タグ情報を追加します。
	 * 
	 * @param タグ情報
	 * @return 成功時 true 失敗時 false
	 */
	public boolean add(String managerName, AddRpaScenarioTagRequest info) {
		boolean ret = false;

		String[] args = { info.getTagId(), managerName };
		try {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(managerName);
			wrapper.addRpaScenarioTag(info);
			ret = true;
			
			MessageDialog.openInformation(null,
					Messages.getString("successful"),
					Messages.getString("message.rpa.tag.1", args));
		} catch (RpaScenarioTagDuplicate e) {
			// タグIDが重複している場合、エラーダイアログを表示する
			MessageDialog.openInformation(null, 
					Messages.getString("message"), 
					Messages.getString("message.rpa.tag.3", args));
		} catch (HinemosUnknown e) {
			String errMessage = HinemosMessage.replace(e.getMessage());
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.rpa.tag.2", args)
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
					Messages.getString("message.rpa.tag.2", args) + errMessage);
		}

		return ret;
	}

}
