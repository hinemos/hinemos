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
 * RPAシナリオジョブ（間接実行）の終了値判定条件テーブル定義を取得するクライアント側アクションクラス
 */
public class GetRpaIndirectEndValueTableDefine {

	/** 終了状態 */
	public static final int END_STATUS = 0;
	/** 終了値 */
	public static final int END_VALUE = 1;
	/** 説明 */
	public static final int DESCRIPTION = 2;

	public static ArrayList<TableColumnInfo> get() {
		// テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();

		tableDefine.add(END_STATUS,
				new TableColumnInfo(Messages.getString("end.status"), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(END_VALUE,
				new TableColumnInfo(Messages.getString("end.value"), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description"), TableColumnInfo.NONE, 150, SWT.LEFT));

		return tableDefine;
	}

}
