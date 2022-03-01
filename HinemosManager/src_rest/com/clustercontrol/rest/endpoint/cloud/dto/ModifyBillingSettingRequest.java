/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class ModifyBillingSettingRequest implements RequestDto {
	private List<OptionRequest> options = new ArrayList<>();

	@RestItemName(value = MessageConstant.XCLOUD_CORE_BILLINGDETAIL_COLLECTOR_FLG)
	@RestValidateObject(notNull = true)
	private Boolean billingDetailCollectorFlg;

	@RestItemName(value = MessageConstant.XCLOUD_CORE_RETENTION_PERIOD)
	@RestValidateInteger(notNull = true, minVal = 0, maxVal = 180)
	private Integer retentionPeriod;

	public ModifyBillingSettingRequest() {
	}

	public List<OptionRequest> getOptions() {
		return options;
	}

	public void setOptions(List<OptionRequest> options) {
		this.options = options;
	}

	public Boolean getBillingDetailCollectorFlg() {
		return billingDetailCollectorFlg;
	}

	public void setBillingDetailCollectorFlg(Boolean billingDetailCollectorFlg) {
		this.billingDetailCollectorFlg = billingDetailCollectorFlg;
	}

	public Integer getRetentionPeriod() {
		return retentionPeriod;
	}

	public void setRetentionPeriod(Integer retentionPeriod) {
		this.retentionPeriod = retentionPeriod;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
