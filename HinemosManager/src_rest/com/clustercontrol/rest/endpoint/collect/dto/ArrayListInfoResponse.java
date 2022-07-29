/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.collect.dto;

import java.util.ArrayList;

public class ArrayListInfoResponse {

	private ArrayList<CollectDataInfoResponse> list1 = new ArrayList<>();
	
	public ArrayListInfoResponse() {}

	public ArrayList<CollectDataInfoResponse> getList1() {
		return list1;
	}
	public void setList1(ArrayList<CollectDataInfoResponse> list1) {
		this.list1 = list1;
	}

	public int size() {
		return list1.size();
	}
}