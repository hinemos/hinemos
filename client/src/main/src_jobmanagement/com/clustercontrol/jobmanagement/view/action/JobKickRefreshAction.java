/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.view.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.jobmanagement.view.JobKickListView;

/**
 * ジョブ[実行契機]ビューの「更新」のクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class JobKickRefreshAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(JobKickRefreshAction.class);
	/** アクションID */
	public static final String ID = JobKickRefreshAction.class.getName();
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
	 * ジョブ[実行契機]ビューの「更新」が押された場合に、ジョブ[実行契機]ビューを更新します。
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.jobmanagement.view.JobKickListView
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
		JobKickListView view = null;
		
		try {
			view = (JobKickListView) viewPart.getAdapter(JobKickListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		//ジョブ[実行契機]ビュー更新
		view.update();
		return null;
	}
}
