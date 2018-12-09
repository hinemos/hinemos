/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite.action;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

import com.clustercontrol.jobmanagement.composite.WaitRuleComposite;

/**
 * 待ち条件タブのテーブルビューア用のSelectionChangedListenerです。
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class WaitRuleSelectionChangedListener implements ISelectionChangedListener {
	/** 待ち条件タブ用のコンポジット */
	private WaitRuleComposite m_composite;

	/**
	 * コンストラクタ
	 * 
	 * @param composite 待ち条件タブ用のコンポジット
	 */
	public WaitRuleSelectionChangedListener(WaitRuleComposite composite) {
		m_composite = composite;
	}

	/**
	 * 選択変更時に呼び出されます。
	 * <P>
	 * <ol>
	 * <li>選択変更イベントから選択アイテムを取得します。</li>
	 * <li>選択アイテムを待ち条件タブ用のコンポジットに設定します。</li>
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
			@SuppressWarnings("unchecked")
			ArrayList<Object> info = (ArrayList<Object>) ((StructuredSelection) event.getSelection()).getFirstElement();
			m_composite.setSelectItem(info);
		} else {
			m_composite.setSelectItem(null);
		}
	}
}
