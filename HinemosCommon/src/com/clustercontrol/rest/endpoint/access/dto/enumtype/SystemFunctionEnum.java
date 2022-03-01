/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.access.dto.enumtype;

import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum SystemFunctionEnum implements EnumDto<String> {
	HinemosAgent(FunctionConstant.HINEMOS_AGENT), 
	HinemosHA(FunctionConstant.HINEMOS_HA), 
	HinemosCLI(FunctionConstant.HINEMOS_CLI), 
	Repository(FunctionConstant.REPOSITORY), 
	AccessControl(FunctionConstant.ACCESSCONTROL), 
	JobManagement(FunctionConstant.JOBMANAGEMENT), 
	Collect(FunctionConstant.COLLECT), 
	MonitorResult(FunctionConstant.MONITOR_RESULT), 
	MonitorSetting(FunctionConstant.MONITOR_SETTING), 
	Calendar(FunctionConstant.CALENDAR), 
	Notify(FunctionConstant.NOTIFY), 
	Infra(FunctionConstant.INFRA), 
	Maintenance(FunctionConstant.MAINTENANCE), 
	CloudManagement(FunctionConstant.CLOUDMANAGEMENT),
	Reporting(FunctionConstant.REPORTING),
	FilterSetting(FunctionConstant.FILTER_SETTING),
	Hub(FunctionConstant.HUB),
	SdmlSetting(FunctionConstant.SDML_SETTING),
	Rpa(FunctionConstant.RPA);

	private final String code;

	private SystemFunctionEnum(final String code) {
		this.code = code;
	}
	
	@Override
	public String getCode() {
		return code;
	}

}
