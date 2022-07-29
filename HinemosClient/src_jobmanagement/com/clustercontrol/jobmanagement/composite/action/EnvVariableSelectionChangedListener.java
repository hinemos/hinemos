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

import com.clustercontrol.jobmanagement.composite.EnvVariableComposite;

/**
 * 環境変数のテーブルビューア用のSelectionChangedListenerです。
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class EnvVariableSelectionChangedListener implements ISelectionChangedListener {
	/** 環境変数コンポジット */
	private EnvVariableComposite m_composite;

	/**
	 * コンストラクタ
	 *
	 * @param composite 環境変数コンポジット
	 */
	public EnvVariableSelectionChangedListener(EnvVariableComposite composite) {
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
