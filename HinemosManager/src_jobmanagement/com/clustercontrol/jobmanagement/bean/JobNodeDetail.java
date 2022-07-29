/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlType;

/**
 * ジョブ履歴のノード詳細のためのクラス。
 * session beanからこのオブジェクトが渡される。
 * 
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobNodeDetail implements Serializable {

	private static final long serialVersionUID = 1284177184128984645L;
	private Integer status = null;
	private Integer endValue = null;
	private String facilityId = null;
	private String nodeName = null;
	private Long startDate = null;
	private Long endDate = null;
	private String message = null;
	public JobNodeDetail() {}
	public JobNodeDetail(Integer status, Integer endValue, String facilityId,
			String nodeName, Long startDate, Long endDate, String message) {
		super();
		this.status = status;
		this.endValue = endValue;
		this.facilityId = facilityId;
		this.nodeName = nodeName;
		this.startDate = startDate;
		this.endDate = endDate;
		this.message = message;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
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
	public String getNodeName() {
		return nodeName;
	}
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
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
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
