/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.monitor.view.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RegistryToggleState;

import com.clustercontrol.bean.Property;
import com.clustercontrol.monitor.dialog.MonitorFilterDialog;
import com.clustercontrol.monitor.view.MonitorListView;

/**
 * 監視[一覧]ビューのフィルタ表示アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 4.0.0
 */
public class MonitorFilterAction extends AbstractHandler {
	private static Log m_log = LogFactory.getLog(MonitorFilterAction.class);
			
	/** アクションID */
	public static final String ID = MonitorFilterAction.class.getName();

	private IWorkbenchWindow window;
	/** ビュー */
	private IWorkbenchPart viewPart;

	/**]
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
	}

	/**
	 * フィルタアクションのメイン処理
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		this.viewPart = HandlerUtil.getActivePart(event);
		MonitorListView view = null;
		try {
			view = (MonitorListView) this.viewPart.getAdapter(MonitorListView.class);
		} catch (Exception e) {
			m_log.info("execute " + e.getMessage());
			return null;
		}

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		ICommandService commandService = (ICommandService)window.getService(ICommandService.class);
		Command command = commandService.getCommand(ID);
		boolean isChecked = !HandlerUtil.toggleCommandState(command);

		if (isChecked) {
			// ダイアログを生成
			MonitorFilterDialog dialog = new MonitorFilterDialog(this.viewPart
					.getSite().getShell());

			// ダイアログにて検索が選択された場合、検索結果をビューに表示
			if (dialog.open() == IDialogConstants.OK_ID) {

				Property condition = dialog.getInputData();

				view.update(condition);
			} else {
				State state = command.getState(RegistryToggleState.STATE_ID);
				state.setValue(false);
			}
		} else {
			// 検索条件クリア
			view.update(null);
		}
		return null;
	}
}
