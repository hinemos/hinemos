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

package com.clustercontrol.notify.composite.action;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;

import com.clustercontrol.monitor.action.NotifyTableDefineNoCheckBox;
import com.clustercontrol.notify.view.action.NotifyModifyAction;

/**
 * 通知[一覧]ビュー用のテーブルビューア用のDoubleClickListenerクラスです。
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class NotifyDoubleClickListener implements IDoubleClickListener {

	/** 通知[一覧]ビュー用のコンポジット */
	private Composite m_composite;

	/**
	 * コンストラクタ
	 *
	 * @param composite 通知[一覧]ビュー用のコンポジット
	 */
	public NotifyDoubleClickListener(Composite composite) {
		m_composite = composite;
	}

	/**
	 * ダブルクリック時に呼び出されます。<BR>
	 * 通知[一覧]ビューのテーブルビューアをダブルクリックした際に、選択した行の内容をダイアログで表示します。
	 * <P>
	 * <ol>
	 * <li>イベントから選択行を取得し、選択行から通知IDを取得します。</li>
	 * <li>通知IDから通知情報を取得し、ダイアログで表示します。</li>
	 * </ol>
	 *
	 * @param event イベント
	 *
	 * @see com.clustercontrol.calendar.dialog.CalendarDialog
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		String managerName = null;
		String notifyId = null;
		Integer notifyType = null;

		//通知IDを取得
		if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
			ArrayList<?> info = (ArrayList<?>) ((StructuredSelection) event.getSelection()).getFirstElement();
			managerName = (String) info.get(NotifyTableDefineNoCheckBox.MANAGER_NAME);
			notifyId = (String) info.get(NotifyTableDefineNoCheckBox.NOTIFY_ID);
			notifyType = (Integer) info.get(NotifyTableDefineNoCheckBox.NOTIFY_TYPE);
		}

		if(notifyId != null && notifyType != null){
			// ダイアログ名を取得
			NotifyModifyAction action = new NotifyModifyAction();
			// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
			if (action.openDialog(m_composite.getShell(), managerName, notifyId, notifyType) ==
					IDialogConstants.OK_ID) {
				m_composite.update();
			}
		}
	}

}
