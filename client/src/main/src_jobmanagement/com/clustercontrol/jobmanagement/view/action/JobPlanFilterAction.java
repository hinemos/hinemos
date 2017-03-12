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

package com.clustercontrol.jobmanagement.view.action;

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
import com.clustercontrol.jobmanagement.dialog.PlanFilterDialog;
import com.clustercontrol.jobmanagement.util.JobPropertyUtil;
import com.clustercontrol.jobmanagement.view.JobPlanListView;

/**
 * ジョブ[スケジュール予定]ビューの「フィルタ処理」のクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class JobPlanFilterAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(JobPlanFilterAction.class);
	/** アクションID */
	public static final String ID = JobPlanFilterAction.class.getName();
	private IWorkbenchWindow window;
	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
		this.window = null;
	}

	/**
	 * ジョブ[スケジュール予定]ビューの「フィルタ処理」が押された場合に、
	 * フィルタ条件に該当するスケジュール予定一覧情報を表示します。
	 * <p>
	 * <ol>
	 * <li>ジョブ[スケジュール予定フィルタ処理]ダイアログを表示します。</li>
	 * <li>ジョブ[スケジュール予定]ビューにフィルタ条件を設定します。</li>
	 * <li>ジョブ[スケジュール予定]ビューを更新します。</li>
	 * </ol>
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.jobmanagement.view.JobHistoryView
	 * @see com.clustercontrol.jobmanagement.dialog.HistoryFilterDialog
	 * @see com.clustercontrol.bean.Property
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);

		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		JobPlanListView view = null;
		try {
			view = (JobPlanListView) viewPart.getAdapter(JobPlanListView.class);
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

		if (isChecked) { // ボタンが押された場合

			PlanFilterDialog dialog = new PlanFilterDialog(HandlerUtil.getActiveWorkbenchWindow( event ).getShell());

			//ジョブ[スケジュール予定]フィルタダイアログを表示
			if (dialog.open() == IDialogConstants.OK_ID) {
				// ジョブ[スケジュール予定]ビューをフィルタモードで表示
				Property property = dialog.getInputData();
				view.setFilterCondition(JobPropertyUtil.getManagerName(property),
						JobPropertyUtil.property2jobPlanFilter(property));
				view.update();
			} else {
				State state = command.getState(RegistryToggleState.STATE_ID);
				state.setValue(false);
			}
		} else { // ボタンが戻った場合
			// ジョブ[スケジュール予定]ビューを通常モードで表示
			view.setFilterCondition(null, null);
			view.update();
		}
		return null;
	}
}
