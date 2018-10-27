/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite.action;

import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

import com.clustercontrol.jobmanagement.composite.ParameterComposite;

/**
 * ジョブ変数タブのテーブルビューア用のSelectionChangedListenerです。
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class ParameterSelectionChangedListener implements ISelectionChangedListener {
	/** ジョブ変数タブ用のコンポジット */
	private ParameterComposite m_composite;

	/**
	 * コンストラクタ
	 *
	 * @param composite ジョブ変数タブ用のコンポジット
	 */
	public ParameterSelectionChangedListener(ParameterComposite composite) {
		m_composite = composite;
	}

	/**
	 * 選択変更時に呼び出されます。
	 * <P>
	 * <ol>
	 * <li>選択変更イベントから選択アイテムを取得します。</li>
	 * <li>選択アイテムをジョブ変数タブ用のコンポジットに設定します。</li>
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
