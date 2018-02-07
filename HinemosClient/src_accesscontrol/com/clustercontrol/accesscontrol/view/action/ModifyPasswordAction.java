/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.view.action;

import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.accesscontrol.action.GetUserListTableDefine;
import com.clustercontrol.accesscontrol.dialog.ModifyPasswordDialog;
import com.clustercontrol.accesscontrol.util.AccessEndpointWrapper;
import com.clustercontrol.accesscontrol.view.UserListView;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.LoginManager;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.access.InvalidRole_Exception;

/**
 * アクセス[ユーザ]ビューの「パスワード変更」のアクションクラス
 *
 * @version 5.0.0
 * @since 2.0.0
 */
public class ModifyPasswordAction extends AbstractHandler implements IElementUpdater {
	private static Log m_log = LogFactory.getLog(ModifyPasswordAction.class);
	
	/** アクションID */
	public static final String ID = ModifyPasswordAction.class.getName();
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
	 * アクセス[ユーザ]ビューの「パスワード変更」が押された場合に、<BR>
	 * アクセス[パスワード変更]ダイアログを表示し、パスワードを変更します。
	 * <p>
	 * <ol>
	 * <li>アクセス[ユーザ]ビューから選択されているユーザのユーザIDを取得します。</li>
	 * <li>アクセス[パスワード変更]ダイアログを表示します。</li>
	 * <li>アクセス[パスワード変更]ダイアログからパスワードを取得します。</li>
	 * <li>パスワードの変更を行います。</li>
	 * </ol>
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.accesscontrol.dialog.ModifyPasswordDialog
	 * @see com.clustercontrol.accesscontrol.view.UserListView
	 * @see com.clustercontrol.accesscontrol.action.ModifyPassword#change(String, String)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);

		// ユーザ一覧より、選択されているユーザのUIDを取得
		UserListView view = null; 
		try { 
			view = (UserListView) this.viewPart.getAdapter(UserListView.class); 
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}
		
		if (view == null) {
			m_log.info("execute: view is null");
			return null;
		}
		
		StructuredSelection selection = (StructuredSelection) view
				.getComposite().getTableViewer().getSelection();

		List<?> list = (List<?>) selection.getFirstElement();
		String managerName = null;
		String uid = null;
		if (list != null) {
			managerName = (String) list.get(GetUserListTableDefine.MANAGER_NAME);
			uid = (String) list.get(GetUserListTableDefine.UID);
		}

		if (uid != null) {
			// ダイアログを生成
			ModifyPasswordDialog dialog =
					new ModifyPasswordDialog(this.viewPart.getSite().getShell());
			dialog.setUserid(uid);

			// ダイアログにて変更が選択された場合、入力内容をもって更新を行う。
			if (dialog.open() == IDialogConstants.OK_ID) {
				String password = dialog.getPassword();

				// 自分自身のユーザパスワードは変更可能とする
				try {
					boolean isLoginUser = EndpointManager.hasLoginUser(managerName, uid);

					//String passwordHash = CryptoUtil.createPasswordHash("MD5", CryptoUtil.BASE64_ENCODING, null, uid, password);
					String passwordHash = Base64.encodeBase64String(MessageDigest.getInstance("MD5").digest(password.getBytes()));
					AccessEndpointWrapper wrapper = AccessEndpointWrapper.getWrapper(managerName);
					if( isLoginUser ){
						wrapper.changeOwnPassword(passwordHash);
					}else{
						wrapper.changePassword(uid, passwordHash);
					}

					Object[] arg = {managerName};
					// 成功報告ダイアログを生成
					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.accesscontrol.14", arg));

					// 自分自身のユーザパスワード変更の場合はログアウトを促す
					if( isLoginUser ){
						MessageDialog.openInformation(
								null,
								Messages.getString("info"),
								Messages.getString("message.accesscontrol.25", arg));

						LoginManager.disconnect(managerName);
						return null;
					}

				} catch (Exception e) {
					String errMessage = "";
					if (e instanceof InvalidRole_Exception) {
						// 権限なし
						MessageDialog.openInformation(null, Messages.getString("message"),
								Messages.getString("message.accesscontrol.16"));
					} else {
						errMessage = ", " + HinemosMessage.replace(e.getMessage());
					}
					// 失敗報告ダイアログを生成
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.accesscontrol.15") + errMessage);
				}

				view.update();
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
