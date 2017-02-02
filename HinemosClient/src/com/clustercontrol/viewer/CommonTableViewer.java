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

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.editor.TextAreaDialogCellEditor;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 共通テーブルビューワークラス<BR>
 *
 * @version 2.2.0
 * @since 1.0.0
 */
public class CommonTableViewer extends TableViewer {
	private ArrayList<TableColumnInfo> m_tableColumnList = null;

	/**
	 * コンストラクタ
	 *
	 * @param parent
	 */
	public CommonTableViewer(Composite parent) {
		super(parent);
		setLabelProvider(new CommonTableLabelProvider(this));
		setContentProvider(new CommonTableContentProvider());
		setCellModifier(new CommonTableViewerModifier(this));
	}

	/**
	 * コンストラクタ
	 *
	 * @param parent
	 * @param style
	 */
	public CommonTableViewer(Composite parent, int style) {
		super(parent, style);
		setLabelProvider(new CommonTableLabelProvider(this));
		setContentProvider(new CommonTableContentProvider());
		setCellModifier(new CommonTableViewerModifier(this));
	}

	/**
	 * コンストラクタ
	 *
	 * @param table
	 */
	public CommonTableViewer(Table table) {
		super(table);
		setLabelProvider(new CommonTableLabelProvider(this));
		setContentProvider(new CommonTableContentProvider());
		setCellModifier(new CommonTableViewerModifier(this));
	}

	/**
	 * テーブルカラムの作成処理
	 *
	 * @param tableColumnList
	 */
	public void createTableColumn(ArrayList<TableColumnInfo> tableColumnList,
			int sortColumnIndex, int sortOrder) {

		//セカンド＿ソーターを使わないときはソートカラム（２つ目）に
		//-1をセットしておく。
		this.createTableColumn(tableColumnList,
				sortColumnIndex,
				-1,
				sortOrder);


	}

	/**
	 * テーブルカラムの作成処理
	 *
	 * @param tableColumnList
	 */
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
			WidgetTestUtil.setTestId(this, null, column);
			//column.setData(ClusterControlPlugin.CUSTOM_WIDGET_ID, "commonTableViewerColumn" + String.valueOf(i));
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

			//初期表示時のソート
			/*  if (i == sortColumnIndex) {
                int order = sortOrder;
                tableColumnInfo.setOrder(order);
                setSorter(new CommonTableViewerSorter(i, order));
            }*/

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

			//ソート用にカラム選択時のリスナーを作成
			column.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					TableColumn selectedColumn = (TableColumn) e.getSource();
					ArrayList<TableColumnInfo> tableColumnList = getTableColumnList();
					int order = 0;
					for (int i = 0; i < tableColumnList.size(); i++) {
						TableColumnInfo tableColumnInfo = tableColumnList.get(i);
						if (tableColumnInfo.getName().compareTo(selectedColumn.getText()) == 0) {
							order = tableColumnInfo.getOrder() * -1;
							tableColumnInfo.setOrder(order);
							//第1ソート順はi 第２ソート順は無し（-1）、ソート順はorder(昇順？降順？)で
							//ソートを実行
							setSorter(new CommonTableViewerSorter(i,-1,order));
							break;
						}
					}
					Table table = selectedColumn.getParent();
					table.setSortColumn(selectedColumn);
					if (order > 0){
						table.setSortDirection(SWT.UP);
					} else {
						table.setSortDirection(SWT.DOWN);
					}
				}
			});
		}

		//カラム・プロパティの設定
		setColumnProperties(properties);
		//セル・エディタの設定
		setCellEditors(editors);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.StructuredViewer#doUpdateItem(org.eclipse.swt.widgets.Widget,
	 *      java.lang.Object, boolean)
	 */
	@Override
	protected void doUpdateItem(Widget widget, Object element, boolean fullMap) {
		if (widget instanceof TableItem) {
			final TableItem item = (TableItem) widget;
			WidgetTestUtil.setTestId(this, null, item);

			// remember element we are showing
			if (fullMap) {
				associate(element, item);
			} else {
				item.setData(element);
				mapElement(element, item);
			}

			IBaseLabelProvider prov = getLabelProvider();
			ICommonTableLabelProvider tprov = null;

			if (prov instanceof ICommonTableLabelProvider) {
				tprov = (ICommonTableLabelProvider) prov;

				int columnCount = super.getTable().getColumnCount();
				TableItem ti = item;
				WidgetTestUtil.setTestId(this, null, ti);
				// Also enter loop if no columns added. See 1G9WWGZ: JFUIF:WINNT
				// - TableViewer with 0 columns does not work
				for (int column = 0; column < columnCount || column == 0; column++) {
					// Similar code in TableTreeViewer.doUpdateItem()
					String text = "";//$NON-NLS-1$
					Image image = null;
					Color color = null;
					text = tprov.getColumnText(element, column);
					image = tprov.getColumnImage(element, column);
					color = tprov.getColumnColor(element, column);
					
					ti.setText(column, text);
					if (ti.getImage(column) != image) {
						ti.setImage(column, image);
					}
					if (color != null) {
						ti.setBackground(column, color);
					}
				}
				if (prov instanceof IColorProvider) {
					IColorProvider cprov = (IColorProvider) prov;
					ti.setForeground(cprov.getForeground(element));
					ti.setBackground(cprov.getBackground(element));
				}
				if (prov instanceof IFontProvider) {
					IFontProvider fprov = (IFontProvider) prov;
					ti.setFont(fprov.getFont(element));
				}
			} else {
				//ICommonTableLabelProviderを実装していない場合、スーパークラスを呼び出す
				super.doUpdateItem(widget, element, fullMap);
			}
		}
	}

	/**
	 * テーブルカラム情報取得処理
	 *
	 * @return テーブルカラム情報
	 */
	public ArrayList<TableColumnInfo> getTableColumnList() {
		return this.m_tableColumnList;
	}

	/**
	 * テーブルカラムインデックス取得処理
	 *
	 * @param type
	 * @return
	 */
	public int getTableColumnIndex(int type) {
		int index = -1;

		for (int i = 0; i < m_tableColumnList.size(); i++) {
			TableColumnInfo tableColumn = (TableColumnInfo) m_tableColumnList.get(i);

			if (type == tableColumn.getType()) {
				index = i;
				break;
			}
		}

		return index;
	}

	/**
	 * ツリーを選択した時にソーターを変更しようとしたけど
	 * 　仕様上ペンディング（ここをいじること。）
	 *
	 */
	public void setInput(ArrayList<?> infoList){
		super.setInput(infoList);
	}
}
