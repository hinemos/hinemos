/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.traputil.dialog;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.viewer.CommonTableViewer;


/**
 * インポートダイアログ用のソートさせないテーブルの実装
 * 
 * @since 6.1.0
 * @version 2.4.0
 *
 */
public class ImportDialogTableViewer extends CommonTableViewer {

	/**
	* コンストラクタ
	* 
	* @param parent
	*/
	public ImportDialogTableViewer(Composite parent) {
		super(parent);
	}

	/**
	* コンストラクタ
	* 
	* @param parent
	* @param style
	*/
	public ImportDialogTableViewer(Composite parent, int style) {
		super(parent, style);
	}

	/**
	* コンストラクタ
	* 
	* @param table
	*/
	public ImportDialogTableViewer(Table table) {
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
		
		super.createTableColumn(tableColumnList, sortColumnIndex, sortColumnIndex2, sortOrder);

		for (TableColumn column : this.getTable().getColumns()) {
			//カラム選択時のリスナーを削除し、ソートできないようにする
			for (Listener listener : column.getListeners(SWT.Selection)) {
				column.removeListener(SWT.Selection, listener);
			}
			for (Listener listener : column.getListeners(SWT.DefaultSelection)) {
				column.removeListener(SWT.DefaultSelection, listener);
			}
		}
	}
}
