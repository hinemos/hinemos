/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.view.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.rpa.view.RpaScenarioTagView;

/**
 * RPA設定[シナリオタグ]ビューの更新アクションクラス<BR>
 */
public class RpaScenarioTagRefreshAction extends AbstractHandler {

	/** ログ */
	private static Log log = LogFactory.getLog(RpaScenarioTagRefreshAction.class);

	/** アクションID */
	public static final String ID = RpaScenarioTagRefreshAction.class.getName();

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
		RpaScenarioTagView view = (RpaScenarioTagView) this.viewPart.getAdapter(RpaScenarioTagView.class);
		if (view == null) {
			log.info("execute: view is null"); 
			return null;
		}
		view.update();
		return null;
	}

}
