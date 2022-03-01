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

public class CollectorItemCodeMstInfoResponse {
	public CollectorItemCodeMstInfoResponse(){
	}

	private List<CollectorItemCodeMstDataResponse> collectorItemCodeMstData = new ArrayList<>();

	public List<CollectorItemCodeMstDataResponse> getCollectorItemCodeMstData(){
		return this.collectorItemCodeMstData;
	}

	public void setCollectorItemCodeMstData(List<CollectorItemCodeMstDataResponse> collectorItemCodeMstData){
		this.collectorItemCodeMstData = collectorItemCodeMstData; 
	}

}
