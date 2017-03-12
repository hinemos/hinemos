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

package com.clustercontrol.notify.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * 通知情報一覧のテーブル定義情報を取得するクライアント側アクションクラス<BR>
 *
 * @version 4.0.0
 * @since 1.0.0
 */
public class GetNotifyTableDefineNoCheckBox {

	/** Manager Name */
	public static final int MANAGER_NAME = 0;

	/** 通知ID。 */
	public static final int NOTIFY_ID = 1;

	/** 説明。 */
	public static final int DESCRIPTION = 2;

	/** 通知タイプ。
	 *
	 * @see com.clustercontrol.bean.NotifyTypeConstant
	 */
	public static final int NOTIFY_TYPE = 3;

	/** 有効／無効。 */
	public static final int VALID_FLG = 4;

	/** オーナーロール */
	public static final int OWNER_ROLE = 5;

	/** カレンダID */
	public static final int CALENDAR_ID = 6;

	/** 新規作成ユーザ。 */
	public static final int CREATE_USER = 7;

	/** 作成日時。 */
	public static final int CREATE_TIME = 8;

	/** 最終変更ユーザ。 */
	public static final int UPDATE_USER = 9;

	/** 最終変更日時。 */
	public static final int UPDATE_TIME = 10;

	/** ダミー**/
	public static final int DUMMY = 11;

	/** 初期表示時ソートカラム。 */
	public static final int SORT_COLUMN_INDEX1 = MANAGER_NAME;
	public static final int SORT_COLUMN_INDEX2 = NOTIFY_ID;

	/** 初期表示時ソートオーダー。 */
	public static final int SORT_ORDER = 1;

	/**
	 * 通知情報一覧のテーブル定義情報を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 *
	 * @see com.clustercontrol.notify.ejb.session.NotifyController
	 * @see com.clustercontrol.notify.ejb.session.NotifyControllerBean#getNotifyListTableDefine(java.util.Locale)
	 */
	public static ArrayList<TableColumnInfo> get() {
		/** テーブル情報定義配列 */
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		// Manager Name
		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", "Manager", locale), TableColumnInfo.MANAGER_NAME, 100, SWT.LEFT));
		tableDefine.add(NOTIFY_ID,
				new TableColumnInfo(Messages.getString("notify.id", locale), TableColumnInfo.NONE, 170, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description", locale), TableColumnInfo.NONE, 180, SWT.LEFT));
		tableDefine.add(NOTIFY_TYPE,
				new TableColumnInfo(Messages.getString("notify.type.list", locale), TableColumnInfo.NOTIFY_TYPE, 100, SWT.LEFT));
		tableDefine.add(VALID_FLG,
				new TableColumnInfo(Messages.getString("valid", locale) + "/" + Messages.getString("invalid", locale), TableColumnInfo.VALID, 80, SWT.LEFT));
		tableDefine.add(OWNER_ROLE,
				new TableColumnInfo(Messages.getString("owner.role.id", locale), TableColumnInfo.NONE, 130, SWT.LEFT));
		tableDefine.add(CALENDAR_ID,
				new TableColumnInfo(Messages.getString("calendar", locale), TableColumnInfo.NONE, 130, SWT.LEFT));
		tableDefine.add(CREATE_USER,
				new TableColumnInfo(Messages.getString("creator.name", locale), TableColumnInfo.NONE, 80, SWT.LEFT));
		tableDefine.add(CREATE_TIME,
				new TableColumnInfo(Messages.getString("create.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(UPDATE_USER,
				new TableColumnInfo(Messages.getString("modifier.name", locale), TableColumnInfo.NONE, 80, SWT.LEFT));
		tableDefine.add(UPDATE_TIME,
				new TableColumnInfo(Messages.getString("update.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(DUMMY,
				new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));

		return tableDefine;
	}
}
