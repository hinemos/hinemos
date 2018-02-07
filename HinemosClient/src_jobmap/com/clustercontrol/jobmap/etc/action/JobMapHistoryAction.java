/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.etc.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.jobmap.view.JobMapHistoryView;
import com.clustercontrol.jobmap.view.action.BaseAction;

/**
 * ビューを表示するクライアント側アクションクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class JobMapHistoryAction extends BaseAction {
	private static Log m_log = LogFactory.getLog(JobMapHistoryAction.class); 

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		super.execute(event);
		
		//アクティブページを手に入れる
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		//ビューを表示する
		try {
			page.showView(JobMapHistoryView.ID);
			IViewPart viewPart = page.findView(JobMapHistoryView.ID);
			if (viewPart == null) {
				m_log.debug("viewPart is null");
				return null;
			}
			JobMapHistoryView view = (JobMapHistoryView) viewPart.getAdapter(JobMapHistoryView.class);
			view.setFocus();
		} catch (PartInitException e) {
		}
		
		return null;
	}
}