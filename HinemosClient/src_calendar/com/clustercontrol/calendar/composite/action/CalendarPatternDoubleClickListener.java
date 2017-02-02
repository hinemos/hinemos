/*

Copyright (C) 2013 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.calendar.composite.action;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.calendar.action.GetCalendarPatternTableDefine;
import com.clustercontrol.calendar.composite.CalendarPatternComposite;
import com.clustercontrol.calendar.dialog.CalendarPatternDialog;

/**
 * カレンダ[カレンダパターン]ビュー用のテーブルビューア用のDoubleClickListenerクラスです。
 *
 * @version 4.1.0
 * @since 4.1.0
 */
public class CalendarPatternDoubleClickListener implements IDoubleClickListener {
	/** カレンダ[カレンダパターン]ビュー用のコンポジット */
	private CalendarPatternComposite m_composite;

	/**
	 * コンストラクタ
	 *
	 * @param composite カレンダ[カレンダパターン]ビュー用のコンポジット
	 */
	public CalendarPatternDoubleClickListener(CalendarPatternComposite composite) {
		m_composite = composite;
	}

	/**
	 * ダブルクリック時に呼び出されます。<BR>
	 * カレンダ[カレンダパターン]ビューのテーブルビューアをダブルクリックした際に、
	 * 選択した行の内容をダイアログで表示します。
	 * <P>
	 * <ol>
	 * <li>イベントから選択行を取得し、選択行からカレンダパターンIDを取得します。</li>
	 * <li>カレンダIDからカレンダ[カレンダパターン]情報を取得し、ダイアログで表示します。</li>
	 * </ol>
	 *
	 * @param event イベント
	 *
	 * @see com.clustercontrol.calendar.dialog.CalendarPatternDialog
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		String managerName = null;
		String id = null;

		//カレンダパターンIDを取得
		if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
			ArrayList<?> info = (ArrayList<?>) ((StructuredSelection) event
					.getSelection()).getFirstElement();
			managerName = (String) info.get(GetCalendarPatternTableDefine.MANAGER_NAME);
			id = (String) info.get(GetCalendarPatternTableDefine.CAL_PATTERN_ID);
		}

		if(id != null){

			// ダイアログを生成
			CalendarPatternDialog dialog = null;

			dialog = new CalendarPatternDialog(m_composite.getShell(), managerName, id,PropertyDefineConstant.MODE_MODIFY);

			// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
			if (dialog.open() == IDialogConstants.OK_ID) {
				m_composite.update();
			}
		}
	}

}
