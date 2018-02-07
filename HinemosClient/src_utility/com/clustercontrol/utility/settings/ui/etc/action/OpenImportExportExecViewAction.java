/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.etc.action;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.utility.settings.ui.views.ImportExportExecView;

/**
 * 設定インポートエクスポートビュー表示用アクションクラス
 * 
 * @version 6.1.0
 * @since 2.0.0
 * 
 *
 */
public class OpenImportExportExecViewAction implements
		IWorkbenchWindowActionDelegate {
	/* ロガー */
	private static Logger log = Logger.getLogger(OpenImportExportExecViewAction.class);

	/**
 	* 設定インポートエクスポートビューを表示します。
 	* 
 	* @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
 	* @see com.clustercontrol.utility.traputil.view.EditTrapMasterView
 	*/
	@Override
	public void run(IAction action) {
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
	}
	

	/**
 	* 終了する際に呼ばれます。
 	* 
 	* @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
 	*/
	@Override
	public void dispose() {
	}

	/**
	 * ワークベンチにロードされた際に呼ばれます。
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void init(IWorkbenchWindow window) {
	}

	/**
	 * 選択を変更した際に呼ばれます。
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *	  org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

}
