/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.view.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.sdml.view.SdmlControlSettingListView;

/**
 * SDML制御設定の一覧を更新するビューアクション
 *
 */
public class SdmlControlSettingRefreshAction extends AbstractHandler {
	private static Log logger = LogFactory.getLog(SdmlControlSettingRefreshAction.class);

	/** アクションID */
	public static final String ID = SdmlControlSettingRefreshAction.class.getName();

	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.viewPart = HandlerUtil.getActivePart(event);

		SdmlControlSettingListView view = null;
		try {
			view = (SdmlControlSettingListView) this.viewPart.getAdapter(SdmlControlSettingListView.class);
		} catch (Exception e) {
			logger.info("execute() : " + e.getMessage());
			return null;
		}
		if (view == null) {
			logger.info("execute() : view is null");
			return null;
		}

		// ビューの更新
		view.update();
		return null;
	}
}
