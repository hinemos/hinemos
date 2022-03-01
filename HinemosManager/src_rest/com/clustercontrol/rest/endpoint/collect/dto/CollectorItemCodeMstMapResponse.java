/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.collect.dto;

import java.util.Map;

public class CollectorItemCodeMstMapResponse {

	public CollectorItemCodeMstMapResponse(){
	}

	private Map<String, CollectorItemTreeItemResponse> itemCodeMap = null;

	public Map<String, CollectorItemTreeItemResponse> getItemCodeMap(){
		return this.itemCodeMap;
	}

	public void setItemCodeMap(Map<String, CollectorItemTreeItemResponse> itemCodeMap){
		this.itemCodeMap = itemCodeMap;
	}
}
