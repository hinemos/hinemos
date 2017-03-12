/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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

import com.clustercontrol.infra.view.InfraManagementView;

/**
 * 環境構築[構築・チェック]ビューを表示するクライアント側アクションクラス<BR>
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class InfraManagementAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(InfraManagementAction.class);

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
			page.showView(InfraManagementView.ID);
			IViewPart viewPart = page.findView(InfraManagementView.ID);
			if (viewPart == null)
				throw new InternalError("viewPart is null.");
			InfraManagementView view = (InfraManagementView) viewPart
					.getAdapter(InfraManagementView.class);
			if (view == null) {
				m_log.info("execute: view is null"); 
				return null;
			}
			view.setFocus();
		} catch (PartInitException e) {
		}
		return null;
	}
}