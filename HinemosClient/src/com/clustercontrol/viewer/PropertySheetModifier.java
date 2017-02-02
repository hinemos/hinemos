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

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.Item;

import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;

/**
 * プロパティシートクラス用のModifierクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class PropertySheetModifier implements ICellModifier {
	private PropertySheet m_viewer;

	/**
	 * コンストラクタ
	 * 
	 * @param viewer
	 */
	public PropertySheetModifier(PropertySheet viewer) {
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
		if (PropertySheet.CLMN_VALUE.equals(property)) {
			if (element instanceof Property) {

				Property nodeProperty = (Property) element;

				if (nodeProperty.getModify() == PropertyDefineConstant.MODIFY_OK) {
					// ここでエディターを定義体から取り出して、エディターを切り替える。
					CellEditor cellEditor = nodeProperty.getCellEditor();

					if (cellEditor.getControl() == null || cellEditor.getControl().isDisposed()) {
						cellEditor.dispose();
						cellEditor.create(m_viewer.getTree());
						nodeProperty.initEditer();
					}

					CellEditor[] editors = new CellEditor[] { null, cellEditor };
					m_viewer.setCellEditors(editors);

					return true;
				}
				else{
					if(nodeProperty.getEditor().compareTo(
							PropertyDefineConstant.EDITOR_TEXTAREA) == 0){
						// ここでエディターを定義体から取り出して、エディターを切り替える。
						CellEditor cellEditor = nodeProperty.getCellEditor();

						if (cellEditor.getControl() == null || cellEditor.getControl().isDisposed()) {
							cellEditor.dispose();
							cellEditor.create(m_viewer.getTree());
							nodeProperty.initEditer();
						}

						CellEditor[] editors = new CellEditor[] { null, cellEditor };
						m_viewer.setCellEditors(editors);

						return true;
					}
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object,
	 *      java.lang.String)
	 */
	@Override
	public Object getValue(Object element, String property) {
		if (PropertySheet.CLMN_VALUE.equals(property)) {
			//定義体から値を取り出す
			if (element instanceof Property) {
				Property nodeProperty = (Property) element;
				return nodeProperty.getModifyValue();
			}
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
		if (PropertySheet.CLMN_VALUE.equals(property)) {

			//定義体からの値更新用メソッドを呼び出す
			if (element instanceof Item) {
				element = ((Item) element).getData();

				if (element instanceof Property) {

					//値の更新用メソッドを呼び出す
					Property nodeProperty = (Property) element;
					nodeProperty.modify(value);
					m_viewer.expandToLevel(nodeProperty, 1);

					// ビューワを更新
					m_viewer.refresh();
				}
			}
		}
	}
}
