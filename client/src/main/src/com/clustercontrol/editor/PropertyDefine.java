/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.editor;

import java.io.Serializable;

import org.eclipse.jface.viewers.CellEditor;

import com.clustercontrol.bean.Property;

/**
 * プロパティの定義クラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
abstract public class PropertyDefine implements Serializable {
	private static final long serialVersionUID = -6812503650365680247L;

	/** この定義で使用するCellEditor */
	protected CellEditor m_cellEditor;

	/**
	 * テーブル表示文字列を取得します。<BR>
	 * 
	 * @param value
	 *            プロパティ値
	 * @return 表示文字列
	 * @since 1.0.0
	 */
	abstract public String getColumnText(Object value);

	/**
	 * エディターに渡す値をモデルを取得します。<BR>
	 * 
	 * @param element
	 *            プロパティ
	 * @return モデル
	 * @since 1.0.0
	 */
	abstract public Object getValue(Property element);

	/**
	 * エディターによって変更された値をモデルに格納します。<BR>
	 * 
	 * @param element
	 *            プロパティ
	 * @param value
	 *            モデル
	 * @since 1.0.0
	 */
	abstract public void modify(Property element, Object value);

	/**
	 * エディターの初期処理を行います。<BR>
	 * PropertySheetModifierでcellEditor.create(composite);の実行後に呼び出される。
	 * 
	 * @since 1.0.0
	 */
	abstract public void initEditer();

	/**
	 * CellEditorを取得します。<BR>
	 * 
	 * @return cellEditor
	 * @since 1.0.0
	 */
	public CellEditor getCellEditor() {
		return m_cellEditor;
	}

	/**
	 * CellEditorを設定します。
	 * 
	 * @param cellEditor
	 * @since 1.0.0
	 */
	public void setCellEditor(CellEditor cellEditor) {
		m_cellEditor = cellEditor;
	}

}
