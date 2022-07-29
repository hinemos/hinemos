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

import com.clustercontrol.monitor.view.StatusView;
import com.clustercontrol.monitor.view.action.ScopeShowActionStatus;

/**
 * HinemosClientプロジェクトのStatusViewとスコープツリーの非表示等の操作が衝突しないように
 * NodeMapプロジェクトに同じものをつくる
 *
 */
public class StatusViewM extends StatusView {
	/** ビューID */
	public static final String ID = StatusViewM.class.getName();
	
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
		Command command = service.getCommand(ScopeShowActionStatus.ID);
		command.getState(RegistryToggleState.STATE_ID).setValue(false);
		service.refreshElements(ScopeShowActionStatus.ID, null);
		hide();
//		update(false);
	}
}
