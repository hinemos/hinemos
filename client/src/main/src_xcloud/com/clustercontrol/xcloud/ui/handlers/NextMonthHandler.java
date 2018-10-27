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
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.xcloud.ui.views.BillingDetailsView;

public class NextMonthHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		BillingDetailsView view = (BillingDetailsView)HandlerUtil.getActiveSite(event).getPage().findView(BillingDetailsView.Id);
		if (view != null)
			view.nextMonth();
		return null;
	}
}
