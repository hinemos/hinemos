/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto.enumtype;

import com.clustercontrol.notify.bean.NotifyJobType;
import com.clustercontrol.rest.dto.EnumDto;

public enum NotifyJobTypeEnum implements EnumDto<Integer> {

	DIRECT(NotifyJobType.TYPE_DIRECT),
	JOB_LINK_SEND(NotifyJobType.TYPE_JOB_LINK_SEND);

	private final Integer code;

	private NotifyJobTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
