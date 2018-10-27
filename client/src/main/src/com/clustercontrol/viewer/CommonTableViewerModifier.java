/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.viewer;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ICellModifier;

import com.clustercontrol.bean.TableColumnInfo;

/**
 * CommonTableViewerクラス用のModifierクラス<BR>
 * 
 * @version 2.2.0
 * @since 2.2.0
 */
public class CommonTableViewerModifier implements ICellModifier {
	private CommonTableViewer m_viewer;

	/**
	 * コンストラクタ
	 * 
	 * @param viewer
	 */
	public CommonTableViewerModifier(CommonTableViewer viewer) {
		this.m_viewer = viewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object,
	 *      java.lang.String)
	 */
	@Override
	public boolean canModify(Object element, String property) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object,
	 *      java.lang.String)
	 */
	@Override
	public Object getValue(Object element, String property) {

		Integer index = Integer.valueOf(property);

		ArrayList<?> list = (ArrayList<?>) element;
		Object item = list.get(index);

		ArrayList<TableColumnInfo> tableColumnList = m_viewer.getTableColumnList();

		TableColumnInfo tableColumn = tableColumnList.get(index);

		if(tableColumn.getType() == TableColumnInfo.TEXT_DIALOG) {
			//上記以外のデータタイプの処理
			return item;
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object,
	 *      java.lang.String, java.lang.Object)
	 */
	@Override
	public void modify(Object element, String property, Object value) {

	}
}
