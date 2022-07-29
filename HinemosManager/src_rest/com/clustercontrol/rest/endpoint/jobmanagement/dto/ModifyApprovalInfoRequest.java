/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JobApprovalResultEnum;

public class ModifyApprovalInfoRequest implements RequestDto {

	/** 承認結果 */
	@RestBeanConvertEnum
	private JobApprovalResultEnum result = null;
	
	/** コメント */
	private String comment;

	public JobApprovalResultEnum getResult() {
		return result;
	}

	public void setResult(JobApprovalResultEnum result) {
		this.result = result;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
