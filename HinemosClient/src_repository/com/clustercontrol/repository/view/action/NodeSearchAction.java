/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.view.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.repository.dialog.NodeSearchDialog;
import com.clustercontrol.repository.view.NodeListView;

/**
 * ノードサーチを行うクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class NodeSearchAction extends AbstractHandler {
	public static final String ID = NodeSearchAction.class.getName();
	/** ログ */
	private static Log m_log = LogFactory.getLog(NodeSearchAction.class);

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
			NodeListView view = null;
			try {
				view = (NodeListView) this.viewPart.getAdapter(NodeListView.class);
			} catch (Exception e) { 
				m_log.info("execute " + e.getMessage()); 
				return null; 
			}
			if (view == null) {
				m_log.info("execute: view is null"); 
				return null;
			}
			view.update();
			ClientSession.doCheck();
		}
		return null;
	}
}
