/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
import org.openapitools.client.model.JobHistoryFilterBaseRequest;

import com.clustercontrol.filtersetting.bean.JobHistoryFilterContext;
import com.clustercontrol.filtersetting.util.JobHistoryFilterHelper;
import com.clustercontrol.jobmanagement.dialog.JobHistoryFilterDialog;
import com.clustercontrol.jobmanagement.view.JobHistoryView;
import com.clustercontrol.util.ManagerTag;

/**
 * ジョブ[履歴]ビューの「フィルタ処理」のクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class HistoryFilterAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(HistoryFilterAction.class);
	/** アクションID */
	public static final String ID = HistoryFilterAction.class.getName();
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
	 * ジョブ[履歴]ビューの「フィルタ処理」が押された場合に、フィルタ条件に該当する履歴一覧情報を表示します。
	 * <p>
	 * <ol>
	 * <li>ジョブ[履歴フィルタ処理]ダイアログを表示します。</li>
	 * <li>ジョブ[履歴]ビューにフィルタ条件を設定します。</li>
	 * <li>ジョブ[履歴]ビューを更新します。</li>
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
		JobHistoryView jobHistoryView = null;
		try {
			jobHistoryView = (JobHistoryView) viewPart.getAdapter(JobHistoryView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (jobHistoryView == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		ICommandService commandService = (ICommandService)window.getService(ICommandService.class);
		Command command = commandService.getCommand(ID);
		boolean isChecked = !HandlerUtil.toggleCommandState(command);

		if (isChecked) { // ボタンが押された場合
			ManagerTag<JobHistoryFilterBaseRequest> mtg = jobHistoryView.getFilter();
			JobHistoryFilterContext context = new JobHistoryFilterContext(
					JobHistoryFilterHelper.duplicate(mtg.data), // ダイアログ側で更新するので複製する必要がある
					mtg.managerName);

			JobHistoryFilterDialog dialog = new JobHistoryFilterDialog(
					HandlerUtil.getActiveWorkbenchWindow(event).getShell(),
					context);

			//ジョブ履歴フィルタダイアログを表示
			if (dialog.open() == IDialogConstants.OK_ID) {
				// ジョブ履歴ビューをフィルタモードで表示
				jobHistoryView.setFilter(context.getManagerName(), context.getFilter());
				jobHistoryView.setFilterEnabled(true);
				jobHistoryView.update(false);
			} else {
				State state = command.getState(RegistryToggleState.STATE_ID);
				state.setValue(false);
			}
		} else { // ボタンが戻った場合
			// ジョブ履歴ビューを通常モードで表示
			jobHistoryView.setFilterEnabled(false);
			jobHistoryView.update(false);
		}
		return null;
	}
}
