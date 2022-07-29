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
 * RPAシナリオジョブのスクリーンショット一覧テーブル定義を取得するクライアント側アクションクラス
 */
public class GetRpaScreenshotTableDefine {

	/** 取得日時 */
	public static final int DATE = 0;
	/** 説明 */
	public static final int DESCRIPTION = 1;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = DATE;
	/** 初期表示時ソートオーダー (昇順=1, 降順=-1) */
	public static final int SORT_ORDER = -1;

	public static ArrayList<TableColumnInfo> get() {
		// テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();

		tableDefine.add(DATE,
				new TableColumnInfo(Messages.getString("obtain.time"), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description"), TableColumnInfo.NONE, 250, SWT.LEFT));

		return tableDefine;
	}

}
