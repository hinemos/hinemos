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

package com.clustercontrol.accesscontrol.composite.action;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.accesscontrol.action.GetRoleListTableDefine;
import com.clustercontrol.accesscontrol.composite.RoleListComposite;
import com.clustercontrol.accesscontrol.dialog.RoleDialog;
import com.clustercontrol.accesscontrol.view.RoleListView;
import com.clustercontrol.accesscontrol.view.RoleSettingTreeView;

/**
 * アカウント[ロール]ビュー用のテーブルビューア用のDoubleClickListenerクラスです。
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class RoleDoubleClickListener implements IDoubleClickListener {
	/** ログ */
	private static Log m_log = LogFactory.getLog(RoleDoubleClickListener.class);
	/** アカウント[ロール]ビュー用のコンポジット */
	private RoleListComposite m_composite;

	/**
	 * コンストラクタ
	 *
	 * @param composite アカウント[ロール]ビュー用のコンポジット
	 */
	public RoleDoubleClickListener(RoleListComposite composite) {
		m_composite = composite;
	}

	/**
	 * ダブルクリック時に呼び出されます。<BR>
	 * アカウント[ロール]ビューのテーブルビューアをダブルクリックした際に、選択した行の内容をダイアログで表示します。
	 * <P>
	 * <ol>
	 * <li>イベントから選択行を取得し、選択行からロールIDを取得します。</li>
	 * <li>ロールIDからロール情報を取得し、ダイアログで表示します。</li>
	 * </ol>
	 *
	 * @param event イベント
	 *
	 * @see com.clustercontrol.accesscontrol.dialog.RoleDialog
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		String roleId = null;
		String managerName = null;

		//ロールIDを取得
		if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
			ArrayList<?> info = (ArrayList<?>) ((StructuredSelection) event
					.getSelection()).getFirstElement();
			roleId = (String) info.get(GetRoleListTableDefine.ROLE_ID);
			managerName = (String)info.get(GetRoleListTableDefine.MANAGER_NAME);
		}

		if(roleId != null){
			// ダイアログを生成
			RoleDialog dialog = new RoleDialog(m_composite.getShell(), managerName, roleId, true);

			// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
			if (dialog.open() == IDialogConstants.OK_ID) {
				//アクティブページを手に入れる
				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				//ビューを更新する
				IViewPart roleTreeViewPart = page.findView(RoleSettingTreeView.ID);
				if (roleTreeViewPart != null) {
					RoleSettingTreeView treeView = (RoleSettingTreeView) roleTreeViewPart
							.getAdapter(RoleSettingTreeView.class);
					if (treeView == null) {
						m_log.info("double click: tree view is null");
					} else {
						treeView.update();
					}
				}
				//ビューを更新する
				IViewPart roleListViewPart = page.findView(RoleListView.ID);
				if (roleListViewPart != null) {
					RoleListView roleListView = (RoleListView) roleListViewPart
							.getAdapter(RoleListView.class);
					if (roleListView == null) {
						m_log.info("double click: role list view is null");
					} else {
						roleListView.update();
					}
				}
			}
		}
	}

}
