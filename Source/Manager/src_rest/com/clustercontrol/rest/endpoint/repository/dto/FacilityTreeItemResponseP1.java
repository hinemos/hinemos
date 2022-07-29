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

public class FacilityTreeItemResponseP1 {

	private FacilityInfoResponseP2 data;
	private List<FacilityTreeItemResponseP1> children = new ArrayList<>();

	public FacilityTreeItemResponseP1() {
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
}
