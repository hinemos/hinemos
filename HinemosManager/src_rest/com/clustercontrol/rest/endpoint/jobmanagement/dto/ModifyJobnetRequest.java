/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class ModifyJobnetRequest extends AbstractModifyJobRequest implements RequestDto {

	public ModifyJobnetRequest() {
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		super.correlationCheck();
	}

}
