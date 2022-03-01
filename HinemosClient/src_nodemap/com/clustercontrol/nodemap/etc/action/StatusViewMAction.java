/*
 * Copyright (c) 2020 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.etc.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import com.clustercontrol.nodemap.view.StatusViewM;

public class StatusViewMAction extends AbstractHandler{

	// ログ
	private static Log m_log = LogFactory.getLog( StatusViewMAction.class );

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		m_log.debug("execute(), new view");

		// アクティブページ取得
		IWorkbenchPage page = PlatformUI.getWorkbench()
		.getActiveWorkbenchWindow().getActivePage();

		// ビューを表示する
		try {
			StatusViewM view = (StatusViewM) page.showView(StatusViewM.ID);
			view.setFocus();
		} catch (PartInitException e) {
			m_log.warn("execute(), " + e.getMessage(), e);
		}
		return null;
	}
}
