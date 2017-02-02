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

import com.clustercontrol.repository.action.DeleteNodeProperty;
import com.clustercontrol.repository.action.GetNodeListTableDefine;
import com.clustercontrol.repository.view.NodeListView;
import com.clustercontrol.util.Messages;

/**
 * ノードの削除を行うクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class NodeDeleteAction extends AbstractHandler implements IElementUpdater {
	public static final String ID = NodeDeleteAction.class.getName();

	//	 ----- instance フィールド ----- //
	/** ログ */
	private static Log m_log = LogFactory.getLog(NodeDeleteAction.class);

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
		NodeListView nodeListView = null;
		try {
			nodeListView = (NodeListView) this.viewPart
					.getAdapter(NodeListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (nodeListView == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		StructuredSelection selection = (StructuredSelection) nodeListView
				.getComposite().getTableViewer().getSelection();

		List<?> sList = (List<?>) selection.toList();
		Map<String, List<String>> map = new ConcurrentHashMap<String, List<String>>();

		for (Object obj : sList) {
			List<?> list = (List<?>)obj;
			String managerName = null;
			if (list != null) {
				managerName = (String) list.get(GetNodeListTableDefine.MANAGER_NAME);
				if(map.get(managerName) == null) {
					map.put(managerName, new ArrayList<String>());
				}
			}
		}

		int size = 0;
		String facilityName = null;
		String facilityId = null;
		for (Object obj : sList) {
			List<?> list = (List<?>)obj;
			String managerName = null;
			if (list != null) {
				managerName = (String) list.get(GetNodeListTableDefine.MANAGER_NAME);
				String id = (String) list.get(GetNodeListTableDefine.FACILITY_ID);
				facilityName = (String) list.get(GetNodeListTableDefine.FACILITY_NAME);
				facilityId = id;
				map.get(managerName).add(id);
				size++;
			}
		}
		if (size > 0) {
			// 確認ダイアログにて変更が選択された場合、削除処理を行う。
			String msg = "";
			String[] args = new String[2];
			if (size == 1) {
				args[0] = facilityName;
				args[1] = facilityId;
				msg = "message.repository.1";
			} else {
				args[0] = Integer.toString(size);
				msg = "message.repository.51";
			}

			if (MessageDialog.openConfirm(
					null,
					Messages.getString("confirmed"),
					Messages.getString(msg, args)) == false) {

				return null;
			}

			for(Map.Entry<String, List<String>> entry : map.entrySet()) {
				new DeleteNodeProperty().delete(entry.getKey(), entry.getValue());
			}
		}
		// ビューを更新
		nodeListView.update();
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
				if(part instanceof NodeListView){
					// Enable button when 1 item is selected
					NodeListView view = (NodeListView)part;

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
