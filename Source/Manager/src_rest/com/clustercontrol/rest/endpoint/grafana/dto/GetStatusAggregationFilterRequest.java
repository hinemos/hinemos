/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.grafana.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.repository.bean.FacilityIdConstant;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.grafana.dto.enumtype.StatusFacilityTargetEnum;
import com.clustercontrol.util.MessageConstant;

public class GetStatusAggregationFilterRequest implements RequestDto {

	public GetStatusAggregationFilterRequest() {
	}

	@RestItemName(value = MessageConstant.FACILITY_ID)
	@RestValidateString(type = CheckType.ID, maxLen = FacilityIdConstant.MAX_LEN)
	private String facilityId;
	
	@RestItemName(value = MessageConstant.FACILITY_TARGET)
	private StatusFacilityTargetEnum facilityTarget = StatusFacilityTargetEnum.ALL;
	
	@RestItemName(value = MessageConstant.EVENT_AGGREGATION_ENTIRE)
	private boolean entire;
	
	@RestItemName(value = MessageConstant.EVENT_AGGREGATION_CONDITIONS)
	private GetStatusAggregationConditionsRequest condition = new GetStatusAggregationConditionsRequest();

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public StatusFacilityTargetEnum getFacilityTarget() {
		return facilityTarget;
	}

	public void setFacilityTarget(StatusFacilityTargetEnum facilityTarget) {
		this.facilityTarget = facilityTarget;
	}

	public boolean isEntire() {
		return entire;
	}

	public void setEntire(boolean entire) {
		this.entire = entire;
	}

	public GetStatusAggregationConditionsRequest getCondition() {
		return condition;
	}

	public void setCondition(GetStatusAggregationConditionsRequest condition) {
		this.condition = condition;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		// rootを示すファシリティIDを渡してきた場合も許容する
		if (FacilityIdConstant.ROOT.equals(facilityId)) {
			facilityId = null;
		}
		// 詳細条件が指定されているならそのチェックをする
		if (condition != null) {
			condition.correlationCheck();
		}
	}

}
