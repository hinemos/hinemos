/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
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
 * コマンド実行結果取得用ジョブ変数のテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class GetCommandParameterTableDefine {
	/** 名前 */
	public static final int PARAM_ID = 0;
	/** 値 */
	public static final int VALUE = 1;
	/** 標準出力から取得 */
	public static final int JOB_STANDARD_OUTPUT = 2;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = PARAM_ID;
	/** 初期表示時ソートオーダー (昇順=1, 降順=-1) */
	public static final int SORT_ORDER = 1;

	/**
	 * ジョブ変数タブのテーブル定義を作成する
	 *
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 */
	public static ArrayList<TableColumnInfo> get() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();

		tableDefine.add(PARAM_ID,
				new TableColumnInfo(Messages.getString("name"), TableColumnInfo.NONE, 110, SWT.LEFT));
		tableDefine.add(VALUE,
				new TableColumnInfo(Messages.getString("value"), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(JOB_STANDARD_OUTPUT,
				new TableColumnInfo(Messages.getString("job.standard.output"), TableColumnInfo.NONE, 190, SWT.LEFT));

		return tableDefine;
	}
}
