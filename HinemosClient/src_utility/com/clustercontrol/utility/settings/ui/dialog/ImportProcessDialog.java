/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.dialog;

/**
 * Utility用確認ダイアログのWrapper
 * 
 * TODO クラス名がわかりにくいため、次期でUtilityProcessDialogなどにリネームする
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
public interface ImportProcessDialog {

	/**
	 * 
	 * @see org.eclipse.jface.dialogs.MessageDialogWithToggle#open()
	 */
	public int open();

	/**
	 * 
	 * @see org.eclipse.jface.dialogs.MessageDialogWithToggle#getToggleState()
	 */
	public boolean getToggleState();
}
