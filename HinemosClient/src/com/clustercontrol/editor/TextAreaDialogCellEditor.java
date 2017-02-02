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

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.dialog.TextAreaDialog;

/**
 * テキストエリアダイアログセルエディタークラス<BR>
 * 
 * @version 2.2.0
 * @since 2.0.0
 */
public class TextAreaDialogCellEditor extends DialogCellEditor {

	/** テキストエリアダイアログ タイトル */
	private String m_title = null;

	/** プロパティ値変更の可/不可 */
	private boolean m_modify = false;

	/**
	 * コンストラクタ
	 */
	public TextAreaDialogCellEditor() {
		super();
	}

	/**
	 * コンストラクタ
	 * 
	 * @param parent 親コンポジット
	 */
	public TextAreaDialogCellEditor(Composite parent) {
		super(parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.DialogCellEditor#openDialogBox(org.eclipse.swt.widgets.Control)
	 */
	@Override
	protected Object openDialogBox(Control cellEditorWindow) {

		//テキストエリアダイアログを表示する
		TextAreaDialog dialog;
		dialog = new TextAreaDialog(cellEditorWindow.getShell(), m_title, m_modify);

		if (this.getValue() instanceof String) {
			dialog.setText((String)this.getValue());
		}
		dialog.open();

		//入力した文字列を取得する
		String text = null;
		if (dialog.getReturnCode() == ScopeTreeDialog.OK) {
			text = dialog.getText();
		}
		return text;
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
