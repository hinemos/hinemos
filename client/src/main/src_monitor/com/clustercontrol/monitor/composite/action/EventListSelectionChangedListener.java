/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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

import com.clustercontrol.monitor.action.GetEventListTableDefine;
import com.clustercontrol.monitor.bean.ConfirmConstant;
import com.clustercontrol.monitor.view.EventView;

/**
 * 監視[イベント]ビューのテーブルビューア用のSelectionChangedListenerクラス<BR>
 *
 * @version 2.2.0
 * @since 2.2.0
 */
public class EventListSelectionChangedListener implements ISelectionChangedListener {

	/** ログ */
	private static Log m_log = LogFactory.getLog(EventListSelectionChangedListener.class);

	/**
	 * コンストラクタ
	 *
	 * @param list 監視[イベント]ビュー用のコンポジット
	 */
	public EventListSelectionChangedListener() {

	}

	/**
	 * 選択変更時に呼び出されます。<BR>
	 * 監視[イベント]ビューのテーブルビューアを選択した際に、<BR>
	 * 選択した行の内容でビューのアクションの有効・無効を設定します。
	 * <P>
	 * <ol>
	 * <li>選択変更イベントから選択行を取得し、選択行からイベントの表示内容を取得します。</li>
	 * <li>取得したイベント内容から、確認状態(CONFIRMED)を取得します。</li>
	 * <li>取得した確認状態から監視[イベント]ビューのアクションの有効・無効を設定します。</li>
	 * </ol>
	 *
	 * @param event 選択変更イベント
	 *
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ArrayList<?> list = null;

		//監視[イベント]ビューのインスタンスを取得
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IViewPart viewPart = page.findView(EventView.ID);
		
		// 見つからない場合ノードマップのビューを検索
		if (viewPart == null) {
			viewPart = page.findView("com.clustercontrol.nodemap.view.EventViewM");
		}

		//選択アイテムを取得
		StructuredSelection selection = (StructuredSelection) event.getSelection();

		if ( viewPart != null ) {
			EventView view = (EventView) viewPart.getAdapter(EventView.class);

			if (view == null) {
				m_log.info("selection changed: view is null"); 
				return;
			}

			if ( selection != null ) {
				//選択アイテムが1つの場合
				if ( selection.size() == 1) {
					list = (ArrayList<?>) selection.getFirstElement();
					String pluginId = list.get(GetEventListTableDefine.PLUGIN_ID).toString();
					view.setEnabledAction(((Integer)list.get(GetEventListTableDefine.CONFIRMED)).intValue(), pluginId, event.getSelection());

					//選択アイテムが複数の場合
				} else if (selection.size() > 1){
					Object [] obj = selection.toArray();
					boolean confirmFlg = false;
					boolean unconfirmFlg = false;

					//選択アイテムの確認/未確認を全てチェックする
					for (int i = 0; i < obj.length; i++) {
						list = (ArrayList<?>) obj[i];
						if (!confirmFlg && (Integer)list.get(GetEventListTableDefine.CONFIRMED) == ConfirmConstant.TYPE_CONFIRMED) {
							confirmFlg = true;
						} else if (!unconfirmFlg && (Integer)list.get(GetEventListTableDefine.CONFIRMED) == ConfirmConstant.TYPE_UNCONFIRMED) {
							unconfirmFlg = true;
						}
					}

					//選択アイテムの確認/未確認の種別でボタン（アクション）の使用可/不可を設定する
					//両方含まれている場合
					if (confirmFlg && unconfirmFlg) {
						view.setEnabledAction(-1, null, event.getSelection());
						//未確認しかない場合
					} else if (unconfirmFlg) {
						view.setEnabledAction(ConfirmConstant.TYPE_UNCONFIRMED, null, event.getSelection());
						//確認しかない場合
					} else if (confirmFlg) {
						view.setEnabledAction(ConfirmConstant.TYPE_CONFIRMED, null, event.getSelection());
					}

					//選択アイテムが0の場合
				} else {
					view.initButton();
				}
			} else {
				//選択アイテムがない場合
				view.initButton();
			}
		}
	}
}
