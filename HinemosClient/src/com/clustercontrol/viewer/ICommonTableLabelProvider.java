/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.viewer;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Color;

/**
 * CommonTableViewerクラス用のLabelProviderインターフェース<BR>
 * 
 * ITableLabelProviderに色情報を追加
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ICommonTableLabelProvider extends ITableLabelProvider {
	/**
	 * 与えられた要素、カラムインデックスのためのラベルカラーを返す
	 * 
	 * @param element
	 * @param columnIndex
	 * @return 色
	 */
	public Color getColumnColor(Object element, int columnIndex);
}
