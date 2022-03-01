/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.AddTemplateSetRequest;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.ReportingDuplicate;
import com.clustercontrol.reporting.util.ReportingRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * 
 * テンプレートセット情報を登録するクライアント側アクションクラス<BR>
 * 
 * @version 5.0.a
 * @since 5.0.a
 * 
 */
public class AddTemplateSet {

	/**
	 * テンプレートセット情報を追加します。
	 * 
	 * @param テンプレートセット情報
	 * @return 成功時 true 失敗時 false
	 */
	public boolean add(String managerName, AddTemplateSetRequest info) {
		boolean ret = false;

		String[] args = { info.getTemplateSetId(), managerName };
		try {
			ReportingRestClientWrapper wrapper = ReportingRestClientWrapper.getWrapper(managerName);
			wrapper.addTemplateSet(info);
			ret = true;
			
			MessageDialog.openInformation(null,
					Messages.getString("successful"),
					Messages.getString("message.reporting.34", args));
		} catch (ReportingDuplicate e) {
			// スケジュールIDが重複している場合、エラーダイアログを表示する
			MessageDialog.openInformation(null, 
					Messages.getString("message"), 
					Messages.getString("message.reporting.36", args));
		} catch (HinemosUnknown e) {
			String errMessage = HinemosMessage.replace(e.getMessage());
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.reporting.35", args)
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
					Messages.getString("message.reporting.35", args) + errMessage);
		}

		return ret;
	}

}
