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

public class GetNodeListRequest implements RequestDto {

	private String parentFacilityId;

	// 構成情報検索条件 AND/OR (true=and, false=or) (ノード検索用)
	private Boolean nodeConfigFilterIsAnd = true;

	// 構成情報リスト(ノード検索用)
	private List<NodeConfigFilterInfoRequest> nodeConfigFilterList = new ArrayList<>();

	// 対象日時
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String nodeConfigTargetDatetime;

	public GetNodeListRequest() {
	}

	public String getParentFacilityId() {
		return parentFacilityId;
	}

	public void setParentFacilityId(String parentFacilityId) {
		this.parentFacilityId = parentFacilityId;
	}

	public List<NodeConfigFilterInfoRequest> getNodeConfigFilterList() {
		return nodeConfigFilterList;
	}

	public void setNodeConfigFilterList(List<NodeConfigFilterInfoRequest> nodeConfigFilterList) {
		this.nodeConfigFilterList = nodeConfigFilterList;
	}

	public Boolean getNodeConfigFilterIsAnd() {
		return nodeConfigFilterIsAnd;
	}

	public void setNodeConfigFilterIsAnd(Boolean nodeConfigFilterIsAnd) {
		this.nodeConfigFilterIsAnd = nodeConfigFilterIsAnd;
	}

	public String getNodeConfigTargetDatetime() {
		return nodeConfigTargetDatetime;
	}

	public void setNodeConfigTargetDatetime(String nodeConfigTargetDatetime) {
		this.nodeConfigTargetDatetime = nodeConfigTargetDatetime;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
