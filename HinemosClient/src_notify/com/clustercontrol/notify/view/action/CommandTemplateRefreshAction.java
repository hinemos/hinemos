/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
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

import com.clustercontrol.notify.view.CommandTemplateListView;

public class CommandTemplateRefreshAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(CommandTemplateRefreshAction.class);

	/** アクションID */
	public static final String ID = CommandTemplateRefreshAction.class.getName();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart viewPart = HandlerUtil.getActivePart(event);
		CommandTemplateListView view = null;
		try {
			view = (CommandTemplateListView) viewPart.getAdapter(CommandTemplateListView.class);
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
