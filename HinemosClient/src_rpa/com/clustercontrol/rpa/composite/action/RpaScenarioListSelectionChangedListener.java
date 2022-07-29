/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.composite.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.rpa.action.GetRpaScenarioListTableDefine;
import com.clustercontrol.rpa.view.RpaScenarioListView;

/**
 * RPAシナリオ実績[シナリオ一覧]ビューのテーブルビューア用のSelectionChangedListenerクラス<BR>
 */
public class RpaScenarioListSelectionChangedListener implements
ISelectionChangedListener {

	/** ログ */
	private static Log log = LogFactory.getLog(RpaScenarioListSelectionChangedListener.class);

	/**
	 * 選択変更時に呼び出されます。<BR>
	 * RPAシナリオ実績[シナリオ一覧]ビューのテーブルビューアを選択した際に、<BR>
	 * 選択した行の内容でビューのアクションの有効・無効を設定します。
	 * 
	 * @param event 選択変更イベント
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		//RPAシナリオ実績[シナリオ一覧]ビューのインスタンスを取得
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IViewPart viewPart = page.findView(RpaScenarioListView.ID);

		//選択アイテムを取得
		StructuredSelection selection = (StructuredSelection) event.getSelection();
		String selectScenarioId = null;
		if ( viewPart != null && selection != null) {
			List<?> list = selection.toList();
			for (Object obj : list) {
				ArrayList<?> item = (ArrayList<?>)obj;
				selectScenarioId = (String) item.get(GetRpaScenarioListTableDefine.SCENARIO_ID);
			}
			RpaScenarioListView view = (RpaScenarioListView) viewPart.getAdapter(RpaScenarioListView.class);

			if (view == null) {
				log.info("selection changed: view is null"); 
				return;
			}

			//ビューのボタン（アクション）の使用可/不可を設定する
			view.setEnabledAction(selection.size(), selectScenarioId, event.getSelection());
		}
	}

}
