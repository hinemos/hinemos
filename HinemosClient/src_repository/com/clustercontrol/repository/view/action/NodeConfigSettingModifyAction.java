/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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

import com.clustercontrol.repository.action.GetNodeConfigSettingListTableDefine;
import com.clustercontrol.repository.dialog.NodeConfigSettingCreateDialog;
import com.clustercontrol.repository.view.NodeConfigSettingListView;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * 構成情報取得ダイアログによる、設定変更を行うクライアント側アクションクラス<BR>
 *
 * @version 6.2.0
 * @since 6.2.0
 */
public class NodeConfigSettingModifyAction extends AbstractHandler implements IElementUpdater {
	public static final String ID = NodeConfigSettingModifyAction.class.getName();

	//	 ----- instance フィールド ----- //
	/** ログ */
	private static Log m_log = LogFactory.getLog(NodeConfigSettingModifyAction.class);

	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
	}

	// ----- instance メソッド ----- //

	/**
	 * @see org.eclipse.core.commands.IHandler#execute
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.viewPart = HandlerUtil.getActivePart(event);

		// ノード一覧より、選択されているノードのファシリティIDを取得
		NodeConfigSettingListView configListView = null;
		try {
			configListView = (NodeConfigSettingListView) this.viewPart
					.getAdapter(NodeConfigSettingListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (configListView == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		StructuredSelection selection = (StructuredSelection) configListView
				.getComposite().getTableViewer().getSelection();

		List<?> list = (List<?>) selection.getFirstElement();

		if (list == null) {
			return null;
		}

		String managerName = (String) list.get(GetNodeConfigSettingListTableDefine.MANAGER_NAME);
		String settingId = (String) list.get(GetNodeConfigSettingListTableDefine.GET_CONFIG_ID);
		
		if (settingId != null) {
			// ダイアログを生成
			NodeConfigSettingCreateDialog dialog = new NodeConfigSettingCreateDialog(this.viewPart
					.getSite().getShell(), managerName, settingId, true);

			// ダイアログにて変更が選択された場合、入力内容をもって更新を行う。
			if (dialog.open() == IDialogConstants.OK_ID) {
				configListView.update();

				// 選択させることで、他のビューの更新を促す。
				CommonTableViewer viewer = configListView.getComposite().getTableViewer();
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
