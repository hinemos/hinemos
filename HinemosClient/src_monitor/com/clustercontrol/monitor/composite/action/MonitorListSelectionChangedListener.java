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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.monitor.view.MonitorListView;

/**
 * 監視[一覧]ビューのテーブルビューア用のSelectionChangedListenerクラス<BR>
 * 
 * @version 4.0.0
 * @since 4.0.0
 */
public class MonitorListSelectionChangedListener implements
ISelectionChangedListener {

	/** ログ */
	private static Log m_log = LogFactory.getLog(MonitorListSelectionChangedListener.class);

	/**
	 * 選択変更時に呼び出されます。<BR>
	 * 監視[一覧]ビューのテーブルビューアを選択した際に、<BR>
	 * 選択した行の内容でビューのアクションの有効・無効を設定します。
	 * 
	 * @param event 選択変更イベント
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		//監視[一覧]ビューのインスタンスを取得
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IViewPart viewPart = page.findView(MonitorListView.ID);

		//選択アイテムを取得
		StructuredSelection selection = (StructuredSelection) event.getSelection();

		if ( viewPart != null && selection != null) {
			MonitorListView view = (MonitorListView) viewPart.getAdapter(MonitorListView.class);

			if (view == null) {
				m_log.info("selection changed: view is null"); 
				return;
			}

			//ビューのボタン（アクション）の使用可/不可を設定する
			view.setEnabledAction(selection.size(), event.getSelection());
		}
	}

}
