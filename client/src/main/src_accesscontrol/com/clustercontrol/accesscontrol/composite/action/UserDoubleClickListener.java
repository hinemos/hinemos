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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.accesscontrol.action.GetUserListTableDefine;
import com.clustercontrol.accesscontrol.composite.UserListComposite;
import com.clustercontrol.accesscontrol.dialog.UserDialog;
import com.clustercontrol.accesscontrol.util.AccessEndpointWrapper;
import com.clustercontrol.accesscontrol.view.RoleSettingTreeView;
import com.clustercontrol.accesscontrol.view.UserListView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.access.InvalidRole_Exception;
import com.clustercontrol.ws.access.UserInfo;

/**
 * アクセス[ユーザ]ビュー用のテーブルビューア用のDoubleClickListenerクラスです。
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class UserDoubleClickListener implements IDoubleClickListener {
	/** ログ */
	private static Log m_log = LogFactory.getLog(UserDoubleClickListener.class);
	/** アクセス[ユーザ]ビュー用のコンポジット */
	private UserListComposite m_composite;

	/**
	 * コンストラクタ
	 *
	 * @param composite アクセス[ユーザ]ビュー用のコンポジット
	 */
	public UserDoubleClickListener(UserListComposite composite) {
		m_composite = composite;
	}

	/**
	 * ダブルクリック時に呼び出されます。<BR>
	 * アクセス[ユーザ]ビューのテーブルビューアをダブルクリックした際に、選択した行の内容をダイアログで表示します。
	 * <P>
	 * <ol>
	 * <li>イベントから選択行を取得し、選択行からユーザIDを取得します。</li>
	 * <li>ユーザIDからユーザ情報を取得し、ダイアログで表示します。</li>
	 * </ol>
	 *
	 * @param event イベント
	 *
	 * @see com.clustercontrol.accesscontrol.dialog.UserDialog
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		String managerName = null;
		String uid = null;

		//UIDを取得
		if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
			ArrayList<?> info = (ArrayList<?>) ((StructuredSelection) event
					.getSelection()).getFirstElement();
			managerName = (String) info.get(GetUserListTableDefine.MANAGER_NAME);
			uid = (String) info.get(GetUserListTableDefine.UID);
		}

		if(uid != null){
			// ダイアログを生成
			UserInfo info = null;
			try {
				AccessEndpointWrapper wrapper = AccessEndpointWrapper.getWrapper(managerName);
				info = wrapper.getUserInfo(uid);
			} catch (InvalidRole_Exception e) {
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
				return;
			} catch (Exception e) {
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
				return;
			}
			
			UserDialog dialog = new UserDialog(m_composite.getShell(), managerName, info, true);

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
				IViewPart userListViewPart = page.findView(UserListView.ID);
				if (userListViewPart != null) {
					UserListView userListView = (UserListView) userListViewPart
							.getAdapter(UserListView.class);
					if (userListView == null) {
						m_log.info("double click: user list view is null");
					} else {
						userListView.update();
					}
				}
			}
		}
	}

}
