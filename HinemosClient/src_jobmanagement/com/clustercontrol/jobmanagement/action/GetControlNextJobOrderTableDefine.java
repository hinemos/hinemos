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
 * 制御タブの後続ジョブ優先順位テーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * @version 6.1.0
 * @since 6.1.0
 */
public class GetControlNextJobOrderTableDefine {
	/** 優先順 */
	public static final int ORDER = 0;

	/** ジョブID */
	public static final int JOB_ID = 1;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = ORDER;

	/** 初期表示時ソートオーダー (昇順=1, 降順=-1) */
	public static final int SORT_ORDER = 1;

	/*
	 * 制御タブの後続ジョブ優先順位のテーブル定義を作成する
	 * 
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 */
	public static ArrayList<TableColumnInfo> get() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();

		tableDefine.add(ORDER,
				new TableColumnInfo(Messages.getString("order"), TableColumnInfo.NONE, 50, SWT.LEFT));
		tableDefine.add(JOB_ID,
				new TableColumnInfo(Messages.getString("job.next.job.id"), TableColumnInfo.NONE, 150, SWT.LEFT));

		return tableDefine;
	}
}
