/*
* Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
*
* Hinemos (http://www.hinemos.info/)
*
* See the LICENSE file for licensing information.
*/

package com.clustercontrol.inquiry.view.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.inquiry.view.InquiryView;

/**
 * 遠隔管理ビューの更新アクションクラス<BR>
 *
 * @version 6.1.0
 * @since 6.1.0
 */
public class RefreshTargetDataAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(RefreshTargetDataAction.class);

	/** アクションID */
	public static final String ID = "com.clustercontrol.enterprise.inquiry.view.action.RefreshTargetDataAction"; //$NON-NLS-1$

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
		InquiryView view = null;
		try {
			view = (InquiryView) this.viewPart.getAdapter(InquiryView.class);
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