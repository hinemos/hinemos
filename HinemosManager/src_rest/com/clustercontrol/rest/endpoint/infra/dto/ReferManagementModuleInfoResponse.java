/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.infra.dto;

import com.clustercontrol.infra.model.InfraModuleInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;

@RestBeanConvertIdClassSet(infoClass = InfraModuleInfo.class, idName = "id")
public class ReferManagementModuleInfoResponse extends InfraModuleInfoResponse {

	private String referManagementId;

	public String getReferManagementId() {
		return referManagementId;
	}

	public void setReferManagementId(String referManagementId) {
		this.referManagementId = referManagementId;
	}
}