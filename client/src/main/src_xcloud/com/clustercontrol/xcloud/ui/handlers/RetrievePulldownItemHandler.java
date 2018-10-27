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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolItem;

public class RetrievePulldownItemHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if ( null == event ) {
			return null;
		}

		if ( ! ( event.getTrigger() instanceof Event ) ) {
			return null;
		}

		Event eventWidget = (Event)event.getTrigger();

		if ( ! ( eventWidget.widget instanceof ToolItem ) ) {
			return null;
		}

		ToolItem toolItem = (ToolItem)eventWidget.widget;
		Event newEvent = new Event();
		newEvent.button = 1;
		newEvent.widget = toolItem;
		newEvent.detail = SWT.ARROW;
		newEvent.x = toolItem.getBounds().x;
		newEvent.y = toolItem.getBounds().y + toolItem.getBounds().height;
		toolItem.notifyListeners( SWT.Selection, newEvent );
		return null;
	}
}
