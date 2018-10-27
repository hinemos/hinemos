/*
* Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
*
* Hinemos (http://www.hinemos.info/)
*
* See the LICENSE file for licensing information.
*/

package com.clustercontrol.inquiry.etc.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.inquiry.view.InquiryView;

/**
 * 遠隔管理ビューを表示するクライアント側アクションクラス<BR>
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
public class OpenInquiryViewAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(OpenInquiryViewAction.class);

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
			page.showView(InquiryView.ID);
			IViewPart viewPart = page.findView(InquiryView.ID);
			if (viewPart == null)
				throw new InternalError("viewPart is null.");
			InquiryView view = (InquiryView) viewPart
					.getAdapter(InquiryView.class);
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