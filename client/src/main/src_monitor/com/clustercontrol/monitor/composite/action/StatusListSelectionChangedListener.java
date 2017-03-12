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

package com.clustercontrol.monitor.composite.action;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.monitor.action.GetStatusListTableDefine;
import com.clustercontrol.monitor.view.StatusView;

/**
 * 監視[ステータス]ビューのテーブルビューア用のSelectionChangedListenerクラス<BR>
 *
 * @version 2.2.0
 * @since 2.2.0
 */
public class StatusListSelectionChangedListener implements ISelectionChangedListener {

	/** ログ */
	private static Log m_log = LogFactory.getLog(StatusListSelectionChangedListener.class);

	/**
	 * コンストラクタ
	 *
	 * @param list 監視[ステータス]ビュー用のコンポジット
	 */
	public StatusListSelectionChangedListener() {

	}

	/**
	 * 選択変更時に呼び出されます。<BR>
	 * 監視[ステータス]ビューのテーブルビューアを選択した際に、<BR>
	 * 選択した行の内容でビューのアクションの有効・無効を設定します。
	 * <P>
	 * <ol>
	 * <li>選択変更イベントから選択行を取得し、選択行からイベントの表示内容を取得します。</li>
	 * <li>取得したイベントから監視[ステータス]ビューのアクションの有効・無効を設定します。</li>
	 * </ol>
	 *
	 * @param event 選択変更イベント
	 *
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		//監視[ステータス]ビューのインスタンスを取得
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IViewPart viewPart = page.findView(StatusView.ID);

		// 見つからない場合ノードマップのビューを検索
		if (viewPart == null) {
			viewPart = page.findView("com.clustercontrol.nodemap.view.StatusViewM");
		}
		
		//選択アイテムを取得
		StructuredSelection selection = (StructuredSelection) event.getSelection();

		if ( viewPart != null && selection != null) {
			StatusView view = (StatusView) viewPart.getAdapter(StatusView.class);

			if (view == null) {
				m_log.info("selection changed: view is null");
				return;
			}

			//監視[ステータス]ビューのボタン（アクション）の使用可/不可を設定する
			ArrayList<?> list = (ArrayList<?>) selection.getFirstElement();
			if (list != null && list.size() > 0) {
				String pluginId = list.get(GetStatusListTableDefine.PLUGIN_ID).toString();
				view.setEnabledAction(selection.size(), pluginId, selection);
			} else {
				view.initButton();
			}
		}
	}
}
