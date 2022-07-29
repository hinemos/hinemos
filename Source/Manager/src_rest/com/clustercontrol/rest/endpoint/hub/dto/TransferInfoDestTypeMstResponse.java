/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.hub.dto;

import java.util.ArrayList;
import java.util.List;

public class TransferInfoDestTypeMstResponse {
	private String destTypeId;
	private String name;
	private String description;
	
	private List<TransferInfoDestTypePropMstResponse> destTypePropMsts = new ArrayList<>();

	public String getDestTypeId() {
		return destTypeId;
	}
	public void setDestTypeId(String destTypeId) {
		this.destTypeId = destTypeId;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public List<TransferInfoDestTypePropMstResponse> getDestTypePropMsts() {
		return destTypePropMsts;
	}
	public void setDestTypePropMsts(List<TransferInfoDestTypePropMstResponse> destTypePropMsts) {
		this.destTypePropMsts = destTypePropMsts;
	}
}
