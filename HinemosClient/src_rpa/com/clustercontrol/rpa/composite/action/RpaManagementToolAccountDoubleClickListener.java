/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.composite.action;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.rpa.composite.RpaManagementToolAccountListComposite;
import com.clustercontrol.rpa.composite.RpaManagementToolAccountListComposite.RpaManagementToolAccountViewColumn;
import com.clustercontrol.rpa.dialog.RpaManagementToolAccountDialog;

/**
 * RPA管理ツールアカウントビューテーブルビューア用のDoubleClickListenerクラスです。
 *
 */
public class RpaManagementToolAccountDoubleClickListener implements IDoubleClickListener {
	/** RPA管理ツールアカウントビュー用のコンポジット */
	private RpaManagementToolAccountListComposite m_composite;

	/**
	 * コンストラクタ
	 *
	 * @param composite RPA管理ツールアカウントビュー用のコンポジット
	 */
	public RpaManagementToolAccountDoubleClickListener(RpaManagementToolAccountListComposite composite) {
		m_composite = composite;
	}

	/**
	 * ダブルクリック時に呼び出されます。
	 * RPAシナリオタグビューのテーブルビューアをダブルクリックした際に、選択した行の内容をダイアログで表示します。
	 * <P>
	 * <ol>
	 * <li>イベントから選択行を取得し、選択行からRPAシナリオタグIDを取得します。</li>
	 * <li>RPAシナリオタグIDからRPAシナリオタグ情報を取得し、ダイアログで表示します。</li>
	 * </ol>
	 *
	 * @param event イベント
	 *
	 * @see com.clustercontrol.rpa.dialog.RpaScenarioTagDialog
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		String managerName = null;
		String rpaScopeId = null;

		//RPAスコープIDを取得
		if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
			RpaManagementToolAccountViewColumn info = (RpaManagementToolAccountViewColumn) ((StructuredSelection) event
					.getSelection()).getFirstElement();
			managerName = info.getManagerName();
			rpaScopeId = info.getRpaManagementToolAccount().getRpaScopeId();
		}

		if(rpaScopeId != null){
			// ダイアログを生成
			RpaManagementToolAccountDialog dialog = 
					new RpaManagementToolAccountDialog(m_composite.getShell(), managerName, rpaScopeId, PropertyDefineConstant.MODE_MODIFY);
			
			// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
			if (dialog.open() == IDialogConstants.OK_ID) {
				m_composite.update();
			}
		}
	}

}
