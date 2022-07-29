/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.view;

import org.eclipse.core.commands.Command;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.RegistryToggleState;

import com.clustercontrol.monitor.view.EventView;
import com.clustercontrol.monitor.view.action.ScopeShowActionEvent;

/**
 * HinemosClientプロジェクトのEventViewとスコープツリーの非表示等の操作が衝突しないように
 * NodeMapプロジェクトに同じものをつくる
 *
 */
public class EventViewM extends EventView {
	/** ビューID */
	public static final String ID = EventViewM.class.getName();
	
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
		Command command = service.getCommand(ScopeShowActionEvent.ID);
		command.getState(RegistryToggleState.STATE_ID).setValue(false);
		service.refreshElements(ScopeShowActionEvent.ID, null);
		hide();
//		update(false);
	}
}
