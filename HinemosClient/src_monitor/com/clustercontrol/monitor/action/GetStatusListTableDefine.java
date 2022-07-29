/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
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
import com.clustercontrol.util.Messages;

/**
 * 監視[ステータス]ビューのテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * マネージャにSessionBean経由でアクセスし、テーブル定義を取得します。
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class GetStatusListTableDefine {

	/** マネージャ名 */
	public static final int MANAGER_NAME = 0;

	/** 重要度。 */
	public static final int PRIORITY = 1;

	/** プラグインID。 */
	public static final int PLUGIN_ID = 2;

	/** 監視項目ID。 */
	public static final int MONITOR_ID = 3;

	/** 監視詳細。 */
	public static final int MONITOR_DETAIL_ID = 4;

	/** ファシリティID。 */
	public static final int FACILITY_ID = 5;

	/** スコープ。 */
	public static final int SCOPE = 6;

	/** アプリケーション。 */
	public static final int APPLICATION = 7;

	/** 更新日時。 */
	public static final int UPDATE_TIME = 8;

	/** 出力日時。 */
	public static final int OUTPUT_TIME = 9;

	/** メッセージ。 */
	public static final int MESSAGE = 10;

	/** オーナーロール */
	public static final int OWNER_ROLE = 11;

	/** ダミー**/
	public static final int DUMMY=12;

	/** 初期表示時ソートカラム。 */
	public static final int SORT_COLUMN_INDEX1 = MANAGER_NAME;
	public static final int SORT_COLUMN_INDEX2 = FACILITY_ID;
	
	/** 初期表示時ソートオーダー。 */
	public static final int SORT_ORDER = 1;

	/** カラム名からINDEXを取得するためのMap */
	public static final Map<String, Integer> COLNAME_INDEX_MAP;
	static {
		Map<String, Integer> map = new LinkedHashMap<>();
		
		map.put("MANAGER_NAME", MANAGER_NAME);
		map.put("PRIORITY", PRIORITY);
		map.put("PLUGIN_ID", PLUGIN_ID);
		map.put("MONITOR_ID", MONITOR_ID);
		map.put("MONITOR_DETAIL_ID", MONITOR_DETAIL_ID);
		map.put("FACILITY_ID", FACILITY_ID);
		map.put("SCOPE", SCOPE);
		map.put("APPLICATION", APPLICATION);
		map.put("UPDATE_TIME", UPDATE_TIME);
		map.put("OUTPUT_TIME", OUTPUT_TIME);
		map.put("MESSAGE", MESSAGE);
		map.put("OWNER_ROLE", OWNER_ROLE);
		map.put("DUMMY", DUMMY);
		COLNAME_INDEX_MAP = Collections.unmodifiableMap(map);
	}
	
	/**
	 * 監視[ステータス]ビューのテーブル定義情報を取得します。<BR><BR>
	 * リストに、カラム毎にテーブルカラム情報をセットします。
	 *
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 *
	 * @see com.clustercontrol.bean.TableColumnInfo#TableColumnInfo(java.lang.String, int, int, int)
	 * @see com.clustercontrol.monitor.bean.StatusTableDefine
	 */
	public static ArrayList<TableColumnInfo> getStatusListTableDefine() {

		Locale locale = Locale.getDefault();

		/** テーブル情報定義配列 */
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();

		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(PRIORITY,
				new TableColumnInfo(Messages.getString("priority", locale), TableColumnInfo.PRIORITY, 55, SWT.LEFT));
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
		tableDefine.add(UPDATE_TIME,
				new TableColumnInfo(Messages.getString("update.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(OUTPUT_TIME,
				new TableColumnInfo(Messages.getString("output.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(MESSAGE,
				new TableColumnInfo(Messages.getString("message", locale), TableColumnInfo.NONE, 200, SWT.LEFT));
		tableDefine.add(OWNER_ROLE,
				new TableColumnInfo(Messages.getString("owner.role.id", locale), TableColumnInfo.NONE, 130, SWT.LEFT));
		tableDefine.add(DUMMY,
				new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));

		return tableDefine;
	}

}
