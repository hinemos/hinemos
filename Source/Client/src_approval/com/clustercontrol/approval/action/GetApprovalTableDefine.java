/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.approval.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * 承認一覧のテーブル定義情報を取得するクライアント側アクションクラス<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class GetApprovalTableDefine {

	/** マネージャ名 */
	public static final int MANAGER_NAME = 0;
	
	/** 承認状態 */
	public static final int APPROVAL_STATUS = 1;

	/** 承認結果 */
	public static final int APPROVAL_RESULT = 2;
	
	/** ジョブセッションID */
	public static final int SESSION_ID = 3;

	/** ジョブユニットID */
	public static final int JOBUNIT_ID = 4;

	/** ジョブID */
	public static final int JOB_ID = 5;

	/** ジョブ名 */
	public static final int JOB_NAME = 6;

	/** 実行ユーザ */
	public static final int APPROVAL_REQUEST_USER = 7;
	
	/** 承認ユーザ */
	public static final int APPROVAL_USER = 8;

	/** 承認依頼日時 */
	public static final int APPROVAL_REQUEST_TIME = 9;

	/** 承認完了日時 */
	public static final int APPROVAL_COMPLETION_TIME = 10;

	/** 依頼文 */
	public static final int APPROVAL_REQUEST_SENTENCE = 11;

	/** コメント */
	public static final int COMMENT = 12;

	/** ダミー */
	public static final int DUMMY = 13;

	/** 初期表示時ソートカラム。 */
	public static final int SORT_COLUMN_INDEX1 = APPROVAL_STATUS;
	public static final int SORT_COLUMN_INDEX2 = SESSION_ID;

	/** 初期表示時ソートオーダー。 */
	public static final int SORT_ORDER = -1;

	/**
	 * 承認一覧のテーブル定義情報を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 *
	 */
	public static ArrayList<TableColumnInfo> get() {
		/** テーブル情報定義配列 */
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		// Manager Name
		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(APPROVAL_STATUS,
				new TableColumnInfo(Messages.getString("approval.status", locale), TableColumnInfo.APPROVAL_STATUS, 100, SWT.LEFT));
		tableDefine.add(APPROVAL_RESULT,
				new TableColumnInfo(Messages.getString("approval.result", locale), TableColumnInfo.APPROVAL_RESULT, 70, SWT.LEFT));
		tableDefine.add(SESSION_ID,
				new TableColumnInfo(Messages.getString("session.id", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(JOBUNIT_ID,
				new TableColumnInfo(Messages.getString("jobunit.id", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(JOB_ID,
				new TableColumnInfo(Messages.getString("job.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(JOB_NAME,
				new TableColumnInfo(Messages.getString("job.name", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(APPROVAL_REQUEST_USER,
				new TableColumnInfo(Messages.getString("approval.request.user", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(APPROVAL_USER,
				new TableColumnInfo(Messages.getString("approval.user", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(APPROVAL_REQUEST_TIME,
				new TableColumnInfo(Messages.getString("approval.request.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(APPROVAL_COMPLETION_TIME,
				new TableColumnInfo(Messages.getString("approval.completion.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(APPROVAL_REQUEST_SENTENCE,
				new TableColumnInfo(Messages.getString("approval.request.sentence", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(COMMENT,
				new TableColumnInfo(Messages.getString("comment", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(DUMMY,
				new TableColumnInfo("", TableColumnInfo.DUMMY, 50, SWT.LEFT));
		return tableDefine;
	}
}
