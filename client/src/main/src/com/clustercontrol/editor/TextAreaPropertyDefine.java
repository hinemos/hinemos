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

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.util.Messages;

/**
 * テキストエリアプロパティ定義クラス<BR>
 * 
 * @version 2.0.0
 * @since 2.0.0
 */
public class TextAreaPropertyDefine extends PropertyDefine implements Serializable {

	private static final long serialVersionUID = -5855792499351692500L;

	/** テキストエリアダイアログ タイトル */
	private String m_title = null;

	/** プロパティ値変更の可/不可 */
	private boolean m_modify = false;

	/**
	 * コンストラクタ
	 * 
	 * @since 2.0.0
	 */
	public TextAreaPropertyDefine() {
		m_cellEditor = new TextAreaDialogCellEditor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clustercontrol.bean.PropertyDefine#getColumnText(java.lang.Object)
	 */
	@Override
	public String getColumnText(Object value) {
		if (value instanceof String) {
			return (String) value;
		} else {
			return "";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clustercontrol.bean.PropertyDefine#getValue(com.clustercontrol.bean.Property)
	 */
	@Override
	public Object getValue(Property element) {
		Object value = element.getValue();
		if (value instanceof String) {
			return value;
		} else {
			return "";
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
		if (value instanceof String) {

			//try {
			if (element.getStringUpperValue() == DataRangeConstant.TEXT) {
				element.setValue(value);
			}
			//else if ( ((String) value).getBytes("UTF-8").length <= element.getStringUpperValue() ) {
			else if ( ((String) value).length() <= element.getStringUpperValue() ) {
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clustercontrol.bean.PropertyDefine#initEditer()
	 */
	@Override
	public void initEditer() {
		((TextAreaDialogCellEditor)m_cellEditor).setTitle(m_title);
		((TextAreaDialogCellEditor)m_cellEditor).setModify(m_modify);
	}

	/**
	 * タイトル設定
	 * 
	 * @param title ダイアログのタイトル
	 */
	public void setTitle(String title) {
		m_title = title;
	}

	/**
	 * タイトル取得
	 * 
	 * @return m_title
	 */
	public String getTitle() {
		return m_title;
	}

	/**
	 * プロパティ値変更の可/不可設定
	 * 
	 * @param title プロパティ値変更の可/不可
	 */
	public void setModify(boolean modify) {
		m_modify = modify;
	}

	/**
	 * プロパティ値変更の可/不可取得
	 * 
	 * @return m_modify
	 */
	public boolean getmodify() {
		return m_modify;
	}
}
