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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.jobmanagement.view.JobHistoryView;

/**
 * ジョブ[履歴]ビューのテーブルビューア用のSelectionChangedListenerです。
 * 
 * @version 2.2.0
 * @since 2.2.0
 */
public class JobHistorySelectionChangedListener implements ISelectionChangedListener {
	/** ログ */
	private static Log m_log = LogFactory.getLog(JobHistorySelectionChangedListener.class);

	/**
	 * コンストラクタ
	 */
	public JobHistorySelectionChangedListener() {

	}

	/**
	 * 選択変更時に呼び出されます。<BR>
	 * ジョブ[履歴]ビューのテーブルビューアを選択した際に、<BR>
	 * 選択した行の内容でビューのアクションの有効・無効を設定します。
	 * <P>
	 * <ol>
	 * <li>選択変更イベントから選択行を取得し、選択行からイベントの表示内容を取得します。</li>
	 * <li>取得した確認状態からジョブ[履歴]ビューのアクションの有効・無効を設定します。</li>
	 * </ol>
	 * 
	 * @param event 選択変更イベント
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		//ジョブ[履歴]ビューのインスタンスを取得
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IWorkbenchPart viewPart = page.getActivePart();

		//選択アイテムを取得
		StructuredSelection selection = (StructuredSelection) event.getSelection();

		if ( viewPart instanceof JobHistoryView && selection != null ) {
			JobHistoryView view = (JobHistoryView) viewPart.getAdapter(JobHistoryView.class);
			if (view == null) {
				m_log.info("selection changed: view is null"); 
				return;
			}
			//ジョブ[履歴]ビューのボタン（アクション）の使用可/不可を設定する
			view.setEnabledAction(selection.size(), selection);
		}
	}
}
