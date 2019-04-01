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
 * 監視[スコープ]ビューのテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * マネージャにSessionBean経由でアクセスし、テーブル定義を取得します。
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class GetScopeListTableDefine {

	/** マネージャ名 */
	public static final int MANAGER_NAME = 0;

	/** 重要度。 */
	public static final int PRIORITY = 1;

	/** ファシリティID。 */
	public static final int FACILITY_ID = 2;

	/** スコープ。 */
	public static final int SCOPE = 3;

	/** ダミー**/
	public static final int DUMMY=4;

	/** 更新日時。 */
	public static final int UPDATE_TIME = 5;

	/** ソート用データ**/
	public static final int SORT_VALUE=6;

	/** 初期表示時ソートカラム。 */
	public static final int SORT_COLUMN_INDEX1 = SORT_VALUE;

	/** 初期表示時ソートカラム。 */
	public static final int SORT_COLUMN_INDEX2 = MANAGER_NAME;

	/** 初期表示時ソートカラム。 */
	public static final int SORT_COLUMN_INDEX3 = FACILITY_ID;

	/** 初期表示時ソートオーダー。 */
	public static final int SORT_ORDER = 1;

	/** カラム名からINDEXを取得するためのMap */
	public static final Map<String, Integer> COLNAME_INDEX_MAP;
	static {
		Map<String, Integer> map = new LinkedHashMap<>();
		map.put("MANAGER_NAME", MANAGER_NAME);
		map.put("PRIORITY", PRIORITY);
		map.put("FACILITY_ID", FACILITY_ID);
		map.put("SCOPE", SCOPE);
		map.put("DUMMY", DUMMY);
		COLNAME_INDEX_MAP = Collections.unmodifiableMap(map);
	}
	
	/**
	 * 監視[スコープ]ビューのテーブル定義情報を取得します。<BR><BR>
	 * リストに、カラム毎にテーブルカラム情報をセットします。
	 *
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 *
	 * @see com.clustercontrol.bean.TableColumnInfo#TableColumnInfo(java.lang.String, int, int, int)
	 * @see com.clustercontrol.monitor.action.GetScopeListTableDefine
	 */
	public static ArrayList<TableColumnInfo> getScopeListTableDefine() {
		/** 出力用変数 */
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		/** メイン処理 */
		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(PRIORITY,
				new TableColumnInfo(Messages.getString("priority", locale), TableColumnInfo.PRIORITY, 55, SWT.LEFT));
		tableDefine.add(FACILITY_ID,
				new TableColumnInfo(Messages.getString("facility.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(SCOPE,
				new TableColumnInfo(Messages.getString("scope", locale), TableColumnInfo.FACILITY, 150, SWT.LEFT));
		tableDefine.add(DUMMY,
				new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));

		return tableDefine;
	}
}
