/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.dialog;

import org.eclipse.jface.dialogs.MessageDialogWithToggle;

import com.clustercontrol.util.Messages;

/**
 * Utility用確認ダイアログ
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
public class ClientDeleteProcessDialog implements DeleteProcessDialog {
	private MessageDialogWithToggle dialog;

	public ClientDeleteProcessDialog(String message){
		this.dialog = new MessageDialogWithToggle(null, 
				  Messages.getString("message.confirm"), 
				  null, 
				  message, 
				  MessageDialogWithToggle.QUESTION,
				  new String[]{Messages.getString("string.delete"),
							   Messages.getString("string.skip"),
							   Messages.getString("string.cancel")}, 
				  0,
				  Messages.getString("message.delete.confirm5"), 
				  false);
	}

	@Override
	public int open() {
		return dialog.open();
	}

	@Override
	public boolean getToggleState() {
		return dialog.getToggleState();
	}

}
