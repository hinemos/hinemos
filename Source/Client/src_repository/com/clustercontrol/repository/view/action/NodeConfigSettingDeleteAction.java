/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.view.action;

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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.repository.action.DeleteNodeConfigSetting;
import com.clustercontrol.repository.action.GetNodeConfigSettingListTableDefine;
import com.clustercontrol.repository.view.NodeConfigSettingListView;
import com.clustercontrol.util.Messages;

/**
 * 構成情報収集設定の削除を行うクライアント側アクションクラス<BR>
 *
 * @version 6.2.0
 * @since 6.2.0
 */
public class NodeConfigSettingDeleteAction extends AbstractHandler implements IElementUpdater {
	public static final String ID = NodeConfigSettingDeleteAction.class.getName();

	//	 ----- instance フィールド ----- //
	/** ログ */
	private static Log m_log = LogFactory.getLog(NodeConfigSettingDeleteAction.class);

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
	 * @see org.eclipse.core.commands.IHandler#execute
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.viewPart = HandlerUtil.getActivePart(event);

		// ノード一覧より、選択されているノードのファシリティIDを取得
		NodeConfigSettingListView nodeConfigSettingListView = null;
		try {
			nodeConfigSettingListView = (NodeConfigSettingListView) this.viewPart
					.getAdapter(NodeConfigSettingListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (nodeConfigSettingListView == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		StructuredSelection selection = (StructuredSelection) nodeConfigSettingListView
				.getComposite().getTableViewer().getSelection();

		List<?> sList = (List<?>) selection.toList();
		Map<String, List<String>> map = new ConcurrentHashMap<String, List<String>>();

		for (Object obj : sList) {
			List<?> list = (List<?>)obj;
			String managerName = null;
			if (list != null) {
				managerName = (String) list.get(GetNodeConfigSettingListTableDefine.MANAGER_NAME);
				if(map.get(managerName) == null) {
					map.put(managerName, new ArrayList<String>());
				}
			}
		}

		int size = 0;
		String configName = null;
		String configId = null;
		for (Object obj : sList) {
			List<?> list = (List<?>)obj;
			String managerName = null;
			if (list != null) {
				managerName = (String) list.get(GetNodeConfigSettingListTableDefine.MANAGER_NAME);
				String id = (String) list.get(GetNodeConfigSettingListTableDefine.GET_CONFIG_ID);
				configName = (String) list.get(GetNodeConfigSettingListTableDefine.GET_CONFIG_NAME);
				configId = id;
				map.get(managerName).add(id);
				size++;
			}
		}
		if (size > 0) {
			// 確認ダイアログにて変更が選択された場合、削除処理を行う。
			String msg = "";
			String[] args = new String[2];
			if (size == 1) {
				args[0] = configName;
				args[1] = configId;
				msg = "message.repository.53";
			} else {
				args[0] = Integer.toString(size);
				msg = "message.repository.59";
			}

			if (MessageDialog.openConfirm(
					null,
					Messages.getString("confirmed"),
					Messages.getString(msg, args)) == false) {

				return null;
			}

			for(Map.Entry<String, List<String>> entry : map.entrySet()) {
				new DeleteNodeConfigSetting().delete(entry.getKey(), entry.getValue());
			}
		}
		// ビューを更新
		nodeConfigSettingListView.update();
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
				if(part instanceof NodeConfigSettingListView){
					// Enable button when 1 item is selected
					NodeConfigSettingListView view = (NodeConfigSettingListView)part;

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
