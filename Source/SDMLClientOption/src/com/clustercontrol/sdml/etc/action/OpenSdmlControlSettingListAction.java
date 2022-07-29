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

import com.clustercontrol.sdml.view.SdmlControlSettingListView;

/**
 * SDML設定[制御設定]ビューを表示するクライアント側アクションクラス
 *
 */
public class OpenSdmlControlSettingListAction extends AbstractHandler {
	private static Logger logger = Logger.getLogger(OpenSdmlControlSettingListAction.class);

	/**
	 * SDML設定[制御設定]ビューを表示します。
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// アクティブページ取得
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		// ビューを表示する
		try {
			SdmlControlSettingListView view = (SdmlControlSettingListView) page.showView(SdmlControlSettingListView.ID);

			view.setFocus();
		} catch (PartInitException e) {
			logger.error("execute() : " + e.getMessage(), e);
		}
		return null;
	}
}
