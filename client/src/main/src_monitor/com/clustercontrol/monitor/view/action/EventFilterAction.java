/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RegistryToggleState;

import com.clustercontrol.bean.Property;
import com.clustercontrol.monitor.dialog.EventFilterDialog;
import com.clustercontrol.monitor.view.EventView;

/**
 * 監視[イベントのフィルタ処理]ダイアログによるイベントの取得処理を行うクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class EventFilterAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(EventFilterAction.class);

	/** アクションID */
	public static final String ID = EventFilterAction.class.getName();

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
	 * 監視[イベントのフィルタ処理]ダイアログで指定された条件に一致するイベント情報を取得し、
	 * ビューを更新します。
	 * <p>
	 * <ol>
	 * <li>監視[イベントのフィルタ処理]ダイアログを表示します。</li>
	 * <li>ダイアログで指定された検索条件を取得します。</li>
	 * <li>監視[イベント]ビューの検索条件に設定します。</li>
	 * <li>監視[イベント]ビューを更新します。</li>
	 * </ol>
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.monitor.dialog.EventFilterDialog
	 * @see com.clustercontrol.monitor.view.EventView#setCondition(Property)
	 * @see com.clustercontrol.monitor.view.EventView#update()
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);

		EventView view = null;
		try {
			view = (EventView) this.viewPart.getAdapter(EventView.class);
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
			EventFilterDialog dialog = new EventFilterDialog(this.viewPart
					.getSite().getShell());

			// ダイアログにて検索が選択された場合、検索結果をビューに表示
			if (dialog.open() == IDialogConstants.OK_ID) {

				Property condition = dialog.getInputData();

				view.setCondition(condition);
				view.update(false);
			} else {
				State state = command.getState(RegistryToggleState.STATE_ID);
				state.setValue(false);
			}
		} else {
			// 検索条件クリア
			view.setCondition(null);
			// スコープツリーのアイテムを再選択(スコープパス文字列の再表示のため)
			TreeViewer tree = view.getScopeTreeComposite().getTreeViewer();

			// スコープツリーのアイテムが選択されていた場合
			if (((StructuredSelection) tree.getSelection()).getFirstElement() != null) {
				tree.setSelection(tree.getSelection()); // ビューの更新も行われる
			}
			// スコープツリーのアイテムが選択されていない場合
			else {
				view.update(false);
			}
		}
		return null;
	}
}
