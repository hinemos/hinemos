/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * ジョブ[一覧]ビューのテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * マネージャにSessionBean経由でアクセスし、テーブル定義を取得する
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class GetJobTableDefine {

	/** マネージャ名 */
	public static final int MANAGER_NAME = 0;
	/** ジョブID */
	public static final int JOB_ID = 1;
	/** ジョブ名 */
	public static final int JOB_NAME = 2;
	/** 種別 */
	public static final int JOB_TYPE = 3;
	/** ファシリティID */
	public static final int FACILITY_ID = 4;
	/** スコープ */
	public static final int SCOPE = 5;
	/** 待ち条件 */
	public static final int WAIT_RULE = 6;
	/** オーナーロール */
	public static final int OWNER_ROLE = 7;
	/** 新規作成ユーザ */
	public static final int CREATE_USER = 8;
	/** 作成日時 */
	public static final int CREATE_TIME = 9;
	/** 最終更新ユーザ */
	public static final int UPDATE_USER = 10;
	/** 最終更新日時 */
	public static final int UPDATE_TIME = 11;
	/** ダミー**/
	public static final int DUMMY=12;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX1 = MANAGER_NAME;
	public static final int SORT_COLUMN_INDEX2 = JOB_ID;
	/** 初期表示時ソートオーダー (昇順=1, 降順=-1) */
	public static final int SORT_ORDER = 1;


	/**
	 * マネージャにSessionBean経由でアクセスし、<BR>
	 * ジョブ[一覧]ビューのテーブル定義を取得する
	 *
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 */
	public static ArrayList<TableColumnInfo> get() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(JOB_ID,
				new TableColumnInfo(Messages.getString("job.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(JOB_NAME,
				new TableColumnInfo(Messages.getString("job.name", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(JOB_TYPE,
				new TableColumnInfo(Messages.getString("type", locale), 	TableColumnInfo.JOB, 110, SWT.LEFT));
		tableDefine.add(FACILITY_ID,
				new TableColumnInfo(Messages.getString("facility.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(SCOPE,
				new TableColumnInfo(Messages.getString("scope", locale), TableColumnInfo.FACILITY, 150, SWT.LEFT));
		tableDefine.add(WAIT_RULE,
				new TableColumnInfo(Messages.getString("wait.rule", locale), TableColumnInfo.WAIT_RULE, 80, SWT.LEFT));
		tableDefine.add(OWNER_ROLE,
				new TableColumnInfo(Messages.getString("owner.role.id", locale), TableColumnInfo.NONE, 130, SWT.LEFT));
		tableDefine.add(CREATE_USER,
				new TableColumnInfo(Messages.getString("creator.name", locale), TableColumnInfo.NONE, 80, SWT.LEFT));
		tableDefine.add(CREATE_TIME,
				new TableColumnInfo(Messages.getString("create.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(UPDATE_USER,
				new TableColumnInfo(Messages.getString("modifier.name", locale), TableColumnInfo.NONE, 80, SWT.LEFT));
		tableDefine.add(UPDATE_TIME,
				new TableColumnInfo(Messages.getString("update.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(DUMMY, new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));

		return tableDefine;
	}
}
