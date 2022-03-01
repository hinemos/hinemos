/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto.enumtype;

import com.clustercontrol.notify.bean.SyslogSeverityConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum SyslogSeverityEnum implements EnumDto<Integer> {

	EMERGENCY(SyslogSeverityConstant.TYPE_EMERG),
	ALERT(SyslogSeverityConstant.TYPE_ALERT),
	CRITICAL(SyslogSeverityConstant.TYPE_CRIT),
	ERROR(SyslogSeverityConstant.TYPE_ERR),
	WARNING(SyslogSeverityConstant.TYPE_WARNING),
	NOTICE(SyslogSeverityConstant.TYPE_NOTICE),
	INFOMATION(SyslogSeverityConstant.TYPE_INFO),
	DEBUG(SyslogSeverityConstant.TYPE_DEBUG);

	private final Integer code;

	private SyslogSeverityEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
