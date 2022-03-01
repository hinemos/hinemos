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
import com.clustercontrol.filtersetting.bean.FilterSettingConstant;
import com.clustercontrol.filtersetting.bean.JobHistoryFilterBaseInfo;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.rest.annotation.validation.RestValidateCollection;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

@RestBeanConvertAssertion(to = JobHistoryFilterBaseInfo.class)
public class JobHistoryFilterBaseRequest implements RequestDto {

	@RestItemName(MessageConstant.FILTER_CONDITION)
	@RestValidateCollection(maxSize = FilterSettingConstant.CONDITION_COUNT_MAX)
	private List<JobHistoryFilterConditionRequest> conditions;

	@Override
	public void correlationCheck() throws InvalidSetting {
		if (conditions != null) {
			for (JobHistoryFilterConditionRequest cnd : conditions) {
				cnd.correlationCheck();
			}
		}
	}

	public List<JobHistoryFilterConditionRequest> getConditions() {
		return conditions;
	}

	public void setConditions(List<JobHistoryFilterConditionRequest> conditions) {
		this.conditions = conditions;
	}

}
