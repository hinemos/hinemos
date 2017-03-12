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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.widgets.Control;

import com.clustercontrol.dialog.DateTimeDialog;
import com.clustercontrol.util.TimezoneUtil;

/**
 * 日時ダイアログセルエディタークラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class DateTimeDialogCellEditor extends DialogCellEditor {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.DialogCellEditor#openDialogBox(org.eclipse.swt.widgets.Control)
	 */
	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		//日時ダイアログを表示
		DateTimeDialog dialog = new DateTimeDialog(cellEditorWindow.getShell());
		if (this.getValue() instanceof String) {
			SimpleDateFormat format = TimezoneUtil.getSimpleDateFormat();
			try {
				Date date = format.parse((String) this.getValue());
				dialog.setDate(date);
			} catch (ParseException e) {
				dialog.setDate(null);
			}
		}

		//選択した日時を取得する
		dialog.open();

		return dialog.getDate();
	}
}
