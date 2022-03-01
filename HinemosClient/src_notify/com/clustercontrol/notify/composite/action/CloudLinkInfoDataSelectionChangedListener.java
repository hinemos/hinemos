/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.composite.action;

import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

import com.clustercontrol.notify.composite.NotifyCloudLinkDataInfoComposite;

/**
 * 連携情報ダイアログののテーブルビューア用のSelectionChangedListenerです。
 */
public class CloudLinkInfoDataSelectionChangedListener implements ISelectionChangedListener {
	/**  連携情報コンポジット */
	private NotifyCloudLinkDataInfoComposite m_composite;

	/**
	 * コンストラクタ
	 *
	 * @param composite  連携情報コンポジット
	 */
	public CloudLinkInfoDataSelectionChangedListener(NotifyCloudLinkDataInfoComposite composite) {
		m_composite = composite;
	}

	/**
	 * 選択変更時に呼び出されます。
	 * <P>
	 * <ol>
	 * <li>選択変更イベントから選択アイテムを取得します。</li>
	 * <li>選択アイテムを環境変数コンポジットに設定します。</li>
	 * </ol>
	 *
	 * @param event 選択変更イベント
	 *
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
			//選択行を取得
			List<?> info = ((StructuredSelection) event.getSelection()).toList();

			m_composite.setSelectItem(info);
		} else {
			m_composite.setSelectItem(null);
		}
	}
}
