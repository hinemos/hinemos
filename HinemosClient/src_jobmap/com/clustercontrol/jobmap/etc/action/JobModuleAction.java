/*
 * Copyright (c) 2020 NTT DATA INTELLILINK Corporation. All rights reserved.
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

import com.clustercontrol.jobmap.view.JobModuleView;
import org.eclipse.core.commands.AbstractHandler;

/**
 * ビューを表示するクライアント側アクションクラス<BR>
 * 
 */

public class JobModuleAction extends AbstractHandler {
	private static Log m_log = LogFactory.getLog(JobModuleAction.class); 

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		//アクティブページを手に入れる
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		//ビューを表示する
		try {
			page.showView(JobModuleView.ID);
			IViewPart viewPart = page.findView(JobModuleView.ID);
			if (viewPart == null) {
				m_log.debug("execute(), ViewPart is null");
				return null;
			}
			JobModuleView view = (JobModuleView) viewPart.getAdapter(JobModuleView.class);
			view.setFocus();
		} catch (PartInitException e) {
		}
		
		return null;
	}
}
