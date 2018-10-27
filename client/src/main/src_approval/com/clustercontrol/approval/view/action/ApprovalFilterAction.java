/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.approval.view.action;

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

import com.clustercontrol.approval.dialog.ApprovalFilterDialog;
import com.clustercontrol.approval.view.ApprovalView;
import com.clustercontrol.bean.Property;

/**
 * 承認ビューの「フィルタ処理」のクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class ApprovalFilterAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(ApprovalFilterAction.class);
	/** アクションID */
	public static final String ID = ApprovalFilterAction.class.getName();
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
	 * 承認ビューの「フィルタ処理」が押された場合に、フィルタ条件に該当する承認ジョブ一覧情報を表示します。
	 * <p>
	 * <ol>
	 * <li>承認ビュー[フィルタ処理]ダイアログを表示します。</li>
	 * <li>承認ビューにフィルタ条件を設定します。</li>
	 * <li>承認ビューを更新します。</li>
	 * </ol>
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.jobmanagement.view.JobHistoryView
	 * @see com.clustercontrol.jobmanagement.dialog.ApprovalFilterDialog
	 * @see com.clustercontrol.bean.Property
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		
		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		ApprovalView view = null;
		try {
			view = (ApprovalView) this.viewPart.getAdapter(ApprovalView.class);
		} catch (Exception e) { 
			m_log.warn("execute " + e.getMessage());
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
			
			ApprovalFilterDialog dialog = new ApprovalFilterDialog(HandlerUtil.getActiveWorkbenchWindow( event ).getShell());
			
			//フィルタダイアログを表示
			if (dialog.open() == IDialogConstants.OK_ID) {
				// ビューをフィルタモードで表示
				Property property = dialog.getInputData();
				view.setFilterCondition(property);
				view.update();
			} else {
				State state = command.getState(RegistryToggleState.STATE_ID);
				state.setValue(false);
			}
		} else { // ボタンが戻った場合
			// ビューを通常モードで表示
			view.setFilterCondition(null);
			view.update();
		}
		return null;

	}
}
