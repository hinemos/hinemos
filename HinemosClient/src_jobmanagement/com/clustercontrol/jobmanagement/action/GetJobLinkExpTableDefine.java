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
 * ジョブ連携拡張情報のテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 */
public class GetJobLinkExpTableDefine {

	/** キー */
	public static final int KEY = 0;
	/** 値 */
	public static final int VALUE = 1;
	
	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = KEY;
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

		tableDefine.add(KEY,
				new TableColumnInfo(Messages.getString("key"), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(VALUE,
				new TableColumnInfo(Messages.getString("value"), TableColumnInfo.NONE, 140, SWT.LEFT));
		return tableDefine;
	}
}
