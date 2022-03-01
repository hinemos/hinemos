/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.access.dto;

import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.endpoint.access.dto.enumtype.SystemFunctionEnum;
import com.clustercontrol.rest.endpoint.access.dto.enumtype.SystemPrivilegeModeEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.dto.RequestDto;

@RestBeanConvertIdClassSet(infoClass = SystemPrivilegeInfo.class, idName = "id")
public class SystemPrivilegeInfoRequestP1 implements RequestDto {

	public SystemPrivilegeInfoRequestP1() {
	}

	@RestBeanConvertEnum
	private SystemFunctionEnum systemFunction;
	@RestBeanConvertEnum
	private SystemPrivilegeModeEnum systemPrivilege;

	public SystemFunctionEnum getSystemFunction() {
		return systemFunction;
	}

	public void setSystemFunction(SystemFunctionEnum systemFunction) {
		this.systemFunction = systemFunction;
	}

	public SystemPrivilegeModeEnum getSystemPrivilege() {
		return systemPrivilege;
	}

	public void setSystemPrivilege(SystemPrivilegeModeEnum systemPrivilege) {
		this.systemPrivilege = systemPrivilege;
	}

	@Override
	public String toString() {
		return "SystemPrivilegeInfoResponseP1 [systemFunction=" + systemFunction + ", systemPrivilege="
				+ systemPrivilege + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
