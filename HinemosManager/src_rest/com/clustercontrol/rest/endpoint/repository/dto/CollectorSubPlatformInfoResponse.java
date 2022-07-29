/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.repository.dto;

public class CollectorSubPlatformInfoResponse {

	private String subPlatformId;
	private String subPlatformName;
	private String type;
	private Integer orderNo;

	public CollectorSubPlatformInfoResponse() {
	}

	public String getSubPlatformId() {
		return subPlatformId;
	}

	public void setSubPlatformId(String subPlatformId) {
		this.subPlatformId = subPlatformId;
	}

	public String getSubPlatformName() {
		return subPlatformName;
	}

	public void setSubPlatformName(String subPlatformName) {
		this.subPlatformName = subPlatformName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}
}
