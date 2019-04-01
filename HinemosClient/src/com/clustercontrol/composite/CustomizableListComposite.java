/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.bean.DefaultLayoutSettingManager.ColumnLayout;
import com.clustercontrol.bean.DefaultLayoutSettingManager.ListLayout;

/**
 * デフォルトカスタムレイアウトに対応した一覧
 * 
 */
public abstract class CustomizableListComposite extends Composite {

	private ListLayout listLayout;
	
	public CustomizableListComposite(Composite parent, int style, ListLayout listLayout) {
		super(parent, style);
		this.listLayout = listLayout;
	}

	public abstract Map<String, Integer> getColumnIndexMap();
	
	/**
	 * 列の並び順の入れ替え
	 * 
	 * @param table
	 */
	public void updateColumnOrder(Table table) {
		if (this.listLayout == null || getColumnIndexMap() == null) {
			return;
		}
		
		List<Integer> orderList = new ArrayList<>();
		
		for (ColumnLayout columnLayout : listLayout.getColumnList()) {
			Integer index = getColumnIndexMap().get(columnLayout.getId());
			if (index  != null) {
				orderList.add(index);
			}
		}
		for (Integer index : getColumnIndexMap().values()) {
			if (!orderList.contains(index)) {
				//存在していないINDEXがあった場合、追加
				orderList.add(index);
			}
		}
		
		int[] orders = new int[orderList.size()];
		for (int i=0; i < orders.length; i++) {
			orders[i] = orderList.get(i);
		}
		table.setColumnOrder(orders);
	}
	
	/**
	 * 列の並び順の入れ替え／幅の変更
	 * 
	 * @param table
	 */
	public void updateColumnWidth(Table table) {
		updateColumnWidth(table, null);
	}
	
	/**
	 * 列の並び順の入れ替え／幅の変更
	 * 
	 * @param table
	 */
	public void updateColumnWidth(Table table, ArrayList<TableColumnInfo> columnInfoList) {
		if (this.listLayout == null || getColumnIndexMap() == null) {
			return;
		}
		
		for (ColumnLayout columnLayout : listLayout.getColumnList()) {
			Integer index = getColumnIndexMap().get(columnLayout.getId());
			if (index == null) {
				continue;
			}
			TableColumn column = table.getColumn(index);
			if (column == null) {
				continue;
			}
			
			if (column.getWidth() <= 0) {
				//元々非表示（width=0）の列は変更しない
				continue;
			}
			
			if (columnLayout.getWidth() <= 0) {
				//レイアウトで非表示
				column.setWidth(0);
				column.setResizable(false);
				column.setMoveable(false);
			} else {
				
				int defaultWidth = -1;
				
				if (columnInfoList != null && columnInfoList.get(index) != null) {
					defaultWidth = columnInfoList.get(index).getWidth();
				}
				
				if (defaultWidth != -1 &&
						column.getWidth() != defaultWidth) {
					//デフォルトサイズでない場合、
					//本処理でセットした幅　または　ユーザが操作して変更した幅のため、
					//何もしない
					continue;
				}
				
				column.setWidth(columnLayout.getWidth());
				column.setResizable(true);
				column.setMoveable(true);
			}
		}
	}
}
