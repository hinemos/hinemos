/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.restaccess.etc.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.notify.restaccess.view.RestAccessInfoListView;

/**
 * RESTアクセス情報[一覧]ビューを表示するクライアント側アクションクラス<BR>
 *
 */
public class OpenRestAccessInfoListViewAction extends AbstractHandler {

	/**
	 * 終了する際に呼ばれます。
	 *
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	@Override
	public void dispose() {
	}

	/**
	 * RESTアクセス情報[一覧]ビューを表示します。
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.notify.restaccess.view.RestAccessInfoListView
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// アクティブページ取得
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow( event ).getActivePage();

		// ビューを表示する
		try {
			RestAccessInfoListView view = (RestAccessInfoListView) page.showView(RestAccessInfoListView.ID);

			view.setFocus();
		} catch (PartInitException e) {
		}
		return null;
	}
}
