/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.filtersetting.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.filtersetting.bean.EventFilterBaseInfo;
import com.clustercontrol.filtersetting.bean.FacilityTarget;
import com.clustercontrol.filtersetting.bean.FilterSettingConstant;
import com.clustercontrol.repository.bean.FacilityIdConstant;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.rest.annotation.validation.RestValidateCollection;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

@RestBeanConvertAssertion(to = EventFilterBaseInfo.class)
public class EventFilterBaseRequest implements RequestDto {

	@RestItemName(MessageConstant.SCOPE)
	@RestValidateString(type = CheckType.ID, maxLen = FacilityIdConstant.MAX_LEN)
	private String facilityId;

	@RestItemName(MessageConstant.FACILITY_TARGET)
	private FacilityTarget facilityTarget;

	@RestItemName(MessageConstant.FILTER_ENTIRE_RANGE)
	private Boolean entire;

	@RestItemName(MessageConstant.FILTER_CONDITION)
	@RestValidateCollection(maxSize = FilterSettingConstant.CONDITION_COUNT_MAX)
	private List<EventFilterConditionRequest> conditions;

	@Override
	public void correlationCheck() throws InvalidSetting {
		// rootを示すファシリティIDを渡してきた場合も許容する
		if (FacilityIdConstant.ROOT.equals(facilityId)) {
			facilityId = null;
		}
		// 詳細条件が指定されているならそのチェックをする
		if (conditions != null) {
			for (EventFilterConditionRequest cnd : conditions) {
				cnd.correlationCheck();
			}
		}
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public FacilityTarget getFacilityTarget() {
		return facilityTarget;
	}

	public void setFacilityTarget(FacilityTarget facilityTarget) {
		this.facilityTarget = facilityTarget;
	}

	public Boolean getEntire() {
		return entire;
	}

	public void setEntire(Boolean entire) {
		this.entire = entire;
	}

	public List<EventFilterConditionRequest> getConditions() {
		return conditions;
	}

	public void setConditions(List<EventFilterConditionRequest> conditions) {
		this.conditions = conditions;
	}

}
