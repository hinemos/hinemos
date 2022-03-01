/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateCollection;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class SetJobKickStatusRequest implements RequestDto {

	/** ジョブID */
	@RestItemName(value = MessageConstant.JOBKICK_ID)
	@RestValidateCollection(notNull = true, minSize = 1)
	private List<String> jobKickId;
	
	/** 有効/無効 */
	@RestItemName(value = MessageConstant.VALID_FLG)
	@RestValidateObject(notNull = true)
	private Boolean validFlag;

	public SetJobKickStatusRequest() {
	}
	
	public List<String> getJobKickId() {
		return jobKickId;
	}

	public void setJobKickId(List<String> jobKickId) {
		this.jobKickId = jobKickId;
	}

	public Boolean getValidFlag() {
		return validFlag;
	}

	public void setValidFlag(Boolean validFlag) {
		this.validFlag = validFlag;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
