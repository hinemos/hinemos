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

public class AddFileCheckJobRequest extends AbstractAddJobRequest implements RequestDto {

	/** ファイルチェックジョブ情報 */
	@RestValidateObject(notNull = true)
	private JobFileCheckInfoRequest jobFileCheck;

	public AddFileCheckJobRequest() {
	}

	public JobFileCheckInfoRequest getJobFileCheck() {
		return jobFileCheck;
	}

	public void setJobFileCheck(JobFileCheckInfoRequest jobFileCheck) {
		this.jobFileCheck = jobFileCheck;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		super.correlationCheck();
		jobFileCheck.correlationCheck();
	}

}
