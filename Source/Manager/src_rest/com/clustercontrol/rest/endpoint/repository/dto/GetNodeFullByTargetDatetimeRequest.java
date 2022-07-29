/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;
import com.clustercontrol.rest.dto.RequestDto;

public class GetNodeFullByTargetDatetimeRequest implements RequestDto {

	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String targetDatetime;
	/** 構成情報リスト(ノード検索用) */
	private List<NodeConfigFilterInfoRequest> nodeConfigFilterList = new ArrayList<>();

	public GetNodeFullByTargetDatetimeRequest() {
	}

	public String getTargetDatetime() {
		return targetDatetime;
	}

	public void setTargetDatetime(String targetDatetime) {
		this.targetDatetime = targetDatetime;
	}

	public List<NodeConfigFilterInfoRequest> getNodeConfigFilterList() {
		return nodeConfigFilterList;
	}

	public void setNodeConfigFilterList(List<NodeConfigFilterInfoRequest> nodeConfigFilterList) {
		this.nodeConfigFilterList = nodeConfigFilterList;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
