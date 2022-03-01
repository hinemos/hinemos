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
 * ジョブ連携の引継ぎ設定テーブル定義を取得するクライアント側アクションクラス<BR>
 *
 */
public class GetJobLinkRcvInheritTableDefine {
	/** メッセージ情報 */
	public static final int KEY_INFO = 0;
	/** 拡張情報キー */
	public static final int EXP_KEY = 1;
	/** ジョブ変数 */
	public static final int PARAM_ID = 2;
	/** 名前 */
	public static final int NAME = 3;

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

		tableDefine.add(KEY_INFO, new TableColumnInfo("", TableColumnInfo.NONE, 0, SWT.LEFT));
		tableDefine.add(EXP_KEY, new TableColumnInfo("", TableColumnInfo.NONE, 0, SWT.LEFT));
		tableDefine.add(PARAM_ID,
				new TableColumnInfo(Messages.getString("job.parameter.name"), TableColumnInfo.NONE, 110, SWT.LEFT));
		tableDefine.add(NAME,
				new TableColumnInfo(Messages.getString("job.joblink.message.info"), TableColumnInfo.NONE, 150, SWT.LEFT));

		return tableDefine;
	}
}
