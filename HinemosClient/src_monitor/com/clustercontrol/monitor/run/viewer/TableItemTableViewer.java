/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.monitor.run.viewer;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 文字列監視の判定情報一覧のテーブルビューアークラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class TableItemTableViewer extends TableViewer {

	private static final String SORT_ORDER = "sortOrder";
	private CommonTableLabelProvider<?> provider = null;

	/**
	 * インスタンスを返します。
	 *
	 * @param table テーブル
	 * @since 5.0.0
	 *
	 * @see org.eclipse.jface.viewers.TableViewer#TableViewer(org.eclipse.swt.widgets.Composite)
	 * @see com.clustercontrol.http.viewer.PageTableLabelProvider
	 */
	public TableItemTableViewer(Table table, CommonTableLabelProvider<?> provider) {
		super(table);
		this.provider = provider;
		setLabelProvider(provider);
		setContentProvider(new ArrayContentProvider());
	}

	/**
	 * テーブルカラムを作成します。
	 *
	 * @param tableColumnList 判定情報一覧のテーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 * @since 5.0.0
	 *
	 * @see com.clustercontrol.bean.TableColumnInfo#TableColumnInfo(java.lang.String, int, int, int)
	 * @see com.clustercontrol.monitor.run.action.GetStringFilterTableDefine
	 */
	public void createTableColumn(ArrayList<?> tableColumnList) {

		for (int i = 0; i < tableColumnList.size(); i++) {
			TableColumnInfo tableColumnInfo = (TableColumnInfo) tableColumnList.get(i);
			TableColumn column = new TableColumn(getTable(), tableColumnInfo.getStyle(), i);
			WidgetTestUtil.setTestId(this, null, column);
			column.setText(tableColumnInfo.getName());
			column.setWidth(tableColumnInfo.getWidth());
			column.setData(column.getText(), i);
			column.setData(SORT_ORDER, -1);

			column.addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetSelected(SelectionEvent e) {
					TableColumn column = (TableColumn) e.getSource();
					WidgetTestUtil.setTestId(this, null, column);
					final int index = (int)column.getData(column.getText());
					final int order = ((int)column.getData(SORT_ORDER)) * -1;
					column.setData(SORT_ORDER, order);
					TableItemTableViewer.this.setSorter(new ViewerSorter(){
						@Override
						public int compare(Viewer viewer, Object e1, Object e2) {
							int result = super.compare(viewer, provider.getColumnText(e1, index), provider.getColumnText(e2, index));
							return result * order;
						}
					});
					
					Table table = column.getParent();
					table.setSortColumn(column);
					if (order > 0){
						table.setSortDirection(SWT.UP);
					} else {
						table.setSortDirection(SWT.DOWN);
					}

				}
			});
		}
	}
}
