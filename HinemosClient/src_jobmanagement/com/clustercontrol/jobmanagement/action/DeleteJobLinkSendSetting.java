/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.action;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * ジョブ連携送信設定情報を削除するクライアント側アクションクラス<BR>
 *
 */
public class DeleteJobLinkSendSetting {

	// ログ
	private static Log m_log = LogFactory.getLog( DeleteJobLinkSendSetting.class );

	/**
	 * ジョブ連携送信設定情報を削除します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName マネージャ名
	 * @param idList 削除対象のIDリスト
	 * @return 削除に成功した場合、<code> true </code>
	 *
	 */
	public boolean delete(String managerName, List<String> idList) {

		boolean result = false;
		String msg = null;
		String[] args = new String[2];

		if (idList.isEmpty()) {
			return result;
		}

		if (idList.size() == 1) {
			args[0] = idList.get(0);
			args[1] = managerName;
			msg = "message.joblinksendsetting.deleted";
		} else {
			args[0] = Integer.toString(idList.size());
			msg = "message.joblinksendsetting.deleted.multiple";
		}

		try {
			JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
			wrapper.deleteJobLinkSendSetting(String.join(",", idList));
			result = true;

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString(msg, args));

		} catch (InvalidRole e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));

		} catch (Exception e) {
			m_log.warn("delete(), " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.joblinksendsetting.deleted.failure")
						+ "[" + HinemosMessage.replace(e.getMessage()) + "]" + " (" + managerName + ")");
		}
		return result;
	}
}
