/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.utility.settings.ui.dialog;

import org.eclipse.jface.dialogs.MessageDialogWithToggle;

import com.clustercontrol.util.Messages;

public class ClientImportContinueDialog implements UtilityProcessDialog {
	private MessageDialogWithToggle dialog;

	public ClientImportContinueDialog(String message){
		this.dialog = new MessageDialogWithToggle(null, 
				Messages.getString("message.confirm"), 
				null, 
				message, 
				MessageDialogWithToggle.QUESTION,
					new String[]{"Yes","No"},
				0,
				Messages.getString("message.import.confirm8"), 
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
