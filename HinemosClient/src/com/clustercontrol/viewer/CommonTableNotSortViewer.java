/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.viewer;

import java.util.ArrayList;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.editor.TextAreaDialogCellEditor;

/**
 * 共通テーブルビューワークラス(ソート無)<BR>
 *
 */
public class CommonTableNotSortViewer extends CommonTableViewer {
	/**
	 * コンストラクタ
	 *
	 * @param parent
	 */
	public CommonTableNotSortViewer(Composite parent) {
		super(parent);
	}

	/**
	 * コンストラクタ
	 *
	 * @param parent
	 * @param style
	 */
	public CommonTableNotSortViewer(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * コンストラクタ
	 *
	 * @param table
	 */
	public CommonTableNotSortViewer(Table table) {
		super(table);
	}

	/**
	 * テーブルカラムの作成処理
	 *
	 * @param tableColumnList
	 */
	@Override
	public void createTableColumn(ArrayList<TableColumnInfo> tableColumnList,
			int sortColumnIndex, int sortColumnIndex2,int sortOrder) {

		this.m_tableColumnList = tableColumnList;

		//カラム・プロパティの設定
		String[] properties = new String[this.m_tableColumnList.size()];
		// 各カラムに設定するセル・エディタの配列
		CellEditor[] editors = new CellEditor[this.m_tableColumnList.size()];

		for (int i = 0; i < this.m_tableColumnList.size(); i++) {
			TableColumnInfo tableColumnInfo = (TableColumnInfo) this.m_tableColumnList.get(i);
			TableColumn column = new TableColumn(getTable(), tableColumnInfo.getStyle(), i);
			column.setData(i);
			column.setText(tableColumnInfo.getName());
			column.setWidth(tableColumnInfo.getWidth());

			//カラム・プロパティの設定
			properties[i] = String.valueOf(i);
			// 各カラムに設定するセル・エディタの配列
			if(tableColumnInfo.getType() == TableColumnInfo.TEXT_DIALOG){
				TextAreaDialogCellEditor dialog = new TextAreaDialogCellEditor(getTable());
				dialog.setTitle(tableColumnInfo.getName());
				dialog.setModify(false);
				editors[i] = dialog;
			}else{
				editors[i] = null;
			}

			if (i == 0) {
				int order = sortOrder;
				tableColumnInfo.setOrder(order);
				setSorter(new CommonTableViewerSorter(sortColumnIndex,sortColumnIndex2, order));
				Table table = column.getParent();
				table.setSortColumn(column);
				if (order > 0){
					table.setSortDirection(SWT.UP);
				} else {
					table.setSortDirection(SWT.DOWN);
				}
			}
		}

		//カラム・プロパティの設定
		setColumnProperties(properties);
		//セル・エディタの設定
		setCellEditors(editors);
	}
}
