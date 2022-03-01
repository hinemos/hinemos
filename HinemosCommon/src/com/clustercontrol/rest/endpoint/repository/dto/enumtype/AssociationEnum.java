/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.repository.dto.enumtype;

import com.clustercontrol.nodemap.bean.AssociationConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum AssociationEnum implements EnumDto<Integer> {

	NORMAL(AssociationConstant.NORMAL), NEW(AssociationConstant.NEW), REMOVE(AssociationConstant.REMOVE);

	private final Integer code;

	private AssociationEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
