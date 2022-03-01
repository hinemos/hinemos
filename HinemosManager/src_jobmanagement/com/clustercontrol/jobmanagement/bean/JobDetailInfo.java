/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobDetailInfo implements Serializable {

	private static final long serialVersionUID = -4404769115576418195L;

	/** 実行状態(履歴で利用) */
	private Integer status;

	/** 終了状態 */
	private Integer endStatus;

	/** 終了値 */
	private Integer endValue;

	/** ファシリティID */
	private String facilityId;

	/** スコープ */
	private String scope;

	/** 時刻 */
	private List<Long> waitRuleTimeList;

	/** 開始時刻 */
	private Long startDate;

	/** 終了時刻 */
	private Long endDate;
	
	/** 実行回数 */
	private Integer runCount;
	
	/** スキップ */
	private Boolean skip;

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getEndStatus() {
		return endStatus;
	}

	public void setEndStatus(Integer endStatus) {
		this.endStatus = endStatus;
	}

	public Integer getEndValue() {
		return endValue;
	}

	public void setEndValue(Integer endValue) {
		this.endValue = endValue;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public List<Long> getWaitRuleTimeList() {
		return waitRuleTimeList;
	}

	public void setWaitRuleTimeList(List<Long> waitRuleTimeList) {
		this.waitRuleTimeList = waitRuleTimeList;
	}

	public Long getStartDate() {
		return startDate;
	}

	public void setStartDate(Long startDate) {
		this.startDate = startDate;
	}

	public Long getEndDate() {
		return endDate;
	}

	public void setEndDate(Long endDate) {
		this.endDate = endDate;
	}
	
	public Integer getRunCount() {
		return runCount;
	}

	public void setRunCount(Integer runCount) {
		this.runCount = runCount;
	}

	public Boolean getSkip() {
		return skip;
	}

	public void setSkip(Boolean skip) {
		this.skip = skip;
	}
}
