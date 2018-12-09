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
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.view.ScopeListBaseView;

/**
 * スコープ階層ペイン表示切替を行うクライアント側アクションクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class ScopeShowAction extends AbstractHandler {

	private static Log m_log = LogFactory.getLog( ScopeShowAction.class );
	
	private IWorkbenchWindow window;
	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
		this.window = null;
	}

	/**
	 * スコープ階層ペインの表示／非表示を行います。
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}

		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		ScopeListBaseView view = null;
		try {
			view = (ScopeListBaseView) viewPart
					.getAdapter(ScopeListBaseView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		ICommandService commandService = (ICommandService)window.getService(ICommandService.class);
		String id = this.getClass().getName();
		m_log.trace("execute id=" + id);
		Command command = commandService.getCommand(id);
		boolean isChecked = !HandlerUtil.toggleCommandState(command);

		if (isChecked) {
			view.show();
		} else {
			view.hide();
		}

		view.setFocus();
		return null;
	}
}
