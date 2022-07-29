/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.view.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.reporting.view.ReportingTemplateSetView;

/**
 * レポーティング[テンプレートセット]ビューの更新アクションクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class TemplateSetRefreshAction extends AbstractHandler {

	/** ログ */
	private static Log m_log = LogFactory.getLog(TemplateSetRefreshAction.class);

	/** アクションID */
	public static final String ID = TemplateSetRefreshAction.class.getName();

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
		ReportingTemplateSetView view = (ReportingTemplateSetView) this.viewPart.getAdapter(ReportingTemplateSetView.class);
		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}
		view.update();
		return null;
	}

}
