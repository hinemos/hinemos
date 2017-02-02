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

package com.clustercontrol.jobmanagement.composite.action;

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

import com.clustercontrol.jobmanagement.composite.JobKickListComposite;
import com.clustercontrol.jobmanagement.view.JobKickListView;

/**
 * ジョブ[実行契機]ビューのテーブルビューア用のSelectionChangedListenerです。
 *
 * @version 4.1.0
 * @since 1.0.0
 */
public class JobKickSelectionChangedListener implements ISelectionChangedListener {
	/** ログ */
	private static Log m_log = LogFactory.getLog(JobKickSelectionChangedListener.class);
	/** ジョブ[スケジュール]ビュー用のコンポジット */
	private JobKickListComposite m_composite;

	/**
	 * コンストラクタ
	 *
	 * @param composite ジョブ[実行契機]ビュー用のコンポジット
	 */
	public JobKickSelectionChangedListener(JobKickListComposite composite) {
		m_composite = composite;
	}

	/**
	 * 選択変更時に呼び出されます。
	 * <P>
	 * <ol>
	 * <li>選択変更イベントから選択アイテムを取得します。</li>
	 * <li>選択アイテムをジョブ[スケジュール]ビュー用のコンポジットに設定します。</li>
	 * </ol>
	 *
	 * @param event 選択変更イベント
	 *
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ArrayList<ArrayList<?>> info = new ArrayList<ArrayList<?>>();
		List<?> list;

		//ジョブ[実行契機]ビューのインスタンスを取得
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IViewPart viewPart = page.findView(JobKickListView.ID);

		//選択行を取得
		StructuredSelection selection = (StructuredSelection) event.getSelection();

		if (selection.getFirstElement() != null) {
			list = ((StructuredSelection) event.getSelection()).toList();
			for(Object obj : list) {
				if(obj instanceof ArrayList) {
					info.add((ArrayList<?>)obj);
				}
			}

			if (viewPart != null) {
				JobKickListView view = (JobKickListView) viewPart.getAdapter(JobKickListView.class);
				if (view == null) {
					m_log.info("selection changed: view is null"); 
					return;
				}
				//選択アイテムの確認/未確認の種別でボタン（アクション）の使用可/不可を設定する
				view.setEnabledAction(selection.size(), selection);
			}
		} else {
			if (viewPart != null) {
				JobKickListView view = (JobKickListView) viewPart.getAdapter(JobKickListView.class);
				if (view == null) {
					m_log.info("selection changed: view is null"); 
					return;
				}
				//選択アイテムの確認/未確認の種別でボタン（アクション）の使用可/不可を設定する
				view.setEnabledAction(0, selection);
			}
		}

		m_composite.setSelectItem(info);
	}
}

