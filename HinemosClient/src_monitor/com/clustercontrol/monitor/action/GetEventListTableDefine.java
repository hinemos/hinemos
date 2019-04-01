/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.monitor.bean.EventHinemosPropertyConstant;
import com.clustercontrol.util.Messages;

/**
 * 監視[イベント]ビューのテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * マネージャにSessionBean経由でアクセスし、テーブル定義を取得します。
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class GetEventListTableDefine {
	/** マネージャ名 */
	public static final int MANAGER_NAME = 0;

	/** 重要度。 */
	public static final int PRIORITY = 1;

	/** 受信日時。 */
	public static final int RECEIVE_TIME = 2;

	/** 出力日時。 */
	public static final int OUTPUT_DATE = 3;

	/** プラグインID。 */
	public static final int PLUGIN_ID = 4;

	/** 監視項目ID。 */
	public static final int MONITOR_ID = 5;

	/** 監視詳細。 */
	public static final int MONITOR_DETAIL_ID = 6;

	/** ファシリティID。 */
	public static final int FACILITY_ID = 7;

	/** スコープ。 */
	public static final int SCOPE = 8;

	/** アプリケーション。 */
	public static final int APPLICATION = 9;

	/** メッセージ。 */
	public static final int MESSAGE = 10;

	/** 確認。 */
	public static final int CONFIRMED = 11;

	/** 確認ユーザ */
	public static final int CONFIRM_USER = 12;

	/** コメント */
	public static final int COMMENT = 13;

	/** オーナーロール */
	public static final int OWNER_ROLE = 14;

	/** ユーザ項目01 */
	public static final int USER_ITEM01 = 15;

	/** ユーザ項目02 */
	public static final int USER_ITEM02 = 16;

	/** ユーザ項目03 */
	public static final int USER_ITEM03 = 17;

	/** ユーザ項目04 */
	public static final int USER_ITEM04 = 18;

	/** ユーザ項目05 */
	public static final int USER_ITEM05 = 19;

	/** ユーザ項目06 */
	public static final int USER_ITEM06 = 20;

	/** ユーザ項目07 */
	public static final int USER_ITEM07 = 21;

	/** ユーザ項目08 */
	public static final int USER_ITEM08 = 22;

	/** ユーザ項目09 */
	public static final int USER_ITEM09 = 23;

	/** ユーザ項目10 */
	public static final int USER_ITEM10 = 24;

	/** ユーザ項目11 */
	public static final int USER_ITEM11 = 25;

	/** ユーザ項目12 */
	public static final int USER_ITEM12 = 26;

	/** ユーザ項目13 */
	public static final int USER_ITEM13 = 27;

	/** ユーザ項目14 */
	public static final int USER_ITEM14 = 28;

	/** ユーザ項目15 */
	public static final int USER_ITEM15 = 29;

	/** ユーザ項目16 */
	public static final int USER_ITEM16 = 30;

	/** ユーザ項目17 */
	public static final int USER_ITEM17 = 31;

	/** ユーザ項目18 */
	public static final int USER_ITEM18 = 32;

	/** ユーザ項目19 */
	public static final int USER_ITEM19 = 33;

	/** ユーザ項目20 */
	public static final int USER_ITEM20 = 34;

	/** ユーザ項目21 */
	public static final int USER_ITEM21 = 35;

	/** ユーザ項目22 */
	public static final int USER_ITEM22 = 36;

	/** ユーザ項目23 */
	public static final int USER_ITEM23 = 37;

	/** ユーザ項目24 */
	public static final int USER_ITEM24 = 38;

	/** ユーザ項目25 */
	public static final int USER_ITEM25 = 39;

	/** ユーザ項目26 */
	public static final int USER_ITEM26 = 40;

	/** ユーザ項目27 */
	public static final int USER_ITEM27 = 41;

	/** ユーザ項目28 */
	public static final int USER_ITEM28 = 42;

	/** ユーザ項目29 */
	public static final int USER_ITEM29 = 43;

	/** ユーザ項目30 */
	public static final int USER_ITEM30 = 44;

	/** ユーザ項目31 */
	public static final int USER_ITEM31 = 45;

	/** ユーザ項目32 */
	public static final int USER_ITEM32 = 46;

	/** ユーザ項目33 */
	public static final int USER_ITEM33 = 47;

	/** ユーザ項目34 */
	public static final int USER_ITEM34 = 48;

	/** ユーザ項目35 */
	public static final int USER_ITEM35 = 49;

	/** ユーザ項目36 */
	public static final int USER_ITEM36 = 50;

	/** ユーザ項目37 */
	public static final int USER_ITEM37 = 51;

	/** ユーザ項目38 */
	public static final int USER_ITEM38 = 52;

	/** ユーザ項目39 */
	public static final int USER_ITEM39 = 53;

	/** ユーザ項目40 */
	public static final int USER_ITEM40 = 54;

	/** イベント番号 */
	public static final int EVENT_NO = 55;

	/** ダミー**/
	public static final int DUMMY = 56;


	/** 初期表示時ソートカラム。 */
	public static final int SORT_COLUMN_INDEX1 = RECEIVE_TIME;
	public static final int SORT_COLUMN_INDEX2 = MANAGER_NAME;

	/** 初期表示時ソートオーダー。 */
	public static final int SORT_ORDER = -1;

	private static final String USER_ITEM_FORMAT = "USER_ITEM%02d";
	
	/** ユーザ拡張イベント項目の列幅 */
	public static final int USERITEM_WIDTH = 100;
	/** イベント番号の列幅 */
	public static final int EVENT_NO_WIDTH = 100;
	
	/** カラム名からINDEXを取得するためのMap */
	public static final Map<String, Integer> COLNAME_INDEX_MAP;
	static {
		Map<String, Integer> map = new LinkedHashMap<>();
		
		map.put("MANAGER_NAME", MANAGER_NAME);
		map.put("PRIORITY", PRIORITY);
		map.put("RECEIVE_TIME", RECEIVE_TIME);
		map.put("OUTPUT_DATE", OUTPUT_DATE);
		map.put("PLUGIN_ID", PLUGIN_ID);
		map.put("MONITOR_ID", MONITOR_ID);
		map.put("MONITOR_DETAIL_ID", MONITOR_DETAIL_ID);
		map.put("FACILITY_ID", FACILITY_ID);
		map.put("SCOPE", SCOPE);
		map.put("APPLICATION", APPLICATION);
		map.put("MESSAGE", MESSAGE);
		map.put("CONFIRMED", CONFIRMED);
		map.put("CONFIRM_USER", CONFIRM_USER);
		map.put("COMMENT", COMMENT);
		map.put("OWNER_ROLE", OWNER_ROLE);
		map.put(String.format(USER_ITEM_FORMAT, 1), USER_ITEM01);
		map.put(String.format(USER_ITEM_FORMAT, 2), USER_ITEM02);
		map.put(String.format(USER_ITEM_FORMAT, 3), USER_ITEM03);
		map.put(String.format(USER_ITEM_FORMAT, 4), USER_ITEM04);
		map.put(String.format(USER_ITEM_FORMAT, 5), USER_ITEM05);
		map.put(String.format(USER_ITEM_FORMAT, 6), USER_ITEM06);
		map.put(String.format(USER_ITEM_FORMAT, 7), USER_ITEM07);
		map.put(String.format(USER_ITEM_FORMAT, 8), USER_ITEM08);
		map.put(String.format(USER_ITEM_FORMAT, 9), USER_ITEM09);
		map.put(String.format(USER_ITEM_FORMAT, 10), USER_ITEM10);
		map.put(String.format(USER_ITEM_FORMAT, 11), USER_ITEM11);
		map.put(String.format(USER_ITEM_FORMAT, 12), USER_ITEM12);
		map.put(String.format(USER_ITEM_FORMAT, 13), USER_ITEM13);
		map.put(String.format(USER_ITEM_FORMAT, 14), USER_ITEM14);
		map.put(String.format(USER_ITEM_FORMAT, 15), USER_ITEM15);
		map.put(String.format(USER_ITEM_FORMAT, 16), USER_ITEM16);
		map.put(String.format(USER_ITEM_FORMAT, 17), USER_ITEM17);
		map.put(String.format(USER_ITEM_FORMAT, 18), USER_ITEM18);
		map.put(String.format(USER_ITEM_FORMAT, 19), USER_ITEM19);
		map.put(String.format(USER_ITEM_FORMAT, 20), USER_ITEM20);
		map.put(String.format(USER_ITEM_FORMAT, 21), USER_ITEM21);
		map.put(String.format(USER_ITEM_FORMAT, 22), USER_ITEM22);
		map.put(String.format(USER_ITEM_FORMAT, 23), USER_ITEM23);
		map.put(String.format(USER_ITEM_FORMAT, 24), USER_ITEM24);
		map.put(String.format(USER_ITEM_FORMAT, 25), USER_ITEM25);
		map.put(String.format(USER_ITEM_FORMAT, 26), USER_ITEM26);
		map.put(String.format(USER_ITEM_FORMAT, 27), USER_ITEM27);
		map.put(String.format(USER_ITEM_FORMAT, 28), USER_ITEM28);
		map.put(String.format(USER_ITEM_FORMAT, 29), USER_ITEM29);
		map.put(String.format(USER_ITEM_FORMAT, 30), USER_ITEM30);
		map.put(String.format(USER_ITEM_FORMAT, 31), USER_ITEM31);
		map.put(String.format(USER_ITEM_FORMAT, 32), USER_ITEM32);
		map.put(String.format(USER_ITEM_FORMAT, 33), USER_ITEM33);
		map.put(String.format(USER_ITEM_FORMAT, 34), USER_ITEM34);
		map.put(String.format(USER_ITEM_FORMAT, 35), USER_ITEM35);
		map.put(String.format(USER_ITEM_FORMAT, 36), USER_ITEM36);
		map.put(String.format(USER_ITEM_FORMAT, 37), USER_ITEM37);
		map.put(String.format(USER_ITEM_FORMAT, 38), USER_ITEM38);
		map.put(String.format(USER_ITEM_FORMAT, 39), USER_ITEM39);
		map.put(String.format(USER_ITEM_FORMAT, 40), USER_ITEM40);
		map.put("EVENT_NO", EVENT_NO);
		map.put("DUMMY", DUMMY);
		COLNAME_INDEX_MAP = Collections.unmodifiableMap(map);
	}
	
	public static int getUserItemIndex(int index) {
		return COLNAME_INDEX_MAP.get(String.format(USER_ITEM_FORMAT, index));
	}
	
	/**
	 * 監視[イベント]ビューのテーブル定義情報を取得します。<BR><BR>
	 * リストに、カラム毎にテーブルカラム情報をセットします。
	 *
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 *
	 * @see com.clustercontrol.bean.TableColumnInfo#TableColumnInfo(java.lang.String, int, int, int)
	 * @see com.clustercontrol.monitor.bean.EventTableDefine
	 */
	public static ArrayList<TableColumnInfo> getEventListTableDefine() {

		Locale locale = Locale.getDefault();

		/** テーブル情報定義配列 */
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();

		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(PRIORITY,
				new TableColumnInfo(Messages.getString("priority", locale), TableColumnInfo.PRIORITY, 55, SWT.LEFT));
		tableDefine.add(RECEIVE_TIME,
				new TableColumnInfo(Messages.getString("receive.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(OUTPUT_DATE,
				new TableColumnInfo(Messages.getString("output.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(PLUGIN_ID,
				new TableColumnInfo(Messages.getString("plugin.id", locale), TableColumnInfo.NONE, 90, SWT.LEFT));
		tableDefine.add(MONITOR_ID,
				new TableColumnInfo(Messages.getString("monitor.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(MONITOR_DETAIL_ID,
				new TableColumnInfo(Messages.getString("monitor.detail.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(FACILITY_ID,
				new TableColumnInfo(Messages.getString("facility.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(SCOPE,
				new TableColumnInfo(Messages.getString("scope", locale), TableColumnInfo.FACILITY, 150, SWT.LEFT));
		tableDefine.add(APPLICATION,
				new TableColumnInfo(Messages.getString("application", locale), TableColumnInfo.NONE, 120, SWT.LEFT));
		tableDefine.add(MESSAGE,
				new TableColumnInfo(Messages.getString("message", locale), TableColumnInfo.NONE, 200, SWT.LEFT));
		tableDefine.add(CONFIRMED,
				new TableColumnInfo(Messages.getString("confirmed", locale), TableColumnInfo.CONFIRM, 80, SWT.LEFT));
		tableDefine.add(CONFIRM_USER,
				new TableColumnInfo(Messages.getString("confirm.user", locale), TableColumnInfo.NONE, 80, SWT.LEFT));
		tableDefine.add(COMMENT,
				new TableColumnInfo(Messages.getString("comment", locale), TableColumnInfo.NONE, 200, SWT.LEFT));
		tableDefine.add(OWNER_ROLE,
				new TableColumnInfo(Messages.getString("owner.role.id", locale), TableColumnInfo.NONE, 130, SWT.LEFT));
		//UserItemはHinemosプロパティから列名をセットするため、空文字
		for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			tableDefine.add(getUserItemIndex(i),
					new TableColumnInfo("", TableColumnInfo.NONE, USERITEM_WIDTH, SWT.LEFT));			
		}
		tableDefine.add(EVENT_NO,
				new TableColumnInfo(Messages.getString("monitor.eventno", locale), TableColumnInfo.NONE, EVENT_NO_WIDTH, SWT.LEFT));

		tableDefine.add(DUMMY,
				new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));

		return tableDefine;
	}

}
