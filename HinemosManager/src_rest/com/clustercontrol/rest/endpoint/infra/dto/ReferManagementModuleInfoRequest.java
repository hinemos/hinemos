/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.infra.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.infra.model.InfraModuleInfo;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.util.MessageConstant;

@RestBeanConvertIdClassSet(infoClass = InfraModuleInfo.class, idName = "id")
public class ReferManagementModuleInfoRequest extends InfraModuleInfoRequest {

	@RestItemName(MessageConstant.INFRA_MANAGEMENT_ID)
	@RestValidateString(maxLen = 64, minLen = 1, notNull = true)
	private String referManagementId;

	public String getReferManagementId() {
		return referManagementId;
	}

	public void setReferManagementId(String referManagementId) {
		this.referManagementId = referManagementId;
	}
	
	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}