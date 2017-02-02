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

package com.clustercontrol.notify.mail.composite.actioin;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Table;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.notify.mail.action.GetMailTemplateListTableDefine;
import com.clustercontrol.notify.mail.composite.MailTemplateListComposite;
import com.clustercontrol.notify.mail.dialog.MailTemplateCreateDialog;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 監視設定[メールテンプレート]ビュー用のテーブルビューア用のDoubleClickListenerクラスです。
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class MailTemplateDoubleClickListener implements IDoubleClickListener {
	/** 監視設定[メールテンプレート]ビュー用のコンポジット */
	private MailTemplateListComposite m_composite;

	/**
	 * コンストラクタ
	 *
	 * @param composite 監視設定[メールテンプレート]ビュー用のコンポジット
	 */
	public MailTemplateDoubleClickListener(MailTemplateListComposite composite) {
		m_composite = composite;
	}

	/**
	 * ダブルクリック時に呼び出されます。<BR>
	 * 監視設定[メールテンプレート]ビューのテーブルビューアをダブルクリックした際に、選択した行の内容をダイアログで表示します。
	 * <P>
	 * <ol>
	 * <li>イベントから選択行を取得し、選択行からメールテンプレートIDを取得します。</li>
	 * <li>メールテンプレートIDからメールテンプレート情報を取得し、ダイアログで表示します。</li>
	 * </ol>
	 *
	 * @param event イベント
	 *
	 * @see com.clustercontrol.notify.mail.dialog.MailTemplateCreateDialog
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		String managerName = null;
		String mailTemplateId = null;

		//メールテンプレートIDを取得
		if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
			ArrayList<?> info = (ArrayList<?>) ((StructuredSelection) event
					.getSelection()).getFirstElement();
			managerName = (String) info.get(GetMailTemplateListTableDefine.MANAGER_NAME);
			mailTemplateId = (String) info.get(GetMailTemplateListTableDefine.MAIL_TEMPLATE_ID);
		}

		if(mailTemplateId != null){
			// ダイアログを生成
			MailTemplateCreateDialog dialog = new MailTemplateCreateDialog(
					m_composite.getShell(), managerName, mailTemplateId,
					PropertyDefineConstant.MODE_MODIFY);

			// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
			if (dialog.open() == IDialogConstants.OK_ID) {
				Table table = m_composite.getTableViewer().getTable();
				WidgetTestUtil.setTestId(this, null, table);
				int selectIndex = table.getSelectionIndex();
				m_composite.update();
				table.setSelection(selectIndex);

			}
		}
	}

}
