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

import com.clustercontrol.maintenance.dialog.HinemosPropertyTypeDialog;
import com.clustercontrol.maintenance.view.HinemosPropertyView;
import com.clustercontrol.util.EndpointManager;

/**
 * メンテナンス[共通設定]ビューの作成アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class HinemosPropertyAddAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(HinemosPropertyAddAction.class);
	
	/** アクションID */
	public static final String ID = HinemosPropertyAddAction.class.getName();

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

		String manageName = EndpointManager.getActiveManagerNameList().get(0);
		HinemosPropertyTypeDialog dialog = new HinemosPropertyTypeDialog(this.viewPart.getSite().getShell(), view, manageName);
		dialog.open();
		return null;
	}

}
