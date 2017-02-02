/*

Copyright (C) 2016 NTT DATA Corporation

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
import com.clustercontrol.jobmanagement.dialog.JobKickFilterDialog;
import com.clustercontrol.jobmanagement.view.JobKickListView;

/**
 * ジョブ設定[実行契機のフィルタ処理]ダイアログによるジョブ実行契機情報の取得処理を行うクライアント側アクションクラス<BR>
 *
 * @version 5.1.0
 */
public class JobKickFilterAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(JobKickFilterAction.class);

	/** アクションID */
	public static final String ID = JobKickFilterAction.class.getName();

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
	 * ジョブ設定[実行契機のフィルタ処理]ダイアログで指定された条件に一致するジョブ実行契機情報を取得し、
	 * ビューを更新します。
	 * <p>
	 * <ol>
	 * <li>ジョブ設定[実行契機のフィルタ処理]ダイアログを表示します。</li>
	 * <li>ダイアログで指定された検索条件を取得します。</li>
	 * <li>ジョブ設定[実行契機]ビューの検索条件に設定します。</li>
	 * <li>ジョブ設定[実行契機]ビューを更新します。</li>
	 * </ol>
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.jobmanagement.dialog.JobKickFilterDialog
	 * @see com.clustercontrol.jobmanagement.view.JobKickListView#setFilterCondition(Property)
	 * @see com.clustercontrol.jobmanagement.view.JobKickListView#update()
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);

		JobKickListView view = null;
		try {
			view = (JobKickListView) this.viewPart.getAdapter(JobKickListView.class);
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
			JobKickFilterDialog dialog = new JobKickFilterDialog(HandlerUtil.getActiveWorkbenchWindow( event ).getShell());

			// ダイアログにて検索が選択された場合、検索結果をビューに表示
			if (dialog.open() == IDialogConstants.OK_ID) {
				Property condition = dialog.getInputData();
				view.setFilterCondition(condition);
				view.update();
			} else {
				State state = command.getState(RegistryToggleState.STATE_ID);
				state.setValue(false);
			}
		} else {
			view.setFilterCondition(null);
			view.update();
		}
		return null;
	}
}
