/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.xcloud.ui.views.RepositoryView;


public class ShowRepositoryViewHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IViewPart view = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().showView(RepositoryView.Id);
			view.setFocus();
		} catch (PartInitException e) {
		}
		return null;
	}
}
