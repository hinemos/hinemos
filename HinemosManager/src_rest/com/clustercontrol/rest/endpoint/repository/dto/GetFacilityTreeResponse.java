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

public class GetFacilityTreeResponse {

	private FacilityInfoResponseP2 data;
	private List<FacilityTreeItemResponseP1> children = new ArrayList<>();
	private Integer childrenTotal;
	
	public GetFacilityTreeResponse() {
	}

	public FacilityInfoResponseP2 getData() {
		return data;
	}

	public void setData(FacilityInfoResponseP2 data) {
		this.data = data;
	}

	public List<FacilityTreeItemResponseP1> getChildren() {
		return children;
	}

	public void setChildren(List<FacilityTreeItemResponseP1> children) {
		this.children = children;
	}

	public Integer getChildrenTotal() {
		return childrenTotal;
	}

	public void setChildrenTotal(Integer childrenTotal) {
		this.childrenTotal = childrenTotal;
	}

}
