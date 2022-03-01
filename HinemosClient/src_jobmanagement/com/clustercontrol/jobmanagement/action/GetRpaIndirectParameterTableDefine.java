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
 * RPAシナリオジョブ（間接実行）の起動パラメータテーブル定義を取得するクライアント側アクションクラス
 */
public class GetRpaIndirectParameterTableDefine {

	/** パラメータ */
	public static final int PARAM_NAME = 0;
	/** 値 */
	public static final int PARAM_VALUE = 1;
	/** 説明 */
	public static final int DESCRIPTION = 2;

	public static ArrayList<TableColumnInfo> get() {
		// テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();

		tableDefine.add(PARAM_NAME, new TableColumnInfo(Messages.getString("setting"), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(PARAM_VALUE,
				new TableColumnInfo(Messages.getString("value"), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description"), TableColumnInfo.NONE, 250, SWT.LEFT));

		return tableDefine;
	}

}
