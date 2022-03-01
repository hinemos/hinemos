/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto;

import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;

public class DeviceSearchMessageInfoResponse {

	private String itemName;
	@RestPartiallyTransrateTarget
	private String itemNameTransrate;
	@RestPartiallyTransrateTarget
	private String lastVal;
	@RestPartiallyTransrateTarget
	private String thisVal;

	public DeviceSearchMessageInfoResponse() {
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getItemNameTransrate() {
		return itemNameTransrate;
	}

	public void setItemNameTransrate(String itemNameTransrate) {
		this.itemNameTransrate = itemNameTransrate;
	}

	public String getLastVal() {
		return lastVal;
	}

	public void setLastVal(String lastVal) {
		this.lastVal = lastVal;
	}

	public String getThisVal() {
		return thisVal;
	}

	public void setThisVal(String thisVal) {
		this.thisVal = thisVal;
	}
}
