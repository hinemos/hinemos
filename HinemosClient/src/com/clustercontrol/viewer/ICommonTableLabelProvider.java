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
