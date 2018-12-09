/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.mail.etc.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.notify.mail.view.MailTemplateListView;

/**
 * メールテンプレート[一覧]ビューを表示するクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 4.0.0
 */
public class OpenMailTemplateListViewAction extends AbstractHandler {

	/**
	 * 終了する際に呼ばれます。
	 *
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	@Override
	public void dispose() {
	}

	/**
	 * メールテンプレート[一覧]ビューを表示します。
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.notify.mail.view.MailTemplateListView
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// アクティブページ取得
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow( event ).getActivePage();

		// ビューを表示する
		try {
			MailTemplateListView view = (MailTemplateListView) page.showView(MailTemplateListView.ID);

			view.setFocus();
		} catch (PartInitException e) {
		}
		return null;
	}
}
