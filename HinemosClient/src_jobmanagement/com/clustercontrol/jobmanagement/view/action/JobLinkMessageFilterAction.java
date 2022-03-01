/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
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

import com.clustercontrol.bean.Property;
import com.clustercontrol.jobmanagement.dialog.JobLinkMessageFilterDialog;
import com.clustercontrol.jobmanagement.view.JobLinkMessageView;

/**
 * ジョブ履歴[受信ジョブ連携メッセージ一覧]ビューの「フィルタ処理」のクライアント側アクションクラス<BR>
 *
 */
public class JobLinkMessageFilterAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(JobLinkMessageFilterAction.class);
	/** アクションID */
	public static final String ID = JobLinkMessageFilterAction.class.getName();
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
	 * ジョブ履歴[受信ジョブ連携メッセージ一覧]ビューの「フィルタ処理」が押された場合に、フィルタ条件に該当する情報を表示します。
	 * <p>
	 * <ol>
	 * <li>ジョブ履歴[受信ジョブ連携メッセージ一覧のフィルタ処理]ダイアログを表示します。</li>
	 * <li>ジョブ履歴[受信ジョブ連携メッセージ一覧]ビューにフィルタ条件を設定します。</li>
	 * <li>ジョブ履歴[受信ジョブ連携メッセージ一覧]ビューを更新します。</li>
	 * </ol>
	 *
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);

		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		JobLinkMessageView jobLinkMessageView = null;
		try {
			jobLinkMessageView = (JobLinkMessageView) viewPart.getAdapter(JobLinkMessageView.class);
		} catch (Exception e) {
			m_log.info("execute " + e.getMessage());
			return null;
		}

		if (jobLinkMessageView == null) {
			m_log.info("execute: view is null");
			return null;
		}

		ICommandService commandService = (ICommandService) window.getService(ICommandService.class);
		Command command = commandService.getCommand(ID);
		boolean isChecked = !HandlerUtil.toggleCommandState(command);

		if (isChecked) { // ボタンが押された場合

			JobLinkMessageFilterDialog dialog = new JobLinkMessageFilterDialog(
					HandlerUtil.getActiveWorkbenchWindow(event).getShell());

			// フィルタダイアログを表示
			if (dialog.open() == IDialogConstants.OK_ID) {
				// ジョブ履歴ビューをフィルタモードで表示
				Property property = dialog.getInputData();
				jobLinkMessageView.setFilterCondition(property);
				jobLinkMessageView.update();
			} else {
				State state = command.getState(RegistryToggleState.STATE_ID);
				state.setValue(false);
			}
		} else { // ボタンが戻った場合
			// ビューを通常モードで表示
			jobLinkMessageView.setFilterCondition(null);
			jobLinkMessageView.update();
		}
		return null;
	}
}
