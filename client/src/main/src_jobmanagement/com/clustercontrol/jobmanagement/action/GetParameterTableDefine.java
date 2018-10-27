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
 * ジョブ変数タブのテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class GetParameterTableDefine {

	/** 名前 */
	public static final int PARAM_ID = 0;
	/** 種別 */
	public static final int TYPE = 1;
	/** 値 */
	public static final int VALUE = 2;
	/** 説明 */
	public static final int DESCRIPTION = 3;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = TYPE;
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
		tableDefine.add(TYPE,
				new TableColumnInfo(Messages.getString("type"), TableColumnInfo.JOB_PARAM_TYPE, 135, SWT.LEFT));
		tableDefine.add(VALUE,
				new TableColumnInfo(Messages.getString("value"), TableColumnInfo.NONE, 60, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description"), TableColumnInfo.NONE, 160, SWT.LEFT));

		return tableDefine;
	}
}
