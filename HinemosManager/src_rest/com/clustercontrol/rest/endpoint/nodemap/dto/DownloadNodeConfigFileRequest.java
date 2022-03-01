/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.nodemap.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.dto.RequestDto;

public class DownloadNodeConfigFileRequest implements RequestDto{

	private List<String> facilityIdList;
	@RestBeanConvertDatetime
	private String targetDatetime;
	private String conditionStr;
	private String language;
	private String managerName;
	private List<String> itemList;
	private Boolean needHeaderInfo;

	public DownloadNodeConfigFileRequest() {
	}

	public List<String> getFacilityIdList() {
		return facilityIdList;
	}

	public void setFacilityIdList(List<String> facilityIdList) {
		this.facilityIdList = facilityIdList;
	}

	public String getTargetDatetime() {
		return targetDatetime;
	}

	public void setTargetDatetime(String targetDatetime) {
		this.targetDatetime = targetDatetime;
	}

	public String getConditionStr() {
		return conditionStr;
	}

	public void setConditionStr(String conditionStr) {
		this.conditionStr = conditionStr;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getManagerName() {
		return managerName;
	}

	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}

	public List<String> getItemList() {
		return itemList;
	}

	public void setItemList(List<String> itemList) {
		this.itemList = itemList;
	}

	public Boolean getNeedHeaderInfo() {
		return needHeaderInfo;
	}

	public void setNeedHeaderInfo(Boolean needHeaderInfo) {
		this.needHeaderInfo = needHeaderInfo;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
