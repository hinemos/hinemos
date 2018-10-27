/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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

