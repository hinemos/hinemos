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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TextCellEditor;

import com.clustercontrol.bean.Property;
import com.clustercontrol.util.Messages;

/**
 * テキストプロパティ定義クラス<BR>
 * 
 * @version 2.2.0
 * @since 1.0.0
 */
public class TextPropertyDefine extends PropertyDefine implements Serializable {
	private static final long serialVersionUID = 8135797079220814828L;

	/**
	 * コンストラクタ
	 */
	public TextPropertyDefine() {
		m_cellEditor = new TextCellEditor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clustercontrol.bean.PropertyDefine#getColumnText(java.lang.Object)
	 */
	@Override
	public String getColumnText(Object value) {
		return value.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clustercontrol.bean.PropertyDefine#getValue(com.clustercontrol.bean.Property)
	 */
	@Override
	public Object getValue(Property element) {
		return element.getValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clustercontrol.bean.PropertyDefine#modify(com.clustercontrol.bean.Property,
	 *      java.lang.Object)
	 */
	@Override
	public void modify(Property element, Object value) {
		//try {
		//	if ( ((String) value).getBytes("UTF-8").length <= element.getStringUpperValue() ) {
		if ( ((String) value).length() <= element.getStringUpperValue() ) {
			element.setValue(value);
		}
		else {
			String[] args = { String.valueOf(element.getStringUpperValue()) };

			//エラーメッセージ
			MessageDialog.openWarning(
					null,
					Messages.getString("message.hinemos.1"),
					Messages.getString("message.hinemos.7", args ));
		}
		/*} catch (UnsupportedEncodingException e) {
		}*/
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
