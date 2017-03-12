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

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.repository.action.GetNodeListTableDefine;
import com.clustercontrol.repository.dialog.NodeCreateDialog;
import com.clustercontrol.repository.view.NodeListView;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * ノードの作成・変更ダイアログによる、ノード変更を行うクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class NodeCopyAction extends AbstractHandler implements IElementUpdater {
	public static final String ID = NodeCopyAction.class.getName();

	//	 ----- instance フィールド ----- //
	/** ログ */
	private static Log m_log = LogFactory.getLog(NodeCopyAction.class);

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
		NodeListView view = null;
		try {
			view = (NodeListView) this.viewPart
					.getAdapter(NodeListView.class);
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
		String facilityId = null;
		if (list == null) {
			return null;
		}

		managerName = (String) list.get(GetNodeListTableDefine.MANAGER_NAME);
		facilityId = (String) list.get(GetNodeListTableDefine.FACILITY_ID);

		if (facilityId != null) {
			// ダイアログを生成
			NodeCreateDialog dialog = new NodeCreateDialog(this.viewPart
					.getSite().getShell(), managerName, facilityId, false);

			// ダイアログにて変更が選択された場合、入力内容をもって更新を行う。
			if (dialog.open() == IDialogConstants.OK_ID) {
				view.update();

				// 選択させることで、他のビューの更新を促す。
				CommonTableViewer viewer = view.getComposite().getTableViewer();
				viewer.setSelection(viewer.getSelection());
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
