/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa.model;

import java.io.Serializable;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * RPA管理ツールの実行結果確認レスポンスの終了状態マスタのEntity定義<br>
 * 
 */
@Entity
@Table(name = "cc_rpa_management_tool_end_status_mst", schema = "setting")
@Cacheable(true)
public class RpaManagementToolEndStatusMst implements Serializable {
	private static final long serialVersionUID = 1L;
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
	private String description;
	/** 表示順序 */
	private Integer orderNo;
	/** RPA管理ツールID */
	private String rpaManagementToolId;

	public RpaManagementToolEndStatusMst() {
	}

	@Id
	@Column(name = "end_status_id")
	public Integer getEndStatusId() {
		return endStatusId;
	}

	public void setEndStatusId(Integer endStatusId) {
		this.endStatusId = endStatusId;
	}

	@Column(name = "end_status")
	public String getEndStatus() {
		return endStatus;
	}

	public void setEndStatus(String endStatus) {
		this.endStatus = endStatus;
	}

	@Column(name = "end_value")
	public Integer getEndValue() {
		return endValue;
	}

	public void setEndValue(Integer endValue) {
		this.endValue = endValue;
	}

	@Column(name = "description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "order_no")
	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	@Column(name = "rpa_management_tool_id")
	public String getRpaManagementToolId() {
		return rpaManagementToolId;
	}

	public void setRpaManagementToolId(String rpaManagementToolId) {
		this.rpaManagementToolId = rpaManagementToolId;
	}
}
