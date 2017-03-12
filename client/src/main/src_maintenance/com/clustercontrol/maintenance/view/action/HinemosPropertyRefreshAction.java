/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.maintenance.view.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.maintenance.view.HinemosPropertyView;

/**
 * メンテナンス[共通設定]ビューの更新アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class HinemosPropertyRefreshAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(HinemosPropertyRefreshAction.class);
	
	/** アクションID */
	public static final String ID = HinemosPropertyRefreshAction.class.getName();

	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// ビューの更新
		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		HinemosPropertyView view = null;
		try {
			view = (HinemosPropertyView) this.viewPart.getAdapter(HinemosPropertyView.class);
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
