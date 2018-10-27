/*
* Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
*
* Hinemos (http://www.hinemos.info/)
*
* See the LICENSE file for licensing information.
*/

package com.clustercontrol.inquiry.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * 遠隔管理テーブル定義情報を取得するクライアント側アクションクラス<BR>
 *
 * @version 6.1.0
 * @since 6.1.0
 */
public class GetInquiryTableDefine {

	/** マネージャ名 */
	public static final int MANAGER_NAME = 0;

	/** 対象 */
	public static final int ID = 1;

	/** 対象 */
	public static final int TARGET = 2;

	/** 最終作成開始日時 */
	public static final int CREATE_START_TIME = 3;

	/** 最終作成終了日時 */
	public static final int CREATE_FINISH_TIME = 4;

	/** ステータス */
	public static final int STATUS = 5;

	/** ダミー**/
	public static final int DUMMY = 6;

	/** 初期表示時ソートカラム。 */
	public static final int SORT_COLUMN_INDEX1 =  MANAGER_NAME;
	public static final int SORT_COLUMN_INDEX2 =  ID;

	/** 初期表示時ソートオーダー。 */
	public static final int SORT_ORDER = 1;


	/**
	 * 遠隔管理テーブル定義情報を返します。
	 *
	 * @return 遠隔管理一覧テーブル定義情報
	 */
	public static ArrayList<TableColumnInfo> get() {
		/** テーブル情報定義配列 */
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<>();
		Locale locale = Locale.getDefault();

		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", locale), TableColumnInfo.NONE, 100, SWT.LEFT));

		tableDefine.add(ID,
				new TableColumnInfo(Messages.getString("inquiry.target.id", locale), TableColumnInfo.NONE, 200, SWT.LEFT));

		tableDefine.add(TARGET,
				new TableColumnInfo(Messages.getString("inquiry.target.name", locale), TableColumnInfo.NONE, 200, SWT.LEFT));

		tableDefine.add(CREATE_START_TIME,
				new TableColumnInfo(Messages.getString("inquiry.target.latest.create.start.datetime", locale), TableColumnInfo.NONE, 150, SWT.LEFT));

		tableDefine.add(CREATE_FINISH_TIME,
				new TableColumnInfo(Messages.getString("inquiry.target.latest.create.finish.datetime", locale), TableColumnInfo.NONE, 150, SWT.LEFT));

		tableDefine.add(STATUS,
				new TableColumnInfo(Messages.getString("inquiry.target.status", locale), TableColumnInfo.NONE, 100, SWT.LEFT));

		tableDefine.add(DUMMY,
				new TableColumnInfo("", TableColumnInfo.DUMMY, 5, SWT.LEFT));

		return tableDefine;
	}
}
