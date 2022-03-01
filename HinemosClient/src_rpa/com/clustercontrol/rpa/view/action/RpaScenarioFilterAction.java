/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.view.action;

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
import com.clustercontrol.rpa.dialog.RpaScenarioFilterDialog;
import com.clustercontrol.rpa.view.RpaScenarioListView;

/**
 * RPAシナリオ実績[シナリオ一覧]ビューのフィルタ表示アクションクラス<BR>
 */
public class RpaScenarioFilterAction extends AbstractHandler {
	
	// ログ
	private static Log log = LogFactory.getLog(RpaScenarioFilterAction.class);
			
	/** アクションID */
	public static final String ID = RpaScenarioFilterAction.class.getName();

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
		RpaScenarioListView view = null;
		try {
			view = (RpaScenarioListView) this.viewPart.getAdapter(RpaScenarioListView.class);
		} catch (Exception e) {
			log.info("execute " + e.getMessage());
			return null;
		}

		if (view == null) {
			log.info("execute: view is null"); 
			return null;
		}

		ICommandService commandService = (ICommandService)window.getService(ICommandService.class);
		Command command = commandService.getCommand(ID);
		boolean isChecked = !HandlerUtil.toggleCommandState(command);

		if (isChecked) {
			// ダイアログを生成
			RpaScenarioFilterDialog dialog = new RpaScenarioFilterDialog(this.viewPart
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
