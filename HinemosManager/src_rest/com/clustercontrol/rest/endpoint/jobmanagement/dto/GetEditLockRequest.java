/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;

public class GetEditLockRequest {

	@RestBeanConvertDatetime
	private String updateTime;

	private Boolean forceFlag;

	public GetEditLockRequest() {
	}

	public String getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
	public Boolean getForceFlag() {
		return forceFlag;
	}
	public void setForceFlag(Boolean forceFlag) {
		this.forceFlag = forceFlag;
	}
	
}
