/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.collect.etc.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.collect.view.CollectGraphView;

/**
 * 性能グラフビューを表示するアクションクラス
 * 
 * @version 5.0.0
 * @since 1.0
 */
public class OpenCollectGraphViewAction extends AbstractHandler {

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	@Override
	public void dispose() {
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// アクティブページ取得
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow( event ).getActivePage();

		// ビューを表示する
		try {
			CollectGraphView view = (CollectGraphView) page
					.showView(CollectGraphView.ID);

			view.setFocus();
		} catch (PartInitException e) {
		}
		return null;
	}
}
