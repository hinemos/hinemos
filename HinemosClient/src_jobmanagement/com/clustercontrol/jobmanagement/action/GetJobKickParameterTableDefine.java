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
 * ジョブ実行契機ダイアログのジョブ変数テーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * @version 5.1.0
 */
public class GetJobKickParameterTableDefine {

	/** 名前 */
	public static final int NAME = 0;
	/** 種別 */
	public static final int TYPE = 1;
	/** デフォルト値 */
	public static final int DEFAULT_VALUE = 2;
	/** 説明 */
	public static final int DESCRIPTION = 3;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = NAME;
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

		tableDefine.add(NAME,
				new TableColumnInfo(Messages.getString("name"), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(TYPE,
				new TableColumnInfo(Messages.getString("type"), TableColumnInfo.JOB_RUNTIME_PARAM_TYPE, 150, SWT.LEFT));
		tableDefine.add(DEFAULT_VALUE,
				new TableColumnInfo(Messages.getString("default.value"), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description"), TableColumnInfo.NONE, 150, SWT.LEFT));

		return tableDefine;
	}
}
