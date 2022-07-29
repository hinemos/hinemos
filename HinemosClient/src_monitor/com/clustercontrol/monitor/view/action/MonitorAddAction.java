/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.view.action;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.monitor.dialog.MonitorTypeDialog;
import com.clustercontrol.monitor.view.MonitorListView;

/**
 * 監視[一覧]ビューの作成アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 4.0.0
 */
public class MonitorAddAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(MonitorAddAction.class);
	
	/** アクションID */
	public static final String ID = MonitorAddAction.class.getName();

	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
	}

	/**
	 * 監視種別一覧ダイアログを表示します
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// ビューの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		MonitorListView view = null;
		try {
			view = (MonitorListView) this.viewPart.getAdapter(MonitorListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		// 監視種別ダイアログを開く
		MonitorTypeDialog dialog = new MonitorTypeDialog(this.viewPart.getSite().getShell(), view);
		dialog.open();

		// ビューの更新
		view.update();

		return null;
	}
}
