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

public class AddRpaJobRequest extends AbstractAddJobRequest implements RequestDto {

	/** RPAシナリオジョブ情報 */
	@RestValidateObject(notNull = true)
	private JobRpaInfoRequest rpa;

	public AddRpaJobRequest() {
	}

	public JobRpaInfoRequest getRpa() {
		return rpa;
	}

	public void setRpa(JobRpaInfoRequest rpa) {
		this.rpa = rpa;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		super.correlationCheck();
		rpa.correlationCheck();
	}

}
