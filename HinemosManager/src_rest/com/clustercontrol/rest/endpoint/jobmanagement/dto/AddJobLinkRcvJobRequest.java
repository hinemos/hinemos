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

public class AddJobLinkRcvJobRequest extends AbstractAddJobRequest implements RequestDto {

	/** ジョブ連携待機ジョブ情報 */
	@RestValidateObject(notNull = true)
	private JobLinkRcvInfoRequest jobLinkRcv;

	public AddJobLinkRcvJobRequest() {
	}

	public JobLinkRcvInfoRequest getJobLinkRcv() {
		return jobLinkRcv;
	}

	public void setJobLinkRcv(JobLinkRcvInfoRequest jobLinkRcv) {
		this.jobLinkRcv = jobLinkRcv;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		super.correlationCheck();
		jobLinkRcv.correlationCheck();
	}
}
