/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.dto.RequestDto;

public class ModifyJobLinkSendJobRequest extends AbstractModifyJobRequest implements RequestDto {

	/** ジョブ連携送信ジョブ情報 */
	@RestValidateObject(notNull = true)
	private JobLinkSendInfoRequest jobLinkSend;

	public ModifyJobLinkSendJobRequest() {
	}

	public JobLinkSendInfoRequest getJobLinkSend() {
		return jobLinkSend;
	}

	public void setJobLinkSend(JobLinkSendInfoRequest jobLinkSend) {
		this.jobLinkSend = jobLinkSend;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		super.correlationCheck();
		jobLinkSend.correlationCheck();
	}
}
