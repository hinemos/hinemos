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
 * ファイル転送一覧情報のためのクラス。
 * session beanからこのオブジェクトが渡される。
 * 
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobForwardFile implements Serializable {

	private static final long serialVersionUID = -6648115342522409508L;
	private Integer status = null;
	private Integer endStatus = null;
	private String file = null;
	private String srcFacility = null;
	private String srcFacilityName = null;
	private String dstFacilityId = null;
	private String dstFacilityName = null;
	private Long startDate = null;
	private Long endDate = null;

	public JobForwardFile() {}
	public JobForwardFile(Integer status, Integer endStatus, String file,
			String srcFacility, String srcFacilityName, String dstFacilityId,
			String dstFacilityName, Long startDate, Long endDate) {
		super();
		this.status = status;
		this.endStatus = endStatus;
		this.file = file;
		this.srcFacility = srcFacility;
		this.srcFacilityName = srcFacilityName;
		this.dstFacilityId = dstFacilityId;
		this.dstFacilityName = dstFacilityName;
		this.startDate = startDate;
		this.endDate = endDate;
	}
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
	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
	}
	public String getSrcFacility() {
		return srcFacility;
	}
	public void setSrcFacility(String srcFacility) {
		this.srcFacility = srcFacility;
	}
	public String getSrcFacilityName() {
		return srcFacilityName;
	}
	public void setSrcFacilityName(String srcFacilityName) {
		this.srcFacilityName = srcFacilityName;
	}
	public String getDstFacilityId() {
		return dstFacilityId;
	}
	public void setDstFacilityId(String dstFacilityId) {
		this.dstFacilityId = dstFacilityId;
	}
	public String getDstFacilityName() {
		return dstFacilityName;
	}
	public void setDstFacilityName(String dstFacilityName) {
		this.dstFacilityName = dstFacilityName;
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
}
