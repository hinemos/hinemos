/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.view.action;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.nodemap.view.NodeListView;
import com.clustercontrol.nodemap.dialog.NodeListFindDialog;

/**
 * ノードマップ検索ダイアログによる、検索処理を行うクライアント側アクションクラス<BR>
 *
 * @version 6.2.0
 */
public class NodeListFindAction extends AbstractHandler implements IElementUpdater {

	public static final String ID = OpenNodeMapAction.ActionIDBase + NodeListFindAction.class.getSimpleName();

	private IWorkbenchWindow window;
	private IWorkbenchPart viewPart;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == window || !isEnabled() ){
			return null;
		}

		this.viewPart = HandlerUtil.getActivePart(event);
		NodeListView view = (NodeListView) this.viewPart.getAdapter(NodeListView.class);

		// ダイアログを生成
		NodeListFindDialog dialog = new NodeListFindDialog(this.viewPart.getSite().getShell(), view.getSecondaryId());
		// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
		if (dialog.open() == IDialogConstants.OK_ID) {
			view.setNodeFilterInfo(dialog.getInputData());
			view.reload();
		}
		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if( null != window ){
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				IWorkbenchPart part = page.getActivePart();

				if (part instanceof NodeListView) {
					element.setChecked(((NodeListView)part).getNodeFilterInfo() != null);
				}
			}
		}
	}
}
