/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.etc.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.jobmanagement.view.JobLinkSendSettingListView;

/**
 * ジョブ設定[ジョブ連携送信設定]ビューを表示するクライアント側アクションクラス<BR>
 * 
 */
public class JobLinkSendSettingListAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(JobLinkSendSettingListAction.class);

	/**
	 * 終了する際に呼ばれます。
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	@Override
	public void dispose() {
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		//アクティブページを手に入れる
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow( event ).getActivePage();

		//ビューを表示する
		try {
			page.showView(JobLinkSendSettingListView.ID);
			IViewPart viewPart = page.findView(JobLinkSendSettingListView.ID);
			if (viewPart == null) {
				throw new InternalError("viewPart is null.");
			}
			JobLinkSendSettingListView view = (JobLinkSendSettingListView) viewPart
					.getAdapter(JobLinkSendSettingListView.class);
			if (view == null) {
				m_log.info("execute: view is null"); 
				return null;
			}
			view.setFocus();
		} catch (PartInitException e) {
		}
		return null;
	}
}
