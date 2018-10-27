/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.view.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.accesscontrol.view.RoleListView;

/**
 * アクセス[ロール]ビューの「更新」のアクションクラス
 *
 * @version 5.0.0
 * @since 4.1.0
 */
public class RoleRefreshAction extends AbstractHandler {
	/** アクションID */
	public static final String ID = RoleRefreshAction.class.getName();
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
	 * アクセス[ロール]ビューの「更新」が押された場合に、<BR>
	 * アクセス[ロール]ビューを更新します。
	 *
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 * @see com.clustercontrol.accesscontrol.view.RoleListView
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.viewPart = HandlerUtil.getActivePart(event);
		if (this.viewPart instanceof RoleListView) {
			((RoleListView) this.viewPart).update();
		}
		return null;
	}
}
