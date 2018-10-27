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
 * 待ち条件タブのテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * @version 2.1.0
 * @since 1.0.0
 */
public class GetWaitRuleTableDefine {

	/** 名前 */
	public static final int JUDGMENT_OBJECT = 0;
	/** ジョブID */
	public static final int JOB_ID = 1;
	/** 値 */
	public static final int START_VALUE = 2;
	/** 判定値1 */
	public static final int DECISION_VALUE_1 = 3;
	/** 判定条件 */
	public static final int DECISION_CONDITION = 4;
	/** 判定値2 */
	public static final int DECISION_VALUE_2 = 5;
	/** セッション横断ジョブ判定対象範囲 */
	public static final int CROSS_SESSION_RANGE = 6;
	/**  説明 */
	public static final int DESCRIPTION = 7;
	/**  セッション開始時の時間（分） */
	public static final int START_MINUTE = 8;
	
	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = JUDGMENT_OBJECT;
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

		tableDefine.add(JUDGMENT_OBJECT,
				new TableColumnInfo(Messages.getString("name"), TableColumnInfo.JUDGMENT_OBJECT, 200, SWT.LEFT));
		tableDefine.add(JOB_ID,
				new TableColumnInfo(Messages.getString("job.id"), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(START_VALUE,
				new TableColumnInfo(Messages.getString("value"), TableColumnInfo.WAIT_RULE_VALUE, 70, SWT.LEFT));
		tableDefine.add(DECISION_VALUE_1,
				new TableColumnInfo(Messages.getString("wait.rule.decision.value1"), TableColumnInfo.NONE, 110, SWT.LEFT));
		tableDefine.add(DECISION_CONDITION,
				new TableColumnInfo(Messages.getString("wait.rule.decision.condition"), TableColumnInfo.DECISION_CONDITION, 120, SWT.LEFT));
		tableDefine.add(DECISION_VALUE_2,
				new TableColumnInfo(Messages.getString("wait.rule.decision.value2"), TableColumnInfo.NONE, 110, SWT.LEFT));
		tableDefine.add(CROSS_SESSION_RANGE,
				new TableColumnInfo(Messages.getString("wait.rule.cross.session.range"), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description"), TableColumnInfo.NONE, 150, SWT.LEFT));

		return tableDefine;
	}
}
