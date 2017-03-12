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

package com.clustercontrol.bean;

import java.io.Serializable;

import org.eclipse.jface.viewers.CellEditor;

import com.clustercontrol.editor.PropertyDefine;

/**
 * プロパティクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class Property extends PropertyTreeItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2745261698259698643L;

	/** プロパティ値 */
	private Object m_Value;

	/** プロパティ定義 */
	private String m_Editor;

	/** プロパティ定義 */
	private PropertyDefine m_Define;

	/** プロパティ値変更の可/不可 */
	private int m_Modify;

	/** プロパティのコピー可/不可 */
	private int m_Copy;

	/** プロパティの変更値 */
	private Object m_selectValues[][];

	/** プロパティの上限値 **/
	private int m_UpperBound ;

	/** プロパティの下限値 **/
	private int m_LowerBound ;

	/** プロパティの文字数上限値 **/
	private int m_stringUpperBound;

	/**
	 * @param id
	 * @param name
	 * @param define
	 * @since 1.0.0
	 */
	public Property(String id, String name, String editor) {
		m_ID = id;
		m_Name = name;
		m_Editor = editor;
		m_Define = null;
		m_Copy = PropertyDefineConstant.COPY_NG;
		m_Modify = PropertyDefineConstant.MODIFY_NG;
	}

	/**
	 * @param id
	 * @param name
	 * @param define
	 * @param upper
	 * @param lower
	 * @since 2.2.0
	 */
	public Property(String id, String name, String editor, int upper, int lower) {
		m_ID = id;
		m_Name = name;
		m_Editor = editor;
		m_UpperBound = Integer.valueOf(upper);
		m_LowerBound = Integer.valueOf(lower);
		m_Define = null;
		m_Copy = PropertyDefineConstant.COPY_NG;
		m_Modify = PropertyDefineConstant.MODIFY_NG;
	}

	/**
	 * @param id
	 * @param name
	 * @param define
	 * @param stringUpper
	 * @since 2.2.0
	 */
	public Property(String id, String name, String editor, int stringUpper) {
		m_ID = id;
		m_Name = name;
		m_Editor = editor;
		m_stringUpperBound = Integer.valueOf(stringUpper);
		m_Define = null;
		m_Copy = PropertyDefineConstant.COPY_NG;
		m_Modify = PropertyDefineConstant.MODIFY_NG;
	}

	/**
	 * プロパティ変更の可/不可を取得します。<BR>
	 * 
	 * @return canModify
	 * @since 1.0.0
	 */
	public int getModify() {
		return m_Modify;
	}

	/**
	 * プロパティ変更の可/不可を設定します。<BR>
	 * 
	 * @param modify
	 * @since 1.0.0
	 */
	public void setModify(int modify) {
		m_Modify = modify;
	}

	/**
	 * プロパティ値を取得します。<BR>
	 * 
	 * @return プロパティ値
	 * @since 1.0.0
	 */
	public Object getValue() {
		return m_Value;
	}

	/**
	 * プロパティ値を設定します。<BR>
	 * 
	 * @param value
	 *            プロパティ値
	 * @since 1.0.0
	 */
	public void setValue(Object value) {
		m_Value = value;
	}

	/**
	 * プロパティ定義を取得します。<BR>
	 * 
	 * @return プロパティ定義
	 * @since 1.0.0
	 */
	public PropertyDefine getDefine() {
		return m_Define;
	}

	/**
	 * プロパティ定義を設定します。<BR>
	 * 
	 * @param define
	 *            プロパティ定義取得
	 * @since 1.0.0
	 */
	public void setDefine(PropertyDefine define) {
		m_Define = define;
	}

	/**
	 * プロパティの変更の可否を取得します。<BR>
	 * @return Returns the m_selectValues.
	 */
	public Object[][] getSelectValues() {
		return m_selectValues;
	}

	/**
	 * プロパティの変更の可否を設定します。<BR>
	 * @param values
	 *            The m_selectValues to set.
	 */
	public void setSelectValues(Object[][] values) {
		m_selectValues = values;
	}

	/**
	 * プロパティのコピー可否を取得します。<BR>
	 * @return Returns the m_Copy.
	 */
	public int getCopy() {
		return m_Copy;
	}

	/**
	 * プロパティのコピー可否を設定します。<BR>
	 * @param copy
	 *            The m_Copy to set.
	 */
	public void setCopy(int copy) {
		m_Copy = copy;
	}

	/**
	 * プロパティを取得します。<BR>
	 * @return Returns the m_Editor.
	 */
	public String getEditor() {
		return m_Editor;
	}

	/**
	 * プロパティを設定します。<BR>
	 * @param editor
	 *            The m_Editor to set.
	 */
	public void setEditor(String editor) {
		m_Editor = editor;
	}

	/**
	 * プロパティ値文字列を取得します。<BR>
	 * 
	 * @return プロパティ値文字列
	 * @since 1.0.0
	 */
	public String getValueText() {
		return m_Define.getColumnText(m_Value);
	}

	/**
	 * CellEditorを取得します。<BR>
	 * 
	 * @return CellEditor
	 * @since 1.0.0
	 */
	public CellEditor getCellEditor() {
		return m_Define.getCellEditor();
	}

	/**
	 * CellEditor初期化処理を行います。<BR>
	 * 
	 * @since 1.0.0
	 */
	public void initEditer() {
		m_Define.initEditer();
	}

	/**
	 * 変更値を取得します。<BR>
	 * @return
	 * @since 1.0.0
	 */
	public Object getModifyValue() {
		return m_Define.getValue(this);
	}

	/**
	 * 変更します。<BR>
	 * @param value
	 * @since 1.0.0
	 */
	public void modify(Object value) {
		m_Define.modify(this, value);
	}

	/**
	 * プロパティの上限値を取得します。<BR>
	 * 
	 * @return
	 * @since 2.2.0
	 */
	public int getUpperBound() {
		return m_UpperBound;
	}

	/**
	 * プロパティの上限値を設定します。<BR>
	 * 
	 * @param value
	 * @since 2.2.0
	 */
	public void setUpperBound(int value) {
		m_UpperBound = value;
	}

	/**
	 * プロパティの下限値を取得します。<BR>
	 * 
	 * @return
	 * @since 2.2.0
	 */
	public int getLowerBound() {
		return m_LowerBound;
	}

	/**
	 * プロパティの下限値を設定します。<BR>
	 * 
	 * @param value
	 * @since 2.2.0
	 */
	public void setLowerBound(int value) {
		m_LowerBound = value;
	}

	/**
	 * プロパティの文字数上限値を取得します。<BR>
	 * 
	 * @return
	 * @since 2.2.0
	 */
	public int getStringUpperValue() {
		return m_stringUpperBound;
	}

	/**
	 * プロパティの文字数上限値を設定します。<BR>
	 * 
	 * @param value
	 * @since 2.2.0
	 */
	public void setStringUpperValue(int value) {
		m_stringUpperBound = value;
	}


}
