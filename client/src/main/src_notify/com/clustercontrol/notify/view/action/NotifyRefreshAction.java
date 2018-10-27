/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.view.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.notify.view.NotifyListView;

/**
 * 通知[一覧]ビューの更新アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 4.0.0
 */
public class NotifyRefreshAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(NotifyRefreshAction.class);

	/** アクションID */
	public static final String ID = NotifyRefreshAction.class.getName();

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
		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		NotifyListView view = null;
		try {
			view = (NotifyListView) this.viewPart.getAdapter(NotifyListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		// ビューの更新
		view.update();
		return null;
	}
}
