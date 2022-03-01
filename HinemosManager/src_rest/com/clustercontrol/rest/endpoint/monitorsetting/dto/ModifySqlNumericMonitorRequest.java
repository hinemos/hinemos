/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;

public class ModifySqlNumericMonitorRequest extends AbstractModifyNumericMonitorRequest {

	public ModifySqlNumericMonitorRequest() {

	}

	@RestValidateObject(notNull=true)
	private SqlCheckInfoRequest sqlCheckInfo;

	
	public SqlCheckInfoRequest getSqlCheckInfo() {
		return sqlCheckInfo;
	}


	public void setSqlCheckInfo(SqlCheckInfoRequest sqlCheckInfo) {
		this.sqlCheckInfo = sqlCheckInfo;
	}

	@Override
	public String toString() {
		return "ModifySqlNumericMonitorRequest [sqlCheckInfo=" + sqlCheckInfo + ", collectorFlg=" + collectorFlg
				+ ", itemName=" + itemName + ", measure=" + measure + ", predictionFlg=" + predictionFlg
				+ ", predictionMethod=" + predictionMethod + ", predictionAnalysysRange=" + predictionAnalysysRange
				+ ", predictionTarget=" + predictionTarget + ", predictionApplication=" + predictionApplication
				+ ", changeFlg=" + changeFlg + ", changeAnalysysRange=" + changeAnalysysRange + ", changeApplication="
				+ changeApplication + ", numericValueInfo=" + numericValueInfo + ", predictionNotifyRelationList="
				+ predictionNotifyRelationList + ", changeNotifyRelationList=" + changeNotifyRelationList
				+ ", application=" + application + ", description=" + description + ", monitorFlg=" + monitorFlg
				+ ", runInterval=" + runInterval + ", calendarId=" + calendarId + ", facilityId=" + facilityId
				+ ", notifyRelationList=" + notifyRelationList + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		super.correlationCheck();
		sqlCheckInfo.correlationCheck();
	}
}
