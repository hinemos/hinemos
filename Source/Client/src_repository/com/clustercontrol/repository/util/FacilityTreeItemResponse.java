/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.repository.util;

import java.util.ArrayList;
import java.util.List;

import org.openapitools.client.model.FacilityInfoResponse;

public class FacilityTreeItemResponse {

	private FacilityTreeItemResponse parent = null;
	private FacilityInfoResponse data = null;
	private List<FacilityTreeItemResponse> children = new ArrayList<>();

	public FacilityTreeItemResponse getParent() {
		return parent;
	}

	public void setParent(FacilityTreeItemResponse parent) {
		this.parent = parent;
	}

	public FacilityInfoResponse getData() {
		return data;
	}

	public void setData(FacilityInfoResponse data) {
		this.data = data;
	}

	public List<FacilityTreeItemResponse> getChildren() {
		return children;
	}

	public void setChildren(List<FacilityTreeItemResponse> children) {
		this.children = children;
	}
}
