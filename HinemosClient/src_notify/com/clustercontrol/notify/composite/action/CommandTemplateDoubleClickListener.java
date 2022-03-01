/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.composite.action;

import java.util.ArrayList;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;

import com.clustercontrol.notify.action.GetCommandTemplateTableDefine;
import com.clustercontrol.notify.view.action.CommandTemplateShowAction;

/**
 * 監視設定[コマンド通知テンプレート]ビュー用のテーブルビューア用のDoubleClickListenerクラスです。
 */
public class CommandTemplateDoubleClickListener implements IDoubleClickListener {

	/** 監視設定[コマンド通知テンプレート]ビュー用のコンポジット */
	private Composite m_composite;

	/**
	 * コンストラクタ
	 *
	 * @param composite 監視設定[コマンド通知テンプレート]ビュー用のコンポジット
	 */
	public CommandTemplateDoubleClickListener(Composite composite) {
		m_composite = composite;
	}

	/**
	 * ダブルクリック時に呼び出されます。<BR>
	 * 監視設定[コマンド通知テンプレート]ビューのテーブルビューアをダブルクリックした際に、選択した行の内容をダイアログで表示します。
	 * <P>
	 * <ol>
	 * <li>イベントから選択行を取得し、選択行からコマンド通知テンプレートIDを取得します。</li>
	 * <li>コマンド通知テンプレートIDから通知情報を取得し、ダイアログで表示します。</li>
	 * </ol>
	 *
	 * @param event イベント
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		String managerName = null;
		String commandTemplateId = null;

		//コマンド通知テンプレートIDを取得
		Object element = ((StructuredSelection) event.getSelection()).getFirstElement();
		if (element != null) {
			ArrayList<?> info = (ArrayList<?>) element;
			managerName = (String) info.get(GetCommandTemplateTableDefine.MANAGER_NAME);
			commandTemplateId = (String) info.get(GetCommandTemplateTableDefine.COMMAND_TEMPLATE_ID);
		}

		if(commandTemplateId != null){
			// ダイアログ名を取得
			CommandTemplateShowAction action = new CommandTemplateShowAction();
			action.openDialog(m_composite.getShell(), managerName, commandTemplateId);
		}
	}

}
