/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.etc.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.utility.settings.ui.views.ImportExportExecView;
import com.clustercontrol.utility.ui.dialog.OpenFolderDialog;

/**
 * ファイルを保持するフォルダを選択するアクションクラス<BR>
 * 
 * @version 6.1.0
 * @since 1.2.0
 */
public class OpenFolderAction implements IWorkbenchWindowActionDelegate {

	/*
 	* (non-Javadoc)
 	* 
 	* @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
 	*/
	@Override
	public void dispose() {

	}

	/*
 	* (non-Javadoc)
 	* 
 	* @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
 	*/
	@Override
	public void init(IWorkbenchWindow window) {

	}

	/*
 	* (non-Javadoc)
 	* 
 	* @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
 	*/
	@Override
	public void run(IAction action) {
		
		//ディレクトリ選択ダイアログを開く
		new OpenFolderDialog(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell());
 
		// Viewのアップデート
		// 呼び出し元のViewを持ってきます。
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		ImportExportExecView listView = (ImportExportExecView) page
				.findView(ImportExportExecView.ID);
		if (listView != null){
			listView.update();
		}
	}


	/*
 	* (non-Javadoc)
 	* 
 	* @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
 	*  	org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {

	}
}