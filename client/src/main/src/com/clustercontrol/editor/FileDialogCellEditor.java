/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.editor;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.widgets.Control;
//import org.eclipse.swt.widgets.FileDialog;

/**
 * ファイルダイアログセルエディタークラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class FileDialogCellEditor extends DialogCellEditor {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.DialogCellEditor#openDialogBox(org.eclipse.swt.widgets.Control)
	 */
	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		//ファイルダイアログを表示
		//FileDialog dialog = new FileDialog(cellEditorWindow.getShell(),
		//		SWT.OPEN);
		//選択したファイルパスを取得する
		//String file = dialog.open();
		return "";
	}
}
