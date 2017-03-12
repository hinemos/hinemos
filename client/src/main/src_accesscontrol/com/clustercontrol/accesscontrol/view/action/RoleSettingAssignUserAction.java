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

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.accesscontrol.bean.RoleSettingTreeConstant;
import com.clustercontrol.accesscontrol.dialog.RoleSettingDialog;
import com.clustercontrol.accesscontrol.view.RoleSettingTreeView;
import com.clustercontrol.accesscontrol.view.SystemPrivilegeListView;
import com.clustercontrol.ws.access.RoleInfo;
import com.clustercontrol.ws.accesscontrol.RoleTreeItem;

/**
 * アクセス権限[ロール設定]ビューの「ユーザの選択」のアクションクラス<BR>
 *
 * @version 5.0.0
 * @since 2.0.0
 */
public class RoleSettingAssignUserAction extends AbstractHandler implements IElementUpdater {
	private static Log m_log = LogFactory.getLog(RoleSettingAssignUserAction.class);
	
	/** アクションID */
	public static final String ID = RoleSettingAssignUserAction.class.getName();
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
	 * アクセス[ロール設定]ビューの「ユーザの選択」のアクション<BR>
	 *
	 * アクセス[ロール設定]ビューの「ユーザの選択」が押された場合に、<BR>
	 * アクセス[ユーザの選択]ダイアログを表示し、ユーザを変更します。
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.accesscontrol.dialog.UserDialog
	 * @see com.clustercontrol.accesscontrol.view.UserListView
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// ロール設定より、選択されているロールのロールIDを取得

		this.viewPart = HandlerUtil.getActivePart(event);
		RoleSettingTreeView roleSettingTreeView = null; 
		try { 
			roleSettingTreeView = (RoleSettingTreeView) this.viewPart.getAdapter(RoleSettingTreeView.class); 
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (roleSettingTreeView == null) {
			m_log.info("execute: role setting tree view is null");
			return null;
		}

		StructuredSelection selection = (StructuredSelection) roleSettingTreeView
				.getTreeComposite().getTreeViewer().getSelection();

		RoleTreeItem item = (RoleTreeItem) selection.getFirstElement();
		Object data = item.getData();
		RoleTreeItem manager = RoleSettingTreeView.getManager(item);
		String managerName = ((RoleInfo)manager.getData()).getRoleName();

		if (data instanceof RoleInfo
				&& !((RoleInfo)data).getRoleId().equals(RoleSettingTreeConstant.ROOT_ID)
				&& !((RoleInfo)data).getRoleId().equals(RoleSettingTreeConstant.MANAGER)) {
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow( event );
			RoleSettingDialog dialog = new RoleSettingDialog(
					window.getShell(),
					managerName,
					((RoleInfo)data).getRoleId());
			//ダイアログ表示
			if (dialog.open() == IDialogConstants.OK_ID) {
				roleSettingTreeView.update();

				//アクティブページを手に入れる
				IWorkbenchPage page = window.getActivePage();

				//システム権限ビューを更新する
				IViewPart systemPrivilegeListView = page.findView(SystemPrivilegeListView.ID);
				if (systemPrivilegeListView != null) {
					SystemPrivilegeListView listView = (SystemPrivilegeListView) systemPrivilegeListView
							.getAdapter(SystemPrivilegeListView.class);
					if (listView == null) {
						m_log.info("execute: list view is null");
						return null;
					}
					listView.update();
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
				if(part instanceof RoleSettingTreeView){
					// Enable button when 1 item is selected
					RoleSettingTreeView view = (RoleSettingTreeView)part;

					if(RoleIdConstant.ADMINISTRATORS.equals(view.getRoleId())) {
						editEnable = true;
					} else if(view.getRoleId() == null){
						editEnable = false;
					} else {
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
