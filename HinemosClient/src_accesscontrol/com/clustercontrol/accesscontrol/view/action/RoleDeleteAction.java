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

import com.clustercontrol.accesscontrol.action.GetRoleListTableDefine;
import com.clustercontrol.accesscontrol.util.AccessEndpointWrapper;
import com.clustercontrol.accesscontrol.view.RoleListView;
import com.clustercontrol.accesscontrol.view.RoleSettingTreeView;
import com.clustercontrol.bean.PluginMessage;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.ws.access.InvalidRole_Exception;
import com.clustercontrol.ws.access.UnEditableRole_Exception;
import com.clustercontrol.ws.access.UsedFacility_Exception;
import com.clustercontrol.ws.access.UsedOwnerRole_Exception;
import com.clustercontrol.ws.access.UsedRole_Exception;

/**
 * アクセス[ロール]ビューの「削除」のアクションクラス<BR>
 *
 * @version 5.0.0
 * @since 2.0.0
 */
public class RoleDeleteAction extends AbstractHandler implements IElementUpdater {
	private static Log m_log = LogFactory.getLog(RoleDeleteAction.class);
	
	/** アクションID */
	public static final String ID = RoleDeleteAction.class.getName();
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
	 * アクセス[ロール]ビューの「削除」が押された場合に、<BR>
	 * ロールを削除します。
	 * <p>
	 * <ol>
	 * <li>アクセス[ロール]ビューから選択されているロールのロールIDを取得します。</li>
	 * <li>ロールIDが一致するロールを削除します。</li>
	 * <li>アクセス[ロール]ビューを更新します。</li>
	 * </ol>
	 *
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 * @see com.clustercontrol.accesscontrol.view.UserListView
	 * @see com.clustercontrol.accesscontrol.action.DeleteUserProperty#delete(String)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// ロール一覧より、選択されているロールのUIDを取得
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
		} else {
			StructuredSelection selection = (StructuredSelection) roleListView.getComposite().getTableViewer()
					.getSelection();

			List<?> list = (List<?>) selection.toList();

			Map<String, List<String>> map = new ConcurrentHashMap<String, List<String>>();
			for (Object obj : list) {
				List<?> objList = (List<?>) obj;
				String managerName = (String) objList.get(GetRoleListTableDefine.MANAGER_NAME);
				if (map.get(managerName) == null) {
					map.put(managerName, new ArrayList<String>());
				}
			}

			int size = 0;
			String id = null;
			for (Object obj : list) {
				List<?> objList = (List<?>) obj;
				String managerName = (String) objList.get(GetRoleListTableDefine.MANAGER_NAME);
				String roleId = (String) objList.get(GetRoleListTableDefine.ROLE_ID);
				map.get(managerName).add(roleId);
				id = roleId;
				size++;
			}

			Map<String, String> errorMsgs = new ConcurrentHashMap<>();
			String[] args = new String[1];
			String msg = null;

			// 確認ダイアログにて変更が選択された場合、削除処理を行う。
			if (size == 1) {
				msg = "message.accesscontrol.32";
				args[0] = id;
			} else {
				msg = "message.accesscontrol.61";
				args[0] = Integer.toString(size);
			}

			if (MessageDialog.openConfirm(null, Messages.getString("confirmed"),
					Messages.getString(msg, args)) == false) {
				return null;
			}

			StringBuffer messageArg = new StringBuffer();
			int i = 0;
			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
				List<String> roleIdList = entry.getValue();
				if (roleIdList.isEmpty()) {
					continue;
				}
				String managerName = entry.getKey();
				AccessEndpointWrapper wrapper = AccessEndpointWrapper.getWrapper(managerName);
				if (i > 0) {
					messageArg.append(", ");
				}
				messageArg.append(managerName);
				try {
					// 削除処理
					wrapper.deleteRoleInfo(roleIdList);
				} catch (UsedFacility_Exception e) {
					// TODO
					// UsedFacilityのメンバ変数のfacilityIdを追加すること。
					String roleId = roleIdList.get(0);

					// ロールIDのスコープが使用されている場合のエラーダイアログを表示する
					Object[] errorArgs = { roleId, PluginMessage.typeToString(e.getFaultInfo().getPlugin()) };

					errorMsgs.put(managerName, Messages.getString("message.repository.27", errorArgs));
				} catch (UsedOwnerRole_Exception e) {
					String roleId = e.getFaultInfo().getRoleId();

					// ロールがオーナーロールとして使用されている場合はエラー
					Object[] errorArgs = { roleId,
							PluginMessage.typeToString(((UsedOwnerRole_Exception) e).getFaultInfo().getPlugin()) };
					errorMsgs.put(managerName, Messages.getString("message.accesscontrol.52", errorArgs));
				} catch (Exception e) {
					String errMessage = "";
					if (e instanceof InvalidRole_Exception) {
						// 権限なし
						errorMsgs.put(managerName, Messages.getString("message.accesscontrol.16"));
					} else if (e instanceof UsedRole_Exception) {
						// ロールに所属するユーザが存在する場合はエラー
						errorMsgs.put(managerName, Messages.getString("message.accesscontrol.42"));
					} else if (e instanceof UnEditableRole_Exception) {
						// 削除不可のロールを削除する場合はエラー（システムロール、内部モジュール用ロール）
						errorMsgs.put(managerName, Messages.getString("message.accesscontrol.41"));
					} else {
						errMessage = ", " + HinemosMessage.replace(e.getMessage());
					}

					// 上記以外の例外
					errorMsgs.put(managerName, Messages.getString("message.accesscontrol.31") + errMessage);
				}
				i++;
			}

			// メッセージ表示
			if (0 < errorMsgs.size()) {
				UIManager.showMessageBox(errorMsgs, true);
			} else {
				Object[] arg = { messageArg.toString() };
				// 完了メッセージ
				MessageDialog.openInformation(null, Messages.getString("successful"),
						Messages.getString("message.accesscontrol.30", arg));
			}

			// ビューを更新
			roleListView.update();
		}

		//アクティブページを手に入れる
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow( event ).getActivePage();

		//ツリービューを更新する
		IViewPart roleTreeViewPart = page.findView(RoleSettingTreeView.ID);
		if (roleTreeViewPart != null) {
			RoleSettingTreeView treeView = (RoleSettingTreeView) roleTreeViewPart
					.getAdapter(RoleSettingTreeView.class);
			if (treeView == null) {
				m_log.info("execute: tree View is null");
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
				if(part instanceof RoleListView){
					// Enable button when 1 item is selected
					RoleListView view = (RoleListView)part;

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
