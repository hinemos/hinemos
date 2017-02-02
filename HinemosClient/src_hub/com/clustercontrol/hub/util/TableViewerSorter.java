/*
Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.
 */
package com.clustercontrol.hub.util;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * TableViewerのSorterクラス<BR>
 * 
 * @version 6.0.0
 * @since 6.0.0
 */
public class TableViewerSorter extends ViewerSorter {
	public static final long serialVersionUID = 1L;
	/** 前回のテーブル　*/
	protected static ColumnViewer lastTableViewer;
	
	/** カレントのカラム */
	protected static ColumnLabelProvider currentColumn;

	/** 一回前のカラム 
	 * -1は評価しない
	 * ソート時にcurrentColumnが入る
	 * */
	protected static ColumnLabelProvider lastColumn;

	/** ソートオーダー */
	protected static int order = 1;

	/** コンストラクター*/
	public TableViewerSorter(ColumnViewer tableViewer, ColumnLabelProvider provider){
		super();
		
		synchronized(TableViewerSorter.class) {
			if(!tableViewer.equals(lastTableViewer)){
				currentColumn = null;
			}
			lastTableViewer = tableViewer;
			lastColumn = currentColumn;
			currentColumn = provider;
			if(currentColumn.equals(lastColumn)){
				order = order * -1;
			} else {
				order = 1;
			}
		}
	}
	
	/**
	 * 比較処理
	 * 
	 * @param viewer
	 * @param e1
	 * @param e2
	 * @return 比較結果。superクラスの結果をソートオーダーにより反転する
	 * @since 1.0.0
	 */
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		
		int result = super.compare(viewer, currentColumn.getText(e1), currentColumn.getText(e2));
		
		if(result == 0 && lastColumn != null && !currentColumn.equals(lastColumn)){
			result = super.compare(viewer, lastColumn.getText(e1), lastColumn.getText(e2));
		}
		
		if (order == 1) {
			return result;
		} else {
			return result * -1;
		}
	}
}
