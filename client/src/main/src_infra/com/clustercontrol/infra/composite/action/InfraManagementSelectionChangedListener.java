/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.composite.action;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.infra.action.GetInfraManagementTableDefine;
import com.clustercontrol.infra.view.InfraManagementView;
import com.clustercontrol.infra.view.InfraModuleView;

/**
 * 環境構築[構築・チェック]ビューのテーブルビューア用のSelectionChangedListenerです。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class InfraManagementSelectionChangedListener implements ISelectionChangedListener {
	/** ログ */
	private static Log m_log = LogFactory.getLog(InfraManagementSelectionChangedListener.class);

	/**
	 * コンストラクタ
	 *
	 * @param composite 環境構築[構築・チェック]ビュー用のコンポジット
	 */
	public InfraManagementSelectionChangedListener(Composite composite) {
	}

	/**
	 * 選択変更時に呼び出されます。<BR>
	 * 環境構築[構築・チェック]ビューのテーブルビューアを選択した際に、選択した行の内容で
	 * 環境構築[モジュール]ビューを更新します。
	 * <P>
	 * <ol>
	 * <li>選択変更イベントから選択行を取得し、選択行からセッションIDと環境構築IDを取得します。</li>
	 * <li>セッションIDと環境構築IDを環境構築[構築・チェック]ビュー用のコンポジットに設定します。</li>
	 * <li>環境構築[モジュール]ビューを更新します。</li>
	 * </ol>
	 *
	 * @param event 選択変更イベント
	 *
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		//環境構築[構築・チェック]ビューのインスタンスを取得
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();

		//選択アイテムを取得
		StructuredSelection selection = (StructuredSelection) event.getSelection();

		//環境構築[構築・チェック]ビューの更新
		IViewPart viewPart = page.findView(InfraManagementView.ID);
		//アクションの状態を更新
		if ( viewPart != null && selection != null ) {
			InfraManagementView view = (InfraManagementView) viewPart.getAdapter(InfraManagementView.class);
			if (view == null) {
				m_log.info("selection changed: infra management view is null");
			} else {
				//環境構築[構築・チェック]ビューのボタン（アクション）の使用可/不可を設定する
				view.setEnabledAction(selection.size(), selection);
			}
		}

		//環境構築[モジュール]ビューの更新
		viewPart = page.findView(InfraModuleView.ID);
		if ( viewPart != null && selection != null) {
			InfraModuleView view = (InfraModuleView) viewPart.getAdapter(InfraModuleView.class);
			if (view == null) {
				m_log.info("execute: infra module view is null");
				return;
			}
			if(selection.size() == 1){
				ArrayList<?> info = (ArrayList<?>) selection.getFirstElement();

				//テーブル情報を更新
				view.update(
						(String) info
								.get(GetInfraManagementTableDefine.MANAGER_NAME),
						(String) info
								.get(GetInfraManagementTableDefine.MANAGEMENT_ID));
			} else {
				//アイテム無しでテーブル情報を更新
				view.update(null, null);
			}
			view.setEnabledAction();
		}
	}
}

