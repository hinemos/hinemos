/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.viewer;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.clustercontrol.monitor.run.composite.ITableItemCompositeDefine;

/**
 * 文字列監視の判定情報一覧のラベルプロバイダークラス<BR>
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class CommonTableLabelProvider<T> extends LabelProvider implements ITableLabelProvider {

	ITableItemCompositeDefine<T> m_define = null;

	/**
	 * コンストラクタ。
	 * 
	 * @since 5.0.0
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	public CommonTableLabelProvider(ITableItemCompositeDefine<T> define) {
		super();
		this.m_define = define;
	}

	/**
	 * カラム文字列を返します。
	 * 
	 * @since 5.0.0
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	@Override
	public String getColumnText(Object element, int columnIndex) {
		return "";
	}

	/**
	 * カラムイメージ(アイコン)を返します。
	 * 
	 * @since 5.0.0
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	/**
	 * カラム文字列を返します。
	 * 
	 * @since 5.0.0
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	protected int indexOf(T item) {
		return this.m_define.indexOf(item);
	}
}
