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

public class AddFileTransferJobRequest extends AbstractAddJobRequest implements RequestDto {

	/** ジョブファイル転送情報 */
	@RestValidateObject(notNull = true)
	private JobFileInfoRequest file;

	public AddFileTransferJobRequest() {
	}

	public JobFileInfoRequest getFile() {
		return file;
	}

	public void setFile(JobFileInfoRequest file) {
		this.file = file;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		super.correlationCheck();
		file.correlationCheck();
	}

}
