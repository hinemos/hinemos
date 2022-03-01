/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.bean;

/**
 * ステータス通知結果のフィルタ条件項目種別。
 */
public enum StatusFilterItemType implements FilterItemType {
	PRIORITY_CRITICAL(1), //重要度危険（true,false)
	PRIORITY_WARNING(2), //重要度警告（true,false)
	PRIORITY_INFO(3), //重要度情報（true,false)
	PRIORITY_UNKNOWN(4), //重要度不明（true,false)
	GENERATION_DATE_FROM(5), //最終変更日時開始
	GENERATION_DATE_TO(6), //最終変更日時終了
	OUTPUT_DATE_FROM(7), //出力日時開始
	OUTPUT_DATE_TO(8), //出力日時終了
	MONITOR_ID(9), //監視項目ID
	MONITOR_DETAIL(10), //監視詳細
	APPLICATION(11), //アプリケーション
	MESSAGE(12), //メッセージ
	OWNER_ROLE_ID(13); //オーナーロールID

	private final Integer code;

	private StatusFilterItemType(int code) {
		this.code = Integer.valueOf(code);
	}

	public static StatusFilterItemType fromCode(Integer code) {
		for (StatusFilterItemType it : StatusFilterItemType.values()) {
			if (it.code.equals(code)) return it;
		}
		throw new IllegalArgumentException("Unknown code=" + code);
	}

	@Override
	public Integer getCode() {
		return code;
	}
}
