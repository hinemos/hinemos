/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.filtersetting.dto;

import com.clustercontrol.fault.InvalidSetting;

public abstract class AbstractModifyFilterSettingRequest extends AbstractFilterSettingRequest {

	@Override
	public void correlationCheck() throws InvalidSetting {
		// NOP
	}
	
}
