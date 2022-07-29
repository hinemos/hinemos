/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.view.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.maintenance.view.MaintenanceListView;


/**
 * メンテナンス[一覧]ビューの更新アクションクラス<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class MaintenanceRefreshAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(MaintenanceRefreshAction.class);
	
	/** アクションID */
	public static final String ID = MaintenanceRefreshAction.class.getName();

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
		// ビューの更新
		this.viewPart = HandlerUtil.getActivePart(event);
		MaintenanceListView view = null;
		try {
			view = (MaintenanceListView) this.viewPart.getAdapter(MaintenanceListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}
		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}
		view.update();
		return null;
	}

}
