/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto.enumtype;

import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum FacilityTypeEnum implements EnumDto<Integer> {

	/** スコープ（ファシリティの種別） */
	TYPE_SCOPE(FacilityConstant.TYPE_SCOPE),
	/** ノード（ファシリティの種別） */
	TYPE_NODE(FacilityConstant.TYPE_NODE),
	/** コンポジット（ファシリティの種別） */
	TYPE_COMPOSITE(FacilityConstant.TYPE_COMPOSITE),
	/** マネージャ（ファシリティの種別） */
	TYPE_MANAGER(FacilityConstant.TYPE_MANAGER);

	private final Integer code;

	private FacilityTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
