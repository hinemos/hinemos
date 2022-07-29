/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.etc.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.rpa.view.RpaScenarioTagView;

/**
 * RPA設定[RPAシナリオタグ]ビューを表示するクライアント側アクションクラス<BR>
 */
public class OpenRpaScenarioTagViewAction extends AbstractHandler{

	/**
	 * RPA設定[RPAシナリオタグ]ビューを表示します。
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		// アクティブページ取得
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow( event ).getActivePage();

		// ビューを表示する
		try {
			IViewPart view = (IViewPart) page.showView(RpaScenarioTagView.ID);

			view.setFocus();
		} catch (PartInitException e) {
		}

		return null;
	}

	/**
	 * 終了する際に呼ばれます。
	 * 
	 * @see org.eclipse.core.commands.AbstractHandler#dispose()
	 */
	@Override
	public void dispose(){}

}
