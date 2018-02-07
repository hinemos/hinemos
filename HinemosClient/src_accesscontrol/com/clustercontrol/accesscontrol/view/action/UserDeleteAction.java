/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.view.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
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
import com.clustercontrol.accesscontrol.util.AccessEndpointWrapper;
import com.clustercontrol.accesscontrol.view.RoleSettingTreeView;
import com.clustercontrol.accesscontrol.view.UserListView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.ws.access.InvalidRole_Exception;
import com.clustercontrol.ws.access.UnEditableUser_Exception;
import com.clustercontrol.ws.access.UsedUser_Exception;

/**
 * アクセス[ユーザ]ビューの「削除」のアクションクラス<BR>
 *
 * @version 5.0.0
 * @since 2.0.0
 */
public class UserDeleteAction extends AbstractHandler implements IElementUpdater {
	private static Log m_log = LogFactory.getLog(UserDeleteAction.class);
	
	/** アクションID */
	public static final String ID = UserDeleteAction.class.getName();
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
	 * アクセス[ユーザ]ビューの「削除」が押された場合に、<BR>
	 * ユーザを削除します。
	 * <p>
	 * <ol>
	 * <li>アクセス[ユーザ]ビューから選択されているユーザのユーザIDを取得します。</li>
	 * <li>ユーザIDが一致するユーザを削除します。</li>
	 * <li>アクセス[ユーザ]ビューを更新します。</li>
	 * </ol>
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.accesscontrol.view.UserListView
	 * @see com.clustercontrol.accesscontrol.action.DeleteUserProperty#delete(String)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);

		// ユーザ一覧より、選択されているユーザのUIDを取得
		UserListView userListView = null; 
		try { 
			userListView = (UserListView) this.viewPart.getAdapter(UserListView.class); 
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (userListView == null) {
			m_log.info("execute: user list view is null");
		} else {
			StructuredSelection selection = (StructuredSelection) userListView
					.getComposite().getTableViewer().getSelection();

			List<?> list = (List<?>)selection.toList();

			Map<String, List<String>> map = new ConcurrentHashMap<String, List<String>>();
			for(Object obj : list) {
				List<?> objList = (List<?>)obj;
				String managerName = (String) objList.get(GetUserListTableDefine.MANAGER_NAME);
				if(map.get(managerName) == null) {
					map.put(managerName, new ArrayList<String>());
				}
			}

			int size = 0;
			String id = null;
			for(Object obj : list) {
				List<?> objList = (List<?>)obj;
				String managerName = (String) objList.get(GetUserListTableDefine.MANAGER_NAME);
				String uid = (String) objList.get(GetUserListTableDefine.UID);
				map.get(managerName).add(uid);
				id = uid;
				size++;
			}

			Map<String, String> errorMsgs = new ConcurrentHashMap<>();
			String[] args = new String[1];
			String msg = null;

			// 確認ダイアログにて変更が選択された場合、削除処理を行う。
			if(size == 1) {
				msg = "message.accesscontrol.13";
				args[0] = id;
			} else {
				msg = "message.accesscontrol.62";
				args[0] = Integer.toString(size);
			}

			if (MessageDialog.openConfirm(
					null,
					Messages.getString("confirmed"),
					Messages.getString(msg, args)) == false) {
				return null;
			}


			StringBuffer messageArg = new StringBuffer();
			int i = 0;
			for(Map.Entry<String, List<String>> entry : map.entrySet()) {
				List<String> uidList = entry.getValue();
				if (uidList.isEmpty()) {
					continue;
				}
				String managerName = entry.getKey();
				AccessEndpointWrapper wrapper = AccessEndpointWrapper.getWrapper(managerName);

				if(i > 0) {
					messageArg.append(", ");
				}
				messageArg.append(managerName);

				try {
					// 削除処理
					wrapper.deleteUserInfo(uidList);
				} catch (Exception e) {
					String errMessage = "";
					if (e instanceof InvalidRole_Exception) {
						// 権限なし
						errorMsgs.put(managerName, Messages.getString("message.accesscontrol.16"));
					} else if (e instanceof UsedUser_Exception) {
						// 現在ログインしているユーザは削除できない
						errorMsgs.put(managerName, Messages.getString("message.accesscontrol.37"));
					} else if (e instanceof UnEditableUser_Exception) {
						// 削除不可なユーザの場合は削除できない。（システムユーザ、内部モジュール用ユーザ
						errorMsgs.put(managerName, Messages.getString("message.accesscontrol.39"));
					} else {
						// 上記以外の例外
						errMessage = ", " + HinemosMessage.replace(e.getMessage());
						errorMsgs.put(managerName, Messages.getString("message.accesscontrol.12") + errMessage);
					}
				}
				i++;
			}

			//メッセージ表示
			if( 0 < errorMsgs.size() ){
				UIManager.showMessageBox(errorMsgs, true);
			} else {
				Object[] arg = {messageArg.toString()};
				// 完了メッセージ
				MessageDialog.openInformation(
						null,
						Messages.getString("successful"),
						Messages.getString("message.accesscontrol.11", arg));
			}

			// ビューを更新
			userListView.update();
		}

		//アクティブページを手に入れる
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow( event ).getActivePage();

		//ツリービューを更新する
		IViewPart roleTreeViewPart = page.findView(RoleSettingTreeView.ID);
		if (roleTreeViewPart != null) {
			RoleSettingTreeView treeView = (RoleSettingTreeView) roleTreeViewPart
					.getAdapter(RoleSettingTreeView.class);
			if (treeView == null) {
				m_log.info("execute: tree view is null");
			} else {
				treeView.update();
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

					if(view.getSelectedNum() > 0) {
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
