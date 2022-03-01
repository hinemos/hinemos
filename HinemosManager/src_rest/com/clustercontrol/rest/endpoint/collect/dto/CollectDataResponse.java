/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.collect.dto;

public class CollectDataResponse {

	public CollectDataResponse(){
	}
	
	private Integer collectId;
	private ArrayListInfoResponse arrayListInfoResponse = new ArrayListInfoResponse();

	public Integer getCollectId() {
		return collectId;
	}

	public void setCollectId(Integer collectId) {
		this.collectId = collectId;
	}
	
	public ArrayListInfoResponse getArrayListInfoResponse() {
		return this.arrayListInfoResponse;
	}

	public void setArrayListInfoResponse(ArrayListInfoResponse list) {
		this.arrayListInfoResponse = list;
	}

}
