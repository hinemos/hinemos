/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
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

import com.clustercontrol.jobmanagement.view.JobLinkSendSettingListView;

/**
 * ジョブ設定[ジョブ連携送信設定]ビューの「更新」のクライアント側アクションクラス<BR>
 *
 */
public class JobLinkSendSettingRefreshAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(JobLinkSendSettingRefreshAction.class);
	/** アクションID */
	public static final String ID = JobLinkSendSettingRefreshAction.class.getName();
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
	 * ジョブ設定[ジョブ連携送信設定]ビューの「更新」が押された場合に、ジョブ[実行契機]ビューを更新します。
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.jobmanagement.view.JobLinkSendSettingListView
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
		JobLinkSendSettingListView view = null;
		
		try {
			view = (JobLinkSendSettingListView) viewPart.getAdapter(JobLinkSendSettingListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		//ジョブ設定[ジョブ連携送信設定]ビュー更新
		view.update();
		return null;
	}
}
