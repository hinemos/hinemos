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

package com.clustercontrol.accesscontrol.view.action;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.accesscontrol.action.GetRoleListTableDefine;
import com.clustercontrol.accesscontrol.dialog.RoleDialog;
import com.clustercontrol.accesscontrol.view.RoleListView;
import com.clustercontrol.accesscontrol.view.RoleSettingTreeView;

/**
 * アクセス[ロール]ビューの「変更」のアクションクラス<BR>
 *
 * @version 5.0.0
 * @since 2.0.0
 */
public class RoleModifyAction extends AbstractHandler implements IElementUpdater {
	private static Log m_log = LogFactory.getLog(RoleModifyAction.class);
	
	/** アクションID */
	public static final String ID = RoleModifyAction.class.getName();
	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
	}

	/**
	 * アクセス[ロール]ビューの「変更」のアクション<BR>
	 *
	 * アクセス[ロール]ビューの「変更」が押された場合に、<BR>
	 * アクセス[ロールの作成・変更]ダイアログを表示し、ユーザを変更します。
	 * <p>
	 * <ol>
	 * <li>アクセス[ロール]ビューから選択されているロールのロールIDを取得します。</li>
	 * <li>ロールIDが一致するロールのアクセス[ロールの作成・変更]ダイアログを表示します。</li>
	 * <li>アクセス[ロールの作成・変更]ダイアログからロール用プロパティを取得します。</li>
	 * <li>ロール用プロパティを元にロールを更新します。</li>
	 * <li>アクセス[ロール]ビューを更新します。</li>
	 * </ol>
	 *
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 * @see com.clustercontrol.accesscontrol.dialog.UserDialog
	 * @see com.clustercontrol.accesscontrol.view.UserListView
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// ロール一覧より、選択されているロールのロールIDを取得
		this.viewPart = HandlerUtil.getActivePart(event);
		RoleListView roleListView = null; 
		try { 
			roleListView = (RoleListView) this.viewPart.getAdapter(RoleListView.class); 
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (roleListView == null) {
			m_log.info("execute: role list view is null");
			return null;
		}

		StructuredSelection selection = (StructuredSelection) roleListView
				.getComposite().getTableViewer().getSelection();

		List<?> list = (List<?>) selection.getFirstElement();
		String managerName = null;
		String roleId = null;
		if (list != null) {
			roleId = (String) list.get(GetRoleListTableDefine.ROLE_ID);
			managerName = (String) list.get(GetRoleListTableDefine.MANAGER_NAME);
		}

		if (roleId != null) {
			// ダイアログを生成
			RoleDialog dialog = new RoleDialog(this.viewPart
					.getSite().getShell(), managerName, roleId, true);

			// ダイアログにて変更が選択された場合、入力内容をもって更新を行う。
			if (dialog.open() == IDialogConstants.OK_ID) {
				roleListView.update();

				//アクティブページを手に入れる
				IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow( event ).getActivePage();

				//ツリービューを更新する
				IViewPart roleTreeViewPart = page.findView(RoleSettingTreeView.ID);
				if (roleTreeViewPart != null) {
					RoleSettingTreeView treeView = (RoleSettingTreeView) roleTreeViewPart
							.getAdapter(RoleSettingTreeView.class);
					if (treeView == null) {
						m_log.info("execute: tree View is null");
						return null;
					}
					treeView.update();
				}
			}
		}
		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if( null != window ){
			IWorkbenchPage page = window.getActivePage();
			if( null != page ){
				IWorkbenchPart part = page.getActivePart();

				boolean editEnable = false;
				if(part instanceof RoleListView){
					// Enable button when 1 item is selected
					RoleListView view = (RoleListView)part;

					if(view.getSelectedNum() == 1) {
						editEnable = true;
					}
				}
				this.setBaseEnabled(editEnable);
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
