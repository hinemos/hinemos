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

import com.clustercontrol.jobmanagement.view.JobLinkMessageView;

/**
 * ジョブ履歴[受信ジョブ連携メッセージ一覧]ビューの「更新」のクライアント側アクションクラス<BR>
 *
 */
public class JobLinkMessageRefreshAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(JobLinkMessageRefreshAction.class);
	/** アクションID */
	public static final String ID = JobLinkMessageRefreshAction.class.getName();
	/** ビュー */
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
	 * ジョブ履歴[受信ジョブ連携メッセージ一覧]ビューの「更新」が押された場合に、ビューを更新します。
	 *
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if (null == this.window || !isEnabled()) {
			return null;
		}

		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		JobLinkMessageView view = null;
		try {
			view = (JobLinkMessageView) viewPart.getAdapter(JobLinkMessageView.class);
		} catch (Exception e) {
			m_log.info("execute " + e.getMessage());
			return null;
		}
		if (view == null) {
			m_log.info("execute: view is null");
			return null;
		}
		// ビュー更新
		view.update();
		return null;
	}
}
