/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.composite.action;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.reporting.action.GetTemplateSetListTableDefine;
import com.clustercontrol.reporting.composite.TemplateSetListComposite;
import com.clustercontrol.reporting.dialog.TemplateSetDialog;

/**
 * レポーティング[テンプレートセット]ビュー用のテーブルビューア用のDoubleClickListenerクラスです。
 *
 * @version 5.0.a
 * @since 5.0.a
 */
public class TemplateSetDoubleClickListener implements IDoubleClickListener {
	/** レポーティング[テンプレートセット]ビュー用のコンポジット */
	private TemplateSetListComposite m_composite;

	/**
	 * コンストラクタ
	 *
	 * @param composite レポーティング[テンプレートセット]ビュー用のコンポジット
	 */
	public TemplateSetDoubleClickListener(TemplateSetListComposite composite) {
		m_composite = composite;
	}

	/**
	 * ダブルクリック時に呼び出されます。<BR>
	 * レポーティング[テンプレートセット]ビューのテーブルビューアをダブルクリックした際に、選択した行の内容をダイアログで表示します。
	 * <P>
	 * <ol>
	 * <li>イベントから選択行を取得し、選択行からテンプレートセットIDを取得します。</li>
	 * <li>テンプレートセットIDからテンプレートセット情報を取得し、ダイアログで表示します。</li>
	 * </ol>
	 *
	 * @param event イベント
	 *
	 * @see com.clustercontrol.reporting.dialog.TemplateSetDialog
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		String managerName = null;
		String templateSetId = null;

		//テンプレートセットIDを取得
		if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
			ArrayList<?> info = (ArrayList<?>) ((StructuredSelection) event
					.getSelection()).getFirstElement();
			managerName = (String) info.get(GetTemplateSetListTableDefine.MANAGER_NAME);
			templateSetId = (String) info.get(GetTemplateSetListTableDefine.TEMPLATE_SET_ID);
		}

		if(templateSetId != null){
			// ダイアログを生成
			TemplateSetDialog dialog = null;
			dialog = new TemplateSetDialog(m_composite.getShell(), managerName, templateSetId, PropertyDefineConstant.MODE_MODIFY);
			
			// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
			if (dialog.open() == IDialogConstants.OK_ID) {
				m_composite.update();
			}
		}
	}

}
