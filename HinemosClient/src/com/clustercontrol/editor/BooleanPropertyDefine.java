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

import org.eclipse.jface.viewers.CheckboxCellEditor;

import com.clustercontrol.bean.Property;

/**
 * チェックボックスプロパティ定義クラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class BooleanPropertyDefine extends PropertyDefine implements Serializable {
	private static final long serialVersionUID = 3576068152251206368L;

	/**
	 * コンストラクタ
	 */
	public BooleanPropertyDefine() {
		m_cellEditor = new CheckboxCellEditor();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clustercontrol.bean.PropertyDefine#getColumnText(java.lang.Object)
	 */
	@Override
	public String getColumnText(Object value) {
		//値から表示テキスト
		if (value instanceof Boolean) {
			if (((Boolean) value).booleanValue()) {
				return "";
			} else {
				return "";
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
		//Checkboxに渡すために値からBooleanに変換
		Object value = element.getValue();
		if (value instanceof Boolean) {
			return value;
		} else {
			return Boolean.FALSE;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clustercontrol.bean.PropertyDefine#modify(com.clustercontrol.bean.Property,
	 *      java.lang.Object)
	 */
	@Override
	public void modify(Property element, Object value) {
		//Booleanから値に変換
		if (value instanceof Boolean) {
			element.setValue(value);
		} else {
			element.setValue(Boolean.FALSE);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clustercontrol.bean.PropertyDefine#initEditer()
	 */
	@Override
	public void initEditer() {

	}
}
