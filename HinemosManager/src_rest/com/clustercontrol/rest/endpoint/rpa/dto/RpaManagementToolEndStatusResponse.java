/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.rpa.dto;

import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;

public class RpaManagementToolEndStatusResponse {
	/** 終了状態ID */
	private Integer endStatusId;
	/**
	 * 終了状態<br>
	 * RPA管理ツールのレスポンスに含まれる値
	 */
	private String endStatus;
	/**
	 * 終了値<br>
	 * 終了状態をHinemosで終了値にマッピングする際のデフォルト値
	 */
	private Integer endValue;
	/** 説明 */
	@RestPartiallyTransrateTarget
	private String description;
	/** 表示順序 */
	private Integer orderNo;
	/** RPA管理ツールID */
	private String rpaManagementToolId;

	public RpaManagementToolEndStatusResponse() {
	}

	/**
	 * @return the endStatusId
	 */
	public Integer getEndStatusId() {
		return endStatusId;
	}

	/**
	 * @param endStatusId
	 *            the endStatusId to set
	 */
	public void setEndStatusId(Integer endStatusId) {
		this.endStatusId = endStatusId;
	}

	/**
	 * @return the endStatus
	 */
	public String getEndStatus() {
		return endStatus;
	}

	/**
	 * @param endStatus
	 *            the endStatus to set
	 */
	public void setEndStatus(String endStatus) {
		this.endStatus = endStatus;
	}

	/**
	 * @return the endValue
	 */
	public Integer getEndValue() {
		return endValue;
	}

	/**
	 * @param endValue
	 *            the endValue to set
	 */
	public void setEndValue(Integer endValue) {
		this.endValue = endValue;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the orderNo
	 */
	public Integer getOrderNo() {
		return orderNo;
	}

	/**
	 * @param orderNo
	 *            the orderNo to set
	 */
	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	/**
	 * @return the rpaManagementToolId
	 */
	public String getRpaManagementToolId() {
		return rpaManagementToolId;
	}

	/**
	 * @param rpaManagementToolId
	 *            the rpaManagementToolId to set
	 */
	public void setRpaManagementToolId(String rpaManagementToolId) {
		this.rpaManagementToolId = rpaManagementToolId;
	}

}
