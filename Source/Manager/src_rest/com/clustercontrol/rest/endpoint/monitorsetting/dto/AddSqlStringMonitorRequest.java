/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityChangeFailureTypeEnum;
import com.clustercontrol.util.MessageConstant;

public class AddSqlStringMonitorRequest extends AbstractAddStringMonitorRequest {

	@RestItemName(value = MessageConstant.PRIORITY_CHANGE_FAILURE_TYPE)
	@RestValidateObject(notNull = true)
	@RestBeanConvertEnum
	private PriorityChangeFailureTypeEnum priorityChangeFailureType;

	public AddSqlStringMonitorRequest() {

	}

	@RestValidateObject(notNull=true)
	private SqlCheckInfoRequest sqlCheckInfo;

	public SqlCheckInfoRequest getSqlCheckInfo() {
		return sqlCheckInfo;
	}

	public void setSqlCheckInfo(SqlCheckInfoRequest sqlCheckInfo) {
		this.sqlCheckInfo = sqlCheckInfo;
	}

	public PriorityChangeFailureTypeEnum getPriorityChangeFailureType() {
		return priorityChangeFailureType;
	}

	public void setPriorityChangeFailureType(PriorityChangeFailureTypeEnum priorityChangeFailureType) {
		this.priorityChangeFailureType = priorityChangeFailureType;
	}

	@Override
	public String toString() {
		return "AddSqlStringMonitorRequest [sqlCheckInfo=" + sqlCheckInfo + ", collectorFlg=" + collectorFlg
				+ ", logFormatId=" + logFormatId + ", stringValueInfo=" + stringValueInfo + ", monitorId=" + monitorId
				+ ", application=" + application + ", description=" + description + ", monitorFlg=" + monitorFlg
				+ ", runInterval=" + runInterval + ", calendarId=" + calendarId + ", facilityId=" + facilityId
				+ ", priorityChangeJudgmentType=" + priorityChangeJudgmentType + ", priorityChangeFailureType="
				+ priorityChangeFailureType + ", notifyRelationList=" + notifyRelationList + ", ownerRoleId="
				+ ownerRoleId + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		super.correlationCheck();
		sqlCheckInfo.correlationCheck();
	}
}
