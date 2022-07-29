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

import com.clustercontrol.rpa.view.RpaManagementToolAccountView;

/**
 * RPA設定[RPA管理ツールアカウント]ビューの更新アクションクラス<BR>
 * 
 */
public class AccountRefreshAction extends AbstractHandler {

	/** ログ */
	private static Log m_log = LogFactory.getLog(AccountRefreshAction.class);

	/** アクションID */
	public static final String ID = AccountRefreshAction.class.getName();

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
		RpaManagementToolAccountView view = (RpaManagementToolAccountView) this.viewPart.getAdapter(RpaManagementToolAccountView.class);
		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}
		view.update();
		return null;
	}

}
