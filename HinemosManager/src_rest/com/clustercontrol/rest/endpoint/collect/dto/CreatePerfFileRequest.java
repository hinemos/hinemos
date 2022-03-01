/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.collect.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.endpoint.collect.dto.emuntype.SummaryTypeEnum;
import com.clustercontrol.rest.dto.RequestDto;

public class CreatePerfFileRequest implements RequestDto {

	public CreatePerfFileRequest() {
	}

	private TreeMap<String, String> facilityNameMap = new TreeMap<>();

	private String localeStr;

	@RestBeanConvertEnum
	private SummaryTypeEnum summaryType;

	private List<String> facilityList = new ArrayList<String>();

	private boolean header;

	private String defaultDateStr;

	private List<CollectKeyInfoRequest> collectKeyInfoList;

	public TreeMap<String, String> getFacilityNameMap() {
		return facilityNameMap;
	}

	public void setFacilityNameMap(TreeMap<String, String> facilityNameMap) {
		this.facilityNameMap = facilityNameMap;
	}

	public List<String> getFacilityList() {
		return this.facilityList;
	}

	public void setFacilityList(List<String> facilityList) {
		this.facilityList = facilityList;
	}

	public SummaryTypeEnum getSummaryType() {
		return this.summaryType;
	}

	public void setSummaryType(SummaryTypeEnum summaryType) {
		this.summaryType = summaryType;
	}

	public String getLocaleStr() {
		return this.localeStr;
	}

	public void setLocalStr(String localeStr) {
		this.localeStr = localeStr;
	}

	public boolean getHeader() {
		return this.header;
	}

	public void setHeader(boolean header) {
		this.header = header;
	}

	public String getDefaultDateStr() {
		return this.defaultDateStr;
	}

	public void setDefaultDateStr(String defaultDateStr) {
		this.defaultDateStr = defaultDateStr;
	}

	public List<CollectKeyInfoRequest> getCollectKeyInfoList() {
		return this.collectKeyInfoList;
	}

	public void setCollectKeyInfoList(List<CollectKeyInfoRequest> collectKeyInfoList) {
		this.collectKeyInfoList = collectKeyInfoList;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
