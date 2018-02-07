/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.view.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.repository.dialog.NodeSearchDialog;

/**
 * ノードサーチを行うクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class NodeSearchAction extends AbstractHandler {
	public static final String ID = OpenNodeMapAction.ActionIDBase + NodeSearchAction.class.getSimpleName();

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

		// ダイアログを生成
		NodeSearchDialog dialog = new NodeSearchDialog(this.viewPart.getSite().getShell());

		// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
		if(dialog.open() == IDialogConstants.OK_ID){
			ClientSession.doCheck();
		}
		return null;
	}
}
