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

package com.clustercontrol.jobmanagement.viewer;

import java.util.ArrayList;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewerSorter;
import com.clustercontrol.viewer.ICommonTableLabelProvider;
//import com.clustercontrol.ClusterControlPlugin;

/**
 * 共通テーブルビューワークラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class JobTableTreeViewer extends TreeViewer {
	private ArrayList<TableColumnInfo> m_tableColumnList = null;

	/**
	 * コンストラクタ
	 *
	 * @param parent
	 * @since 1.0.0
	 */
	public JobTableTreeViewer(Composite parent) {
		super(parent);
		setLabelProvider(new JobTableTreeLabelProvider(this));
		setContentProvider(new JobTableTreeContentProvider());
	}

	/**
	 * コンストラクタ
	 *
	 * @param parent
	 * @param style
	 * @since 1.0.0
	 */
	public JobTableTreeViewer(Composite parent, int style) {
		super(parent, style);
		setLabelProvider(new JobTableTreeLabelProvider(this));
		setContentProvider(new JobTableTreeContentProvider());
	}

	/**
	 * コンストラクタ
	 *
	 * @param table
	 * @since 1.0.0
	 */
	public JobTableTreeViewer(Tree tree) {
		super(tree);
		setLabelProvider(new JobTableTreeLabelProvider(this));
		setContentProvider(new JobTableTreeContentProvider());
	}

	/**
	 * テーブルカラムの作成処理
	 *
	 * @param tableColumnList
	 * @since 1.0.0
	 */
	public void createTableColumn(ArrayList<TableColumnInfo> tableColumnList,
			int sortColumnIndex, int sortOrder) {
		this.m_tableColumnList = tableColumnList;

		for (int i = 0; i < this.m_tableColumnList.size(); i++) {
			TableColumnInfo tableColumnInfo = this.m_tableColumnList.get(i);
			TreeColumn column = new TreeColumn(getTree(), SWT.LEFT);
			WidgetTestUtil.setTestId(this, "column", column);
			//column.setData(ClusterControlPlugin.CUSTOM_WIDGET_ID, "jobTableTreeViewerColumn");
			column.setText(tableColumnInfo.getName());
			column.setWidth(tableColumnInfo.getWidth());

			//初期表示時のソート
			if (i == sortColumnIndex) {
				int order = sortOrder;
				tableColumnInfo.setOrder(order);
				setSorter(new CommonTableViewerSorter(i, order));
			}

			//ソート用にカラム選択時のリスナーを作成
			column.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					TreeColumn column = (TreeColumn) e.getSource();
					WidgetTestUtil.setTestId(this, null, column);

					ArrayList<TableColumnInfo> tableColumnList = getTableColumnList();
					for (int i = 0; i < tableColumnList.size(); i++) {
						TableColumnInfo tableColumnInfo = tableColumnList
								.get(i);
						if (tableColumnInfo.getName().compareTo(
								column.getText()) == 0) {
							int order = tableColumnInfo.getOrder() * -1;
							tableColumnInfo.setOrder(order);
							setSorter(new CommonTableViewerSorter(i, order));
							break;
						}
					}
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#doUpdateItem(org.eclipse.swt.widgets.Item,
	 *      java.lang.Object)
	 */
	@Override
	protected void doUpdateItem(Item item, Object element) {
		// update icon and label
		// Similar code in TableTreeViewer.doUpdateItem()
		IBaseLabelProvider prov = getLabelProvider();
		ICommonTableLabelProvider tprov = null;
		if (prov instanceof ICommonTableLabelProvider) {
			tprov = (ICommonTableLabelProvider) prov;

			int columnCount = getTree().getColumnCount();
			TreeItem ti = (TreeItem)item;
			WidgetTestUtil.setTestId(this, null, ti);
			////ti.setData(ClusterControlPlugin.CUSTOM_WIDGET_ID, "jobTableTreeViewerTi");
			// Also enter loop if no columns added. See 1G9WWGZ: JFUIF:WINNT -
			// TableViewer with 0 columns does not work
			for (int column = 0; column < columnCount || column == 0; column++) {
				String text = "";//$NON-NLS-1$
				Image image = null;
				Color color = null;
				text = tprov.getColumnText(element, column);
				image = tprov.getColumnImage(element, column);
				color = tprov.getColumnColor(element, column);
				ti.setText(column, text);
				// Apparently a problem to setImage to null if already null
				if (ti.getImage(column) != image)
					ti.setImage(column, image);
				if (color != null) {
					ti.setBackground(color);
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
			super.doUpdateItem(item, element);
		}
	}

	/**
	 * テーブルカラム情報取得処理
	 *
	 * @return テーブルカラム情報
	 * @since 1.0.0
	 */
	public ArrayList<TableColumnInfo> getTableColumnList() {
		return this.m_tableColumnList;
	}

	/**
	 * テーブルカラムインデックス取得処理
	 *
	 * @param type
	 * @return
	 * @since 1.0.0
	 */
	public int getTableColumnIndex(int type) {
		int index = -1;

		for (int i = 0; i < m_tableColumnList.size(); i++) {
			TableColumnInfo tableColumn = m_tableColumnList
					.get(i);

			if (type == tableColumn.getType()) {
				index = i;
				break;
			}
		}

		return index;
	}
}
