/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto.enumtype;

import com.clustercontrol.rest.dto.EnumDto;
import com.clustercontrol.xcloud.bean.CloudConstant;

public enum NotifyCloudPlatformTypeEnum implements EnumDto<Integer> {

	AWS(CloudConstant.notify_aws_platform),
	AZURE(CloudConstant.notify_azure_platform),
	OTHER(-1);

	private final Integer code;

	private NotifyCloudPlatformTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
