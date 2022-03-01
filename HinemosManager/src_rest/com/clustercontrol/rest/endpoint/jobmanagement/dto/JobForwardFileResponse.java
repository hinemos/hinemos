/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.EndStatusEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.StatusEnum;

public class JobForwardFileResponse {
	@RestBeanConvertEnum
	private StatusEnum status = null;
	@RestBeanConvertEnum
	private EndStatusEnum endStatus = null;
	private String file = null;
	private String srcFacility = null;
	private String srcFacilityName = null;
	private String dstFacilityId = null;
	private String dstFacilityName = null;
	@RestBeanConvertDatetime
	private String startDate = null;
	@RestBeanConvertDatetime
	private String endDate = null;

	public JobForwardFileResponse() {
	}

	public StatusEnum getStatus() {
		return status;
	}

	public void setStatus(StatusEnum status) {
		this.status = status;
	}

	public EndStatusEnum getEndStatus() {
		return endStatus;
	}

	public void setEndStatus(EndStatusEnum endStatus) {
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

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}


}
