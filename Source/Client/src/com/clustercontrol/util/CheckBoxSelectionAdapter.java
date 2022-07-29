/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

import com.clustercontrol.viewer.CommonTableViewer;

public class CheckBoxSelectionAdapter extends SelectionAdapter {
	
	private Widget parent;
	private CommonTableViewer tableViewer;
	private int checkBoxColIndex;
	
	public CheckBoxSelectionAdapter(Composite parent, CommonTableViewer tableViewer, int checkBoxColIndex) {
		this.parent = parent;
		this.tableViewer = tableViewer;
		this.checkBoxColIndex = checkBoxColIndex;
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		// 選択されたTableColumnを取得します。
		
		Table table = tableViewer.getTable();
		TableItem[] ti = table.getSelection();
		TableItem tableItem = (TableItem)e.item;
		
		if ((e.stateMask & SWT.SHIFT ) != 0) {
			for (int i = 0; i < table.getItemCount(); i++) {
				TableItem item = table.getItem(i);
				if (isIgnoreRow(item)) {
					continue;
				}
				for (TableItem select : ti) {
					//選択されている行は何もしない
					select.equals(item);
					continue;
				}
				//選択されていない行は選択を解除
				setCheckBoxValue(item, false);
			}
			
			//複数件が選択の場合は選択範囲をすべてON
			for (int i = 0; i < ti.length; i++){
				TableItem item = ti[i];
				if (isIgnoreRow(item)) {
					continue;
				}
				setCheckBoxValue(item, true);
			}
		} else {
			if (isIgnoreRow(tableItem)) {
				return;
			}
			
			WidgetTestUtil.setTestId(parent, "tableitem", tableItem);
			if (getCheckBoxValue(tableItem)) {
				//YESならNO
				setCheckBoxValue(tableItem, false);
			} else {
				//NOならYES
				setCheckBoxValue(tableItem, true);
			}
		}
		//チェックボックスが入るので、再描画。
		tableViewer.refresh();
	}
	
	protected boolean isIgnoreRow(TableItem item) {
		//for override
		return false;
	}
	
	protected boolean getCheckBoxValue(TableItem item){ 
		return (boolean) toRowValues(item).get(checkBoxColIndex);
	}
	
	protected void setCheckBoxValue(TableItem item, boolean check){ 
		toRowValues(item).set(checkBoxColIndex, check);
	}
	
	protected ArrayList<Object> toRowValues(TableItem ti) {
		@SuppressWarnings("unchecked")
		ArrayList<Object> al = (ArrayList<Object>)ti.getData();
		return al;
	}
}
