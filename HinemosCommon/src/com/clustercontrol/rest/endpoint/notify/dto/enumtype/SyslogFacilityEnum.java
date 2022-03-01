/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto.enumtype;

import com.clustercontrol.notify.bean.SyslogFacilityConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum SyslogFacilityEnum implements EnumDto<Integer> {

	KERN(SyslogFacilityConstant.TYPE_KERN),
	USER(SyslogFacilityConstant.TYPE_USER),
	MAIL(SyslogFacilityConstant.TYPE_MAIL),
	DAEMON(SyslogFacilityConstant.TYPE_DAEMON),
	AUTH(SyslogFacilityConstant.TYPE_AUTH),
	SYSLOG(SyslogFacilityConstant.TYPE_SYSLOG),
	LPR(SyslogFacilityConstant.TYPE_LPR),
	NEWS(SyslogFacilityConstant.TYPE_NEWS),
	UUCP(SyslogFacilityConstant.TYPE_UUCP),
	CRON(SyslogFacilityConstant.TYPE_CRON),
	AUTHPRIV(SyslogFacilityConstant.TYPE_AUTHPRIV),
	FTP(SyslogFacilityConstant.TYPE_FTP),
	LOCAL0(SyslogFacilityConstant.TYPE_LOCAL0),
	LOCAL1(SyslogFacilityConstant.TYPE_LOCAL1),
	LOCAL2(SyslogFacilityConstant.TYPE_LOCAL2),
	LOCAL3(SyslogFacilityConstant.TYPE_LOCAL3),
	LOCAL4(SyslogFacilityConstant.TYPE_LOCAL4),
	LOCAL5(SyslogFacilityConstant.TYPE_LOCAL5),
	LOCAL6(SyslogFacilityConstant.TYPE_LOCAL6),
	LOCAL7(SyslogFacilityConstant.TYPE_LOCAL7);

	private final Integer code;

	private SyslogFacilityEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
