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

package com.clustercontrol.editor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.SWT;

import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;

/**
 * コンボボックスプロパティ定義クラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class ComboPropertyDefine extends PropertyDefine implements Serializable {
	private static final long serialVersionUID = -2603242158303086391L;

	private Object m_values[][]; //表示文字列と、値のマップm_values[0]が表示

	/**
	 * プロパティシートでコンボボックスを表示する際に使用するコンストラクタ
	 */
	public ComboPropertyDefine() {
		m_cellEditor = new ComboBoxCellEditor();
		m_cellEditor.setStyle(SWT.READ_ONLY);
	}

	/**
	 * プロパティシートでコンボボックスを表示する際に使用するコンストラクタ
	 * 
	 * @param value コンボボックスに表示する値
	 */
	public ComboPropertyDefine(Object[][] value) {
		m_values = value;
		m_cellEditor = new ComboBoxCellEditor();
		m_cellEditor.setStyle(SWT.READ_ONLY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clustercontrol.bean.PropertyDefine#getColumnText(java.lang.Object)
	 */
	@Override
	public String getColumnText(Object value) {
		//値から表示テキスト
		for (int i = 0; i < m_values[PropertyDefineConstant.SELECT_VALUE].length; i++) {
			Object select = null;
			if (m_values[PropertyDefineConstant.SELECT_VALUE][i] instanceof HashMap) {
				@SuppressWarnings("unchecked")
				HashMap<String, Object> map = (HashMap<String, Object>) m_values[PropertyDefineConstant.SELECT_VALUE][i];
				select = map.get(PropertyDefineConstant.MAP_VALUE);
			} else {
				select = m_values[PropertyDefineConstant.SELECT_VALUE][i];
				//            	select = m_values[PropertyConstant.SELECT_DISP_TEXT][i];
			}

			if (value.equals(select)) {
				return (String) m_values[PropertyDefineConstant.SELECT_DISP_TEXT][i];
				//                return (String) select;
			}
		}
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clustercontrol.bean.PropertyDefine#getValue(com.clustercontrol.bean.Property)
	 */
	@Override
	public Object getValue(Property element) {
		//Comboに渡すために値からインデックス番号に変換
		Object value = element.getValue();
		for (int i = 0; i < m_values[PropertyDefineConstant.SELECT_VALUE].length; i++) {
			Object select = null;
			if (m_values[PropertyDefineConstant.SELECT_VALUE][i] instanceof HashMap) {
				@SuppressWarnings("unchecked")
				HashMap<String, Object> map = (HashMap<String, Object>) m_values[PropertyDefineConstant.SELECT_VALUE][i];
				select = map.get(PropertyDefineConstant.MAP_VALUE);
			} else {
				select = m_values[PropertyDefineConstant.SELECT_VALUE][i];
				//            	select = m_values[PropertyConstant.SELECT_DISP_TEXT][i];
			}

			if (value.equals(select)) {
				return i;
			}
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clustercontrol.bean.PropertyDefine#modify(com.clustercontrol.bean.Property,
	 *      java.lang.Object)
	 */
	@Override
	public void modify(Property element, Object value) {
		//インデックス番号から値に変換
		if (value instanceof Integer) {
			int valueIndex = ((Integer) value).intValue();
			if (valueIndex >= 0) {
				if (m_values[PropertyDefineConstant.SELECT_VALUE][valueIndex] instanceof HashMap) {
					@SuppressWarnings("unchecked")
					HashMap<String, Object> map = (HashMap<String, Object>) m_values[PropertyDefineConstant.SELECT_VALUE][valueIndex];
					element.setValue(map.get(PropertyDefineConstant.MAP_VALUE));

					ArrayList<?> propertyList = (ArrayList<?>)map.get(PropertyDefineConstant.MAP_PROPERTY);
					if (propertyList != null) {
						element.removeChildren();
						for (int i = 0; i < propertyList.size(); i++) {
							element.addChildren((Property)propertyList.get(i));
						}
					}
				} else {
					element.removeChildren();
					Object select = m_values[PropertyDefineConstant.SELECT_VALUE][valueIndex];
					//                    Object select = m_values[PropertyConstant.SELECT_DISP_TEXT][valueIndex];
					element.setValue(select);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clustercontrol.bean.PropertyDefine#initEditer()
	 */
	@Override
	public void initEditer() {
		//createでparentを指定した後に、リストの文字列を渡さないといけないので。
		String[] value = new String[m_values[PropertyDefineConstant.SELECT_DISP_TEXT].length];
		for (int i = 0; i < m_values[PropertyDefineConstant.SELECT_DISP_TEXT].length; i++) {
			value[i] = (String) m_values[PropertyDefineConstant.SELECT_DISP_TEXT][i];
		}
		((ComboBoxCellEditor) m_cellEditor).setItems(value);
	}

	/**
	 * @return Returns the m_values.
	 */
	public Object[][] getValues() {
		return m_values;
	}

	/**
	 * @param m_values
	 *            The m_values to set.
	 */
	public void setValues(Object[][] values) {
		this.m_values = values;
	}
}
