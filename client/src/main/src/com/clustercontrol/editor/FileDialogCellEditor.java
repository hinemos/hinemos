/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
