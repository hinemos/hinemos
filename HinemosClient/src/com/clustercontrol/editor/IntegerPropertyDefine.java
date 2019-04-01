/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.editor;

import java.io.Serializable;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TextCellEditor;

import com.clustercontrol.bean.Property;
import com.clustercontrol.util.Messages;

/**
 * 数値プロパティ(Integer)を定義するクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class IntegerPropertyDefine extends PropertyDefine implements Serializable {
	private static final long serialVersionUID = 3562301924650606249L;

	/**
	 * コンストラクタ
	 */
	public IntegerPropertyDefine() {
		m_cellEditor = new TextCellEditor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clustercontrol.bean.PropertyDefine#getColumnText(java.lang.Object)
	 */
	@Override
	public String getColumnText(Object value) {
		if (value instanceof Integer) {
			return value.toString();
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
		Object value = element.getValue();
		if (value instanceof Integer) {
			return value.toString();
		}
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clustercontrol.bean.PropertyDefine#modify(com.clustercontrol.bean.Property,
	 *      java.lang.Object)
	 */
	@Override
	public void modify(Property element, Object value) {
		try {
			if(((String)value).length() == 0){
				element.setValue("");
			}
			else if ( element.getLowerBound() <= Integer.parseInt((String)value) && Integer.parseInt((String)value) <= element.getUpperBound() ){
				Integer check = Integer.valueOf(value.toString());
				element.setValue(check);
			}
			else {
				String[] args = { String.valueOf(element.getLowerBound()), String.valueOf(element.getUpperBound()) };

				//エラーメッセージ
				MessageDialog.openWarning(
						null,
						Messages.getString("message.hinemos.1"),
						Messages.getString("message.hinemos.8", args ));
			}
		} catch (NumberFormatException e) {

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
