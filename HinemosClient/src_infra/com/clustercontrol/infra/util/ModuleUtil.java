/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.util;

import java.util.Base64;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.ModuleNodeResultResponse;
import org.openapitools.client.model.ModuleResultResponse;
import org.openapitools.client.model.ModuleNodeResultResponse.RunCheckTypeEnum;
import org.openapitools.client.model.ModuleResultResponse.ModuleTypeEnum;

import com.clustercontrol.dialog.TextAreaDialog;
import com.clustercontrol.infra.bean.ModuleTypeMessage;
import com.clustercontrol.infra.bean.RunCheckTypeMessage;
import com.clustercontrol.infra.dialog.FileDiffDialog;
import com.clustercontrol.util.Messages;

public class ModuleUtil {
	private static Log m_log = LogFactory.getLog( ModuleUtil.class );

	/**
	 * 
	 * @param moduleId
	 * @param moduleResult
	 * @return
	 */
	public static boolean displayResult(String moduleId, ModuleResultResponse moduleResult) {
		boolean ret = false; // trueになったら、以降実行しない。
		List<ModuleNodeResultResponse> resultList = moduleResult.getNodeResultList();
		StringBuilder str = new StringBuilder();

		String strModuleInfo = "moduleId=" + moduleId;

		if (resultList.size() > 0) {
			if (moduleResult.getModuleType() == ModuleTypeEnum.COMMAND) {
				// コマンドモジュール
				str.append(strModuleInfo + ", size=" + resultList.size() +
						", type=" + ModuleTypeMessage.enumToString(moduleResult.getModuleType()) + "\n");
				for (ModuleNodeResultResponse result : resultList) {
					str.append("\n#####" + result.getFacilityId() + " : " +
							result.getResult().getValue() +
							"(" + RunCheckTypeMessage.enumToString(result.getRunCheckType()) + ")" +
							" \n" + result.getMessage() + " \n");
				}
				m_log.debug("checkModule : " + str);
			} else if (moduleResult.getModuleType() == ModuleTypeEnum.FILETRANSFER){
				// ファイル転送モジュール
				for (ModuleNodeResultResponse result : resultList) {
					if (result.getRunCheckType() == RunCheckTypeEnum.CHECK) {
						if (result.getFileDiscarded()) {
							// ファイルサイズオーバーの場合はメッセージを表示して比較ダイアログを表示しない
							MessageDialog.openInformation(null, Messages.getString("message"), Messages.getString("message.infra.file.discarded"));
						} else {
							FileDiffDialog diffDialog = new FileDiffDialog(null);
							diffDialog.setTitle(Messages.getString("dialog.infra.title.diff", new Object[]{result.getFacilityId()}));
							if (result.getOldFilename() != null) {
								diffDialog.setOldFilename(result.getOldFilename());
							}
							if (result.getOldFile() != null) {
								byte[] oldFile = Base64.getDecoder().decode(result.getOldFile());
								diffDialog.setOldFile(oldFile);
							}
							if (result.getNewFilename() != null) {
								diffDialog.setNewFilename(result.getNewFilename());
							}
							if (result.getNewFile() != null) {
								byte[] newFile = Base64.getDecoder().decode(result.getNewFile());
								diffDialog.setNewFile(newFile);
							}
							m_log.debug("newFilename=" + result.getNewFilename());
							if (diffDialog.open() != IDialogConstants.OK_ID) {
								break;
							}
						}
					}
				}
				str.append(strModuleInfo + ", size=" + resultList.size() +
						", type=" + ModuleTypeMessage.enumToString(moduleResult.getModuleType()) + "\n");
				for (ModuleNodeResultResponse result : resultList) {
					str.append("\n#####" + result.getFacilityId() + " : " +
							result.getResult().getValue() +
							"(" + RunCheckTypeMessage.enumToString(result.getRunCheckType()) + ")" +
							" \n" + result.getMessage() + " \n");
					if (result.getOldFile() != null) {
						byte[] oldFile = Base64.getDecoder().decode(result.getOldFile());
						str.append(", oldFile.size=" + oldFile.length);
					}
					if (result.getNewFile() != null) {
						byte[] newFile = Base64.getDecoder().decode(result.getNewFile());
						str.append(", newFile.size=" + newFile.length);
					}
					str.append("\n");
				}
			}
			TextAreaDialog dialog = new TextAreaDialog(null, Messages.getString("dialog.infra.result"), true, false); 
			dialog.setText(str.toString());
			dialog.setOkButtonText(Messages.getString("next"));
			dialog.setCancelButtonText(Messages.getString("suspend"));
			if (dialog.open() == IDialogConstants.OK_ID) {
				ret = true;
			}
		}
		return ret;
	}
}
