/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
