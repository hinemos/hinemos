/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.bean;

/**
 * ジョブ実行履歴のフィルタ条件項目種別。
 */
public enum JobHistoryFilterItemType implements FilterItemType {
	START_DATE_FROM(1), //開始・再実行日時開始
	START_DATE_TO(2), //開始・再実行日時終了
	END_DATE_FROM(3), //終了・中断日時開始
	END_DATE_TO(4), //終了・中断日時終了
	JOB_ID(5), //ジョブID
	STATUS(6), //実行状態
	END_STATUS(7), //終了状態
	TRIGGER_TYPE(8), //実行契機種別
	TRIGGER_INFO(9), //実行契機情報
	OWNER_ROLE_ID(10), //オーナーロールID
	SESSION_ID(11); //セッションID

	private final Integer code;

	private JobHistoryFilterItemType(int code) {
		this.code = Integer.valueOf(code);
	}

	public static JobHistoryFilterItemType fromCode(Integer code) {
		for (JobHistoryFilterItemType it : JobHistoryFilterItemType.values()) {
			if (it.code.equals(code)) return it;
		}
		throw new IllegalArgumentException("Unknown value=" + code);
	}

	@Override
	public Integer getCode() {
		return code;
	}

}
