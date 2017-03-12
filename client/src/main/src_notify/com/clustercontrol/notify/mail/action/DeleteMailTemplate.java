/*

Copyright (C) 2008 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.notify.mail.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.notify.mail.util.MailTemplateEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.mailtemplate.InvalidRole_Exception;

/**
 * メールテンプレート情報を削除するクライアント側アクションクラス<BR>
 *
 * @version 2.4.0
 * @since 2.4.0
 */
public class DeleteMailTemplate {

	// ログ
	private static Log m_log = LogFactory.getLog( DeleteMailTemplate.class );

	/**
	 * メールテンプレート情報を削除します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName マネージャ名
	 * @param mailTemplateId 削除対象のメールテンプレートID
	 * @return 削除に成功した場合、<code> true </code>
	 *
	 */
	public boolean delete(String managerName, String mailTemplateId) {

		boolean result = false;
		String[] args = { mailTemplateId, managerName };
		try {
			MailTemplateEndpointWrapper wrapper = MailTemplateEndpointWrapper.getWrapper(managerName);
			result = wrapper.deleteMailTemplate(mailTemplateId);

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.notify.mail.5", args));

		} catch (InvalidRole_Exception e) {
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.notify.mail.11", args));

		} catch (Exception e) {
			m_log.warn("delete(), " + e.getMessage());
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.notify.mail.6", args) + " " + HinemosMessage.replace(e.getMessage()));
		}

		return result;
	}
}
