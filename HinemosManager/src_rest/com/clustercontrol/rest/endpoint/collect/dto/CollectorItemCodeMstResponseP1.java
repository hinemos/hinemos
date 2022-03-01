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

public class CollectorItemCodeMstResponseP1 {

	public CollectorItemCodeMstResponseP1(){
	}

	private List<CollectorItemInfoResponse> availableCollectorItemList = new ArrayList<>();

	public List<CollectorItemInfoResponse> getAvailableCollectorItemList(){
		return this.availableCollectorItemList;
	}

	public void setAvailableCollectorItemList(List<CollectorItemInfoResponse> availableCollectorItemList){
		this.availableCollectorItemList = availableCollectorItemList;
	}
}
