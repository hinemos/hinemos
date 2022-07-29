/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto.enumtype;

import com.clustercontrol.repository.bean.LevelConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum Level implements EnumDto<Integer> {

	ALL(LevelConstant.ALL), ONE_LEVEL(LevelConstant.ONE_LEVEL);

	private final Integer code;

	private Level(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
