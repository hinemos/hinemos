/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.action;

import java.util.ArrayList;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * RPAシナリオジョブ（直接実行）の実行パラメータテーブル定義を取得するクライアント側アクションクラス
 */
public class GetRpaDirectParameterTableDefine {

	/** 順序 */
	public static final int ORDER_NO = 0;
	/** パラメータ */
	public static final int PARAMETER = 1;
	/** 説明 */
	public static final int DESCRIPTION = 2;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = ORDER_NO;
	/** 初期表示時ソートオーダー (昇順=1, 降順=-1) */
	public static final int SORT_ORDER = 1;

	public static ArrayList<TableColumnInfo> get() {
		// テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();

		tableDefine.add(ORDER_NO, new TableColumnInfo(Messages.getString("order"), TableColumnInfo.NONE, 50, SWT.LEFT));
		tableDefine.add(PARAMETER,
				new TableColumnInfo(Messages.getString("parameter"), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description"), TableColumnInfo.NONE, 150, SWT.LEFT));

		return tableDefine;
	}
}
