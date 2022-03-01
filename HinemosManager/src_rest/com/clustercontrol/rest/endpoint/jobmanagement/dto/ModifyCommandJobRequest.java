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

public class ModifyCommandJobRequest extends AbstractModifyJobRequest implements RequestDto {

	/** ジョブコマンド情報 */
	@RestValidateObject(notNull = true)
	private JobCommandInfoRequest command;


	public ModifyCommandJobRequest() {
	}
	
	

	public JobCommandInfoRequest getCommand() {
		return command;
	}



	public void setCommand(JobCommandInfoRequest command) {
		this.command = command;
	}



	@Override
	public void correlationCheck() throws InvalidSetting {
		super.correlationCheck();
		command.correlationCheck();
	}

}
