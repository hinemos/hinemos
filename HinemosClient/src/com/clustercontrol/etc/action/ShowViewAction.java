/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.etc.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.view.CommonViewPart;

public abstract class ShowViewAction extends AbstractHandler {
	private static Log log = LogFactory.getLog(ShowViewAction.class);
	
	private Class<?> viewClass;
	private String viewId;
	
	public ShowViewAction(Class<?> viewClass, String viewId) {
		this.viewClass = viewClass;
		this.viewId = viewId;
	}

	@Override
	public void dispose() {
		// NOP
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// アクティブページを手に入れる
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();

		// ビューを表示する
		try {
			page.showView(viewId);

			IViewPart viewPart = page.findView(viewId);
			if (viewPart == null) throw new InternalError("execute: ViewPart is null. viewId=" + viewId);

			CommonViewPart view = (CommonViewPart) viewPart.getAdapter(viewClass);
			if (view == null) {
				log.info("execute: View is null. viewId=" + viewId);
				return null;
			}
			view.setFocus();
		} catch (PartInitException e) {
		}
		return null;
	}
}
