/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.ClusterControlPlugin;

/**
 * エラー・ログ出力クラス<BR>
 *
 * エラー・ログビューにエラーを出力する。
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class ErrorLog {

	/**
	 * エラーログ記録処理
	 *
	 * エラーメッセージをエラーログに記録する
	 *
	 * @param message
	 *            出力メッセージ
	 * @param e
	 *            Exceptionクラス
	 */
	public static void log(String message, Exception e) {
		ClusterControlPlugin plugin = ClusterControlPlugin.getDefault();

		IStatus status = new Status(IStatus.ERROR, plugin.getBundle()
				.getSymbolicName(), 0, message, e);

		plugin.getLog().log(status);
	}

	/**
	 * エラーダイアログ表示処理
	 *
	 * エラーメッセージをエラーダイアログに表示する
	 *
	 * @param message
	 *            出力メッセージ
	 * @param e
	 *            Exceptionクラス
	 */
	public static void openErrorDialog(String message, Exception e) {
		ClusterControlPlugin plugin = ClusterControlPlugin.getDefault();

		IStatus status = new Status(IStatus.ERROR, plugin.getBundle()
				.getSymbolicName(), 0, message, e);

		//エラーダイアログ表示
		ErrorDialog.openError(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(), "Error", null, status);
	}
}
