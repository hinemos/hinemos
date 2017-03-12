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

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.composite.action.CopyPropertyAction;
import com.clustercontrol.composite.action.DeletePropertyAction;
import com.clustercontrol.editor.ComboPropertyDefine;
import com.clustercontrol.editor.PropertyDefine;
import com.clustercontrol.editor.TextAreaPropertyDefine;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * プロパティシートクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class PropertySheet extends TreeViewer {
	public static final String CLMN_KEY = "key";

	public static final String CLMN_VALUE = "value";

	private TreeColumn keyColumn;

	private TreeColumn valueColumn;

	/**
	 * @return Returns the copy.
	 */
	public boolean isCopy() {
		if (getSelection() instanceof TreeSelection) {
			TreeSelection selection = (TreeSelection)getSelection();
			if (selection.getFirstElement() instanceof Property) {
				Property property = (Property)selection.getFirstElement();
				return (property.getCopy() == PropertyDefineConstant.COPY_OK);
			}
		}
		return false;
	}

	/**
	 * コンストラクタ
	 *
	 * @param tree
	 *            テーブルツリー
	 * @since 1.0.0
	 */
	public PropertySheet(Tree tree) {
		super(tree);

		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		// テーブルカラムを作成
		keyColumn = new TreeColumn(tree, SWT.NONE);
		WidgetTestUtil.setTestId(this, "key", keyColumn);
		keyColumn.setText(Messages.getString("name"));
		valueColumn = new TreeColumn(tree, SWT.NONE);
		WidgetTestUtil.setTestId(this, "value", valueColumn);
		valueColumn.setText(Messages.getString("value"));
		setSize(200,200);

		setContentProvider(new PropertySheetContentProvider());
		setLabelProvider(new PropertySheetLabelProvider());
		setColumnProperties(new String[] { CLMN_KEY, CLMN_VALUE, });

		// 各カラムに設定するセル・エディタの配列
		CellEditor[] editors = new CellEditor[] { null, new TextCellEditor() };

		//セル・エディタの設定
		setCellEditors(editors);

		//セル・モディファイアを設定
		setCellModifier(new PropertySheetModifier(this));

		//ポップアップメニュー作成
		createContextMenu();
	}

	/**
	 * サイズ変更
	 */
	public void setSize(int keyColumnSize, int valueColumnSize) {
		keyColumn.setWidth(keyColumnSize);
		valueColumn.setWidth(valueColumnSize);
	}

	/**
	 * ポップアップメニュー作成
	 *
	 */
	private void createContextMenu() {
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			/*
			 * (non-Javadoc)
			 *
			 * @see org.eclipse.jface.action.IMenuListener#menuAboutToShow(org.eclipse.jface.action.IMenuManager)
			 */
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				//メニュー項目設定
				if (isCopy()) {
					fillContextMenu(manager);
				}
			}
		});

		Menu menu = menuManager.createContextMenu(this.getTree());
		WidgetTestUtil.setTestId(this, null, menu);
		this.getTree().setMenu(menu);
	}

	/**
	 * メニュー項目追加
	 *
	 * @param manager
	 */
	private void fillContextMenu(IMenuManager manager) {
		//ポップアップメニューにアクションを追加
		manager.add(new CopyPropertyAction(this));
		manager.add(new DeletePropertyAction(this));
	}

	public void setInput(Property property) {
		//子Propertyを取得
		Object[] childrens = property.getChildren();

		//子PropertyにPropertyDefineを定義する
		for (int i = 0; i < childrens.length; i++) {
			Property children = (Property) childrens[i];
			setPropertyDefine(children);
		}

		super.setInput(property);
	}

	public void setPropertyDefine(Property property) {
		//PropertyDefineを定義する
		if (property.getDefine() == null) {
			PropertyDefine define = null;
			try {
				define = (PropertyDefine) Class.forName(property.getEditor())
						.newInstance();
				if (property.getEditor().equals(PropertyDefineConstant.EDITOR_SELECT)) {
					((ComboPropertyDefine) define).setValues(property
							.getSelectValues());
				}
				else if (property.getEditor().equals(PropertyDefineConstant.EDITOR_TEXTAREA)) {
					((TextAreaPropertyDefine) define).setTitle(property.getName());
					if(property.getModify() == PropertyDefineConstant.MODIFY_OK){
						((TextAreaPropertyDefine) define).setModify(true);
					}
				}
			} catch (InstantiationException e) {
			} catch (IllegalAccessException e) {
			} catch (ClassNotFoundException e) {
			}
			property.setDefine(define);
		}

		if (property.getEditor().equals(PropertyDefineConstant.EDITOR_SELECT)) {
			Object[][] value = property.getSelectValues();

			for (int j = 0; j < value[PropertyDefineConstant.SELECT_VALUE].length; j++) {
				if (value[PropertyDefineConstant.SELECT_VALUE][j] instanceof HashMap) {
					@SuppressWarnings("unchecked")
					HashMap<String, Object> map = (HashMap<String, Object>) value[PropertyDefineConstant.SELECT_VALUE][j];

					ArrayList<?> propertyList = (ArrayList<?>) map
							.get(PropertyDefineConstant.MAP_PROPERTY);
					if (propertyList != null) {
						for (int k = 0; k < propertyList.size(); k++) {
							Property children = (Property) propertyList.get(k);
							setPropertyDefine(children);
						}
					}
				}
			}
		}

		//子Propertyを取得
		Object[] childrens = property.getChildren();

		//子PropertyにPropertyDefineを定義する
		for (int i = 0; i < childrens.length; i++) {
			Property children1 = (Property) childrens[i];
			setPropertyDefine(children1);
		}
	}
}
