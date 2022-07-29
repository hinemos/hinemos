/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.view.action;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.rpa.action.GetRpaScenarioListTableDefine;
import com.clustercontrol.rpa.composite.RpaScenarioListComposite;
import com.clustercontrol.rpa.dialog.RpaScenarioDialog;
import com.clustercontrol.rpa.view.RpaScenarioListView;
import com.clustercontrol.util.Messages;

/**
 * RPAシナリオ実績[シナリオ一覧]ビューの編集アクションクラス<BR>
 */
public class RpaScenarioModifyAction extends AbstractHandler implements IElementUpdater {

	// ログ
	private static Log log = LogFactory.getLog( RpaScenarioModifyAction.class );

	/** アクションID */
	public static final String ID = RpaScenarioModifyAction.class.getName();

	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		RpaScenarioListView view = (RpaScenarioListView) this.viewPart
				.getAdapter(RpaScenarioListView.class);
		if (view == null) {
			log.info("execute: view is null"); 
			return null;
		}
		RpaScenarioListComposite composite = (RpaScenarioListComposite) view
				.getListComposite();
		StructuredSelection selection = (StructuredSelection) composite
				.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.getFirstElement();
		String managerName = null;
		String scenarioId = null;
		if (list != null && list.size() > 0) {
			managerName = (String) list.get(GetRpaScenarioListTableDefine.MANAGER_NAME);
			scenarioId = (String) list.get(GetRpaScenarioListTableDefine.SCENARIO_ID);
		}
		Table table = composite.getTableViewer().getTable();

		// 選択アイテムがある場合に、編集ダイアログを表示する
		if (scenarioId != null) {

			try {
				// ダイアログを生成
				RpaScenarioDialog dialog = new RpaScenarioDialog(
						this.viewPart.getSite().getShell(), managerName, scenarioId,
						PropertyDefineConstant.MODE_MODIFY);

				// ダイアログにて変更が選択された場合、入力内容をもって更新を行う。
				if (dialog.open() == IDialogConstants.OK_ID) {
					int selectIndex = table.getSelectionIndex();
					view.update();
					table.setSelection(selectIndex);
				}
			} catch (Exception e1) {
				log.warn("run(), " + e1.getMessage(), e1);
			}

		} else {
			MessageDialog.openWarning(null, 
					Messages.getString("warning"),
					Messages.getString("message.rpa.scenario.4"));
		}
		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if( null != window ){
			IWorkbenchPage page = window.getActivePage();
			if( null != page ){
				IWorkbenchPart part = page.getActivePart();

				boolean editEnable = false;
				if(part instanceof RpaScenarioListView){
					// Enable button when 1 item is selected
					RpaScenarioListView view = (RpaScenarioListView)part;

					if(view.getSelectedNum() == 1) {
						editEnable = true;
					}
				}
				this.setBaseEnabled(editEnable);
			} else {
				this.setBaseEnabled(false);
			}
		}
	}

}
