/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.dialog;

import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.util.Messages;

/**
 * インポート処理方法の確認ダイアログ
 * 
 * @version 6.1.0
 * @since 2.2.0
 * 
 *
 */
public class DeleteProcessDialog extends MessageDialogWithToggle {
	public static final int DELETE = 256;
	public static final int SKIP = 257;
	public static final int CANCEL = 258;

	/**
	 * コンストラクタ
	 * 
	 * @param parent 親シェル
	 * @param message メッセージ
	 */
	public DeleteProcessDialog(Shell parent, String message) {
		super(parent, 
			  Messages.getString("message.confirm"), 
			  null, 
			  message, 
			  QUESTION, 
			  new String[]{Messages.getString("string.delete"),
						   Messages.getString("string.skip"),
						   Messages.getString("string.cancel")}, 
			  0,
			  Messages.getString("message.delete.confirm5"), 
			  false);
	}
}
