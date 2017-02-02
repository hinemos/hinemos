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

package com.clustercontrol.infra.composite.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.infra.view.InfraModuleView;

/**
 * 環境構築モジュールビューのテーブルビューアのSelectionChangedListenerです。
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class InfraModuleSelectionChangedListener implements ISelectionChangedListener {
	/** ログ */
	private static Log m_log = LogFactory.getLog(InfraModuleSelectionChangedListener.class);
	/**
	 * コンストラクタ
	 * 
	 * @param composite 環境構築モジュールビュー用のコンポジット
	 */
	public InfraModuleSelectionChangedListener() {
	}

	/**
	 * 選択変更時に呼び出されます。<BR>
	 * 環境構築[モジュール]ビューを選択した際に、<BR>
	 * 選択した行の内容でアクションの可・不可を更新します。
	 * 
	 * @param event 選択変更イベント
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		//アクティブページを手に入れる
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		//ビューを更新する
		IViewPart viewPart = page.findView(InfraModuleView.ID);
		if (viewPart != null && event.getSelection() != null) {
			InfraModuleView view = (InfraModuleView) viewPart.getAdapter(InfraModuleView.class);
			if (view == null) {
				m_log.info("selection changed: view is null");
				return;
			}
			view.setEnabledAction();
		}
	}
}

