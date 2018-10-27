/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.etc.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.monitor.view.MonitorListView;

/**
 * 監視[一覧]ビューを表示するクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 4.0.0
 */
public class OpenMonitorListViewAction extends AbstractHandler {

	/**
	 * 終了する際に呼ばれます。
	 *
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	@Override
	public void dispose() {
	}

	/**
	 * 監視[一覧]ビューを表示します。
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.monitor.view.MonitorListView
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// アクティブページ取得
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow( event ).getActivePage();

		// ビューを表示する
		try {
			MonitorListView view = (MonitorListView) page.showView(MonitorListView.ID);

			view.setFocus();
		} catch (PartInitException e) {
		}
		return null;
	}
}
