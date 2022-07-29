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
public abstract class InfraModuleInfoResponse {

	private String managementId;
	private String moduleId;
	private String name;
	private Integer orderNo;
	private Boolean validFlg;
	private Boolean stopIfFailFlg;
	private Boolean precheckFlg;
	private String execReturnParamName;

	public InfraModuleInfoResponse() {
	}

	public String getManagementId() {
		return managementId;
	}

	public void setManagementId(String managementId) {
		this.managementId = managementId;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	public Boolean getValidFlg() {
		return validFlg;
	}

	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}

	public Boolean getStopIfFailFlg() {
		return stopIfFailFlg;
	}

	public void setStopIfFailFlg(Boolean stopIfFailFlg) {
		this.stopIfFailFlg = stopIfFailFlg;
	}

	public Boolean getPrecheckFlg() {
		return precheckFlg;
	}

	public void setPrecheckFlg(Boolean precheckFlg) {
		this.precheckFlg = precheckFlg;
	}

	public String getExecReturnParamName() {
		return execReturnParamName;
	}

	public void setExecReturnParamName(String execReturnParamName) {
		this.execReturnParamName = execReturnParamName;
	}

	@Override
	public String toString() {
		return "InfraModuleInfoResponse [managementId=" + managementId + ", moduleId=" + moduleId + ", name=" + name
				+ ", orderNo=" + orderNo + ", validFlg=" + validFlg + ", stopIfFailFlg=" + stopIfFailFlg
				+ ", precheckFlg=" + precheckFlg + ", execReturnParamName=" + execReturnParamName + "]";
	}

}
