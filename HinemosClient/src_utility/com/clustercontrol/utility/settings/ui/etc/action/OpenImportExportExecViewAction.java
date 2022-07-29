/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.etc.action;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.core.commands.AbstractHandler;

import com.clustercontrol.utility.settings.ui.views.ImportExportExecView;

/**
 * 設定インポートエクスポートビュー表示用アクションクラス
 * 
 * @version 6.1.0
 * @since 2.0.0
 * 
 *
 */
public class OpenImportExportExecViewAction extends AbstractHandler {
	/* ロガー */
	private static Logger log = Logger.getLogger(OpenImportExportExecViewAction.class);

	/**
 	* 設定インポートエクスポートビューを表示します。
 	* 
 	* @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
 	*/

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// アクティブページ取得
		IWorkbenchPage page = PlatformUI.getWorkbench()
		.getActiveWorkbenchWindow().getActivePage();
		
		// ビューを表示する
		try {
			ImportExportExecView view = (ImportExportExecView) page.showView(ImportExportExecView.ID);
			
			view.setFocus();
		} catch (PartInitException e) {
			log.error(e);
		}
		return null;
	}
}
