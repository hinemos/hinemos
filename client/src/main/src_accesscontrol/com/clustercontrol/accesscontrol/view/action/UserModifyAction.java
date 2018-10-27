/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.accesscontrol.action.GetUserListTableDefine;
import com.clustercontrol.accesscontrol.dialog.UserDialog;
import com.clustercontrol.accesscontrol.util.AccessEndpointWrapper;
import com.clustercontrol.accesscontrol.view.RoleSettingTreeView;
import com.clustercontrol.accesscontrol.view.UserListView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.access.InvalidRole_Exception;
import com.clustercontrol.ws.access.UserInfo;

/**
 * アクセス[ユーザ]ビューの「変更」のアクションクラス<BR>
 *
 * @version 5.0.0
 * @since 2.0.0
 */
public class UserModifyAction extends AbstractHandler implements IElementUpdater {
	private static Log m_log = LogFactory.getLog(UserModifyAction.class);
	
	/** アクションID */
	public static final String ID = UserModifyAction.class.getName();
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
	 * アクセス[ユーザ]ビューの「変更」のアクション<BR>
	 *
	 * アクセス[ユーザ]ビューの「変更」が押された場合に、<BR>
	 * アクセス[ユーザの作成・変更]ダイアログを表示し、ユーザを変更します。
	 * <p>
	 * <ol>
	 * <li>アクセス[ユーザ]ビューから選択されているユーザのユーザIDを取得します。</li>
	 * <li>ユーザIDが一致するユーザのアクセス[ユーザの作成・変更]ダイアログを表示します。</li>
	 * <li>アクセス[ユーザの作成・変更]ダイアログからユーザ用プロパティを取得します。</li>
	 * <li>ユーザ用プロパティを元にユーザを更新します。</li>
	 * <li>アクセス[ユーザ]ビューを更新します。</li>
	 * </ol>
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.accesscontrol.dialog.UserDialog
	 * @see com.clustercontrol.accesscontrol.view.UserListView
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		UserListView userListView = null; 
		try { 
			userListView = (UserListView) this.viewPart.getAdapter(UserListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (userListView == null) {
			m_log.info("execute: user list view is null");
			return null;
		}

		// ユーザ一覧より、選択されているユーザのUIDを取得
		StructuredSelection selection = (StructuredSelection) userListView
				.getComposite().getTableViewer().getSelection();

		List<?> list = (List<?>) selection.getFirstElement();
		String managerName = null;
		String uid = null;
		if (list != null) {
			managerName = (String) list.get(GetUserListTableDefine.MANAGER_NAME);
			uid = (String) list.get(GetUserListTableDefine.UID);
		}

		if (uid != null) {
			// ユーザIDが指定されている場合、その情報を初期化する。
			UserInfo info = null;
			try {
				AccessEndpointWrapper wrapper = AccessEndpointWrapper.getWrapper(managerName);
				info = wrapper.getUserInfo(uid);
			} catch (InvalidRole_Exception e) {
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
				return null;
			} catch (Exception e) {
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
				return null;
			}
			
			// ダイアログを生成
			UserDialog dialog = new UserDialog(this.viewPart
					.getSite().getShell(), managerName, info, true);

			// ダイアログにて変更が選択された場合、入力内容をもって更新を行う。
			if (dialog.open() == IDialogConstants.OK_ID) {
				userListView.update();

				//アクティブページを手に入れる
				IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow( event ).getActivePage();

				//ツリービューを更新する
				IViewPart roleTreeViewPart = page.findView(RoleSettingTreeView.ID);
				if (roleTreeViewPart != null) {
					RoleSettingTreeView treeView = (RoleSettingTreeView) roleTreeViewPart
							.getAdapter(RoleSettingTreeView.class);
					if (treeView == null) {
						m_log.info("execute: tree view is null");
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
				if(part instanceof UserListView){
					// Enable button when 1 item is selected
					UserListView view = (UserListView)part;

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
