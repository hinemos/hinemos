/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.dialog;

/**
 * インポート処理方法の確認ダイアログWrapper(Rich/Webクライアント用)
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
public class ImportProcessClientDialog implements IUtilityDialog{
	private UtilityProcessDialog dialog;

	public ImportProcessClientDialog(String message){
		dialog = UtilityDialogInjector.createDeleteProcessDialog(null, message);
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
