/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.etc.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.notify.dialog.NotifyListDialog;

/**
 * 通知[一覧]ダイアログを表示するクライアント側アクションクラス<BR>
 * 
 * @version 2.0.0
 * @since 2.0.0
 */
public class NotifyListAction implements IWorkbenchWindowActionDelegate {

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
	 * 通知[一覧]ダイアログを表示します。
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 * @see com.clustercontrol.notify.dialog.NotifyListDialog
	 */
	@Override
	public void run(IAction action) {
		// シェルを取得
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getShell();

		// ダイアログ表示及び終了処理
		NotifyListDialog dialog = new NotifyListDialog(shell);
		dialog.open();
	}

	/**
	 * 選択を変更した際に呼ばれます。
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}
}
