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
import org.openapitools.client.model.StatusFilterBaseRequest;

import com.clustercontrol.bean.Property;
import com.clustercontrol.filtersetting.bean.StatusFilterContext;
import com.clustercontrol.filtersetting.util.StatusFilterHelper;
import com.clustercontrol.monitor.dialog.StatusFilterDialog;
import com.clustercontrol.monitor.view.StatusView;

/**
 * 監視[ステータスのフィルタ処理]ダイアログによるステータスの取得処理を行うクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class StatusFilterAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(StatusFilterAction.class);

	/** アクションID */
	public static final String ID = StatusFilterAction.class.getName();

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
	 * 監視[ステータスのフィルタ処理]ダイアログで指定された条件に一致するステータス情報を取得し、
	 * ビューを更新します。
	 * <p>
	 * <ol>
	 * <li>監視[ステータスのフィルタ処理]ダイアログを表示します。</li>
	 * <li>ダイアログで指定された検索条件を取得します。</li>
	 * <li>監視[ステータス]ビューの検索条件に設定します。</li>
	 * <li>監視[ステータス]ビューを更新します。</li>
	 * </ol>
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.monitor.dialog.StatusFilterDialog
	 * @see com.clustercontrol.monitor.view.StatusView#setCondition(Property)
	 * @see com.clustercontrol.monitor.view.StatusView#update()
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}
		
		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		StatusView view = null;
		try {
			view = (StatusView) this.viewPart.getAdapter(StatusView.class);
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
			StatusFilterContext context = new StatusFilterContext(
					StatusFilterHelper.duplicate(view.getFilter()), // ダイアログ側の処理の影響を受けないように複製する
					view.getSingleSelectedManagerName(),
					view.getSelectedScopeLabel());
			StatusFilterDialog dialog = new StatusFilterDialog(
					this.viewPart.getSite().getShell(),
					context);

			// ダイアログにて検索が選択された場合、検索結果をビューに表示
			if (dialog.open() == IDialogConstants.OK_ID) {
				StatusFilterBaseRequest filter = context.getFilter();
				if (filter.getFacilityId() != null) {
					// フィルタ設定固有のスコープが指定されているのでスコープツリーへ反映
					view.selectScope(context.getManagerName(), filter.getFacilityId());
					filter.setFacilityId(null); // 後でスコープツリーの選択から再設定するのでリセットしておく
				}
				// ビューにフィルタ情報を反映
				view.setFilter(filter);
				view.setFilterEnabled(true);
				view.update(false);
			} else {
				State state = command.getState(RegistryToggleState.STATE_ID);
				state.setValue(false);
			}
		} else {
			// フィルタ無効化
			view.setFilterEnabled(false);
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
