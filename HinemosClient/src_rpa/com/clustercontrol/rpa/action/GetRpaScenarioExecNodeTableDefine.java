/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * 実行ノードタブのテーブル定義を取得するクライアント側アクションクラス<BR>
 */
public class GetRpaScenarioExecNodeTableDefine {

	/** 名前 */
	public static final int FACILITY_ID = 0;
	/** ジョブID */
	public static final int NODE_NAME = 1;
	
	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = FACILITY_ID;
	/** 初期表示時ソートオーダー (昇順=1, 降順=-1) */
	public static final int SORT_ORDER = 1;

	/**
	 * 待ち条件タブのテーブル定義を作成する
	 * 
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 */
	public static ArrayList<TableColumnInfo> get() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(FACILITY_ID,
				new TableColumnInfo(Messages.getString("rpa.scenario.exec.node.facility.id", locale), TableColumnInfo.NONE, 200, SWT.LEFT));
		tableDefine.add(NODE_NAME,
				new TableColumnInfo(Messages.getString("rpa.scenario.exec.node.name", locale), TableColumnInfo.NONE, 200, SWT.LEFT));

		return tableDefine;
	}
}
