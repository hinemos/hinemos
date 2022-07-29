/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.bean;

import com.clustercontrol.rest.dto.EnumDto;

/**
 * ログフォーマットで抽出されるタグのデータ種別。
 *
 */
public enum ValueType implements EnumDto<Integer> {
	string,
	number,
	bool;

	@Override
	public Integer getCode() {
		return this.ordinal();
	}
}