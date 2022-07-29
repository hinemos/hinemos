/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.etc.action;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.sdml.view.MonitorListViewM;

public class OpenMonitorListViewMAction extends AbstractHandler {
	private static Logger logger = Logger.getLogger(OpenMonitorListViewMAction.class);

	/**
	 * SDML設定パースペクティブ用の監視設定[一覧]ビューを表示します。
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// アクティブページ取得
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		// ビューを表示する
		try {
			MonitorListViewM view = (MonitorListViewM) page.showView(MonitorListViewM.ID);

			view.setFocus();
		} catch (PartInitException e) {
			logger.error("execute() : " + e.getMessage(), e);
		}
		return null;
	}
}
