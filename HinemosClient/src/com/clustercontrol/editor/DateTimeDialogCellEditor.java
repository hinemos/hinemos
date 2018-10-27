/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
