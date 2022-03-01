/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.repository.dto;

public class CollectorPlatformInfoResponse {

	private String platformId;
	private String platformName;
	private Short orderNo;

	public CollectorPlatformInfoResponse() {
	}

	public CollectorPlatformInfoResponse(String platformId, String platformName,
			Short orderNo) {
		setPlatformId(platformId);
		setPlatformName(platformName);
		setOrderNo(orderNo);
	}

	public CollectorPlatformInfoResponse(CollectorPlatformInfoResponse otherData) {
		setPlatformId(otherData.getPlatformId());
		setPlatformName(otherData.getPlatformName());
		setOrderNo(otherData.getOrderNo());

	}

	public String getPlatformId() {
		return this.platformId;
	}

	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}

	public String getPlatformName() {
		return this.platformName;
	}

	public void setPlatformName(String platformName) {
		this.platformName = platformName;
	}

	public Short getOrderNo() {
		return this.orderNo;
	}

	public void setOrderNo(Short orderNo) {
		this.orderNo = orderNo;
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer("{");

		str.append("platformId=" + getPlatformId() + " " + "platformName=" + getPlatformName() + " " + "orderNo="
				+ getOrderNo());
		str.append('}');

		return (str.toString());
	}
}
