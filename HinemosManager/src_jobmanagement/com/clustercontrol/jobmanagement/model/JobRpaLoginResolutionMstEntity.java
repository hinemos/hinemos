/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.model;

import java.io.Serializable;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * The persistent class for the cc_job_rpa_endvalue_condition_info database table.
 * 
 */
@Entity
@Table(name="cc_job_rpa_login_resolution_mst", schema="setting")
@Cacheable(true)
public class JobRpaLoginResolutionMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	/** 解像度 */
	private String resolution;
	/** 順序 */
	private String orderNo;

	@Id
	@Column(name="resolution")
	/**
	 * @return 解像度を返します。
	 */
	public String getResolution() {
		return resolution;
	}

	/**
	 * @param resolution
	 *            解像度を設定します。
	 */
	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	@Column(name="order_no")
	/**
	 * @return 順序を返します。
	 */
	public String getOrderNo() {
		return orderNo;
	}

	/**
	 * @param orderNo
	 *            順序を設定します。
	 */
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

}
