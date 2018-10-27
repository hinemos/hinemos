/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.etc.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.infra.view.InfraFileManagerView;

/**
 * 環境構築[ファイルマネージャ]ビューを表示するクライアント側アクションクラス<BR>
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class InfraFileManagerAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(InfraFileManagerAction.class);

	/**
	 * 終了する際に呼ばれます。
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	@Override
	public void dispose() {
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		//アクティブページを手に入れる
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow( event ).getActivePage();

		//ビューを表示する
		try {
			page.showView(InfraFileManagerView.ID);
			IViewPart viewPart = page.findView(InfraFileManagerView.ID);
			if (viewPart == null)
				throw new InternalError("viewPart is null.");
			InfraFileManagerView view = (InfraFileManagerView) viewPart
					.getAdapter(InfraFileManagerView.class);
			if (view == null) {
				m_log.info("execute: view is null"); 
				return null;
			}
			view.setFocus();
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		return null;
	}
}