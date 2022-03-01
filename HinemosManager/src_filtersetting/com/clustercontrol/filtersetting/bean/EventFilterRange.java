/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.bean;

import com.clustercontrol.rest.dto.EnumDto;

/**
 * イベント履歴フィルタ適用範囲。
 */
public enum EventFilterRange implements EnumDto<Integer> {

	/** 0: キャッシュ内 */
	CACHED(0),

	/** 1: 全範囲 */
	ENTIRE(1);

	/** DBにおけるコード値 */
	private Integer code;

	private EventFilterRange(int code) {
		this.code = Integer.valueOf(code);
	}

	public static EventFilterRange fromCode(Integer code) {
		for (EventFilterRange it : EventFilterRange.values()) {
			if (it.code.equals(code)) return it;
		}
		throw new IllegalArgumentException("Unknown code=" + code);
	}

	@Override
	public Integer getCode() {
		return code;
	}

}
