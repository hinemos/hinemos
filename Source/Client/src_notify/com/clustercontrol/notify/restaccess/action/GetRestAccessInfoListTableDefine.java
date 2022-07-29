/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.restaccess.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

public class GetRestAccessInfoListTableDefine {
	/** Manager Name */
	public static final int MANAGER_NAME = 0;

	/** RESTアクセスID。 */
	public static final int REST_ACCESS_ID = 1;

	/** 説明。 */
	public static final int DESCRIPTION = 2;

	/** オーナーロール */
	public static final int OWNER_ROLE = 3;

	/** 新規作成ユーザ。 */
	public static final int CREATE_USER = 4;

	/** 作成日時。 */
	public static final int CREATE_TIME = 5;

	/** 最終変更ユーザ。 */
	public static final int UPDATE_USER = 6;

	/** 最終変更日時。 */
	public static final int UPDATE_TIME = 7;

	/** ダミー**/
	public static final int DUMMY = 8;

	/** 初期表示時ソートカラム。 */
	public static final int SORT_COLUMN_INDEX1 = MANAGER_NAME;
	public static final int SORT_COLUMN_INDEX2 = REST_ACCESS_ID;

	/** 初期表示時ソートオーダー。 */
	public static final int SORT_ORDER = 1;


	/**
	 * RESTアクセス情報一覧のテーブル定義情報を返します。<BR>
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
				new TableColumnInfo(Messages.getString("facility.manager", "Manager", locale), TableColumnInfo.MANAGER_NAME, 100, SWT.LEFT));

		tableDefine.add(REST_ACCESS_ID,
				new TableColumnInfo(Messages.getString("restaccess.id", locale), TableColumnInfo.NONE, 150, SWT.LEFT));

		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description", locale), TableColumnInfo.NONE, 250, SWT.LEFT));

		tableDefine.add(OWNER_ROLE,
				new TableColumnInfo(Messages.getString("owner.role.id", locale), TableColumnInfo.NONE, 130, SWT.LEFT));

		tableDefine.add(CREATE_USER,
				new TableColumnInfo(Messages.getString("creator.name", locale), TableColumnInfo.NONE, 130, SWT.LEFT));

		tableDefine.add(CREATE_TIME,
				new TableColumnInfo(Messages.getString("create.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));

		tableDefine.add(UPDATE_USER,
				new TableColumnInfo(Messages.getString("modifier.name", locale), TableColumnInfo.NONE, 130, SWT.LEFT));

		tableDefine.add(UPDATE_TIME,
				new TableColumnInfo(Messages.getString("update.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));

		tableDefine.add(DUMMY,
				new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));

		return tableDefine;
	}

}
