/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.infra.view.action;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.infra.view.InfraFileManagerView;


public class RefreshInfraFileManagerAction extends InfraFileManagerBaseAction {
	/** ログ */
	private static Log m_log = LogFactory.getLog(RefreshInfraFileManagerAction.class);
	/** アクションID */
	public static final String ID = RefreshInfraFileManagerAction.class.getName();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		InfraFileManagerView view = getView(event);
		if (view == null) {
			m_log.info("execute: view is null");
			return null;
		}

		view.update();
		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
	}
}
