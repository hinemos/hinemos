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

package com.clustercontrol.jobmanagement.action;

import java.util.ArrayList;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * ランタイムジョブ変数ダイアログのテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * @version 5.1.0
 */
public class GetRuntimeParameterTableDefine {

	/** デフォルト値 */
	public static final int DEFAULT_VALUE = 0;
	/** 順序 */
	public static final int ORDER_NO = 1;
	/** 値 */
	public static final int VALUE = 2;
	/** 説明 */
	public static final int DESCRIPTION = 3;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = ORDER_NO;
	/** 初期表示時ソートオーダー (昇順=1, 降順=-1) */
	public static final int SORT_ORDER = 1;

	/**
	 * テーブル定義を作成する
	 *
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 */
	public static ArrayList<TableColumnInfo> get() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();

		tableDefine.add(DEFAULT_VALUE,
				new TableColumnInfo("", TableColumnInfo.NONE, 20, SWT.LEFT));
		tableDefine.add(ORDER_NO,
				new TableColumnInfo(Messages.getString("order"), TableColumnInfo.NONE, 50, SWT.LEFT));
		tableDefine.add(VALUE,
				new TableColumnInfo(Messages.getString("job.manual.param.detail.value"), TableColumnInfo.NONE, 60, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description"), TableColumnInfo.NONE, 120, SWT.LEFT));

		return tableDefine;
	}
}
