/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.dto.RequestDto;

public class GetJobQueueListSearchRequest implements RequestDto {

	private String queueId;

	private String queueName;

	private Integer concurrencyFrom;

	private Integer concurrencyTo;

	private String ownerRoleId;

	private String regUser;
	@RestBeanConvertDatetime
	private String regDateFrom;
	@RestBeanConvertDatetime
	private String regDateTo;

	private String updateUser;
	@RestBeanConvertDatetime
	private String updateDateFrom;
	@RestBeanConvertDatetime
	private String updateDateTo;

	public GetJobQueueListSearchRequest(){
	}

	public String getQueueId() {
		return queueId;
	}

	public void setQueueId(String queueId) {
		this.queueId = queueId;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public Integer getConcurrencyFrom() {
		return concurrencyFrom;
	}

	public void setConcurrencyFrom(Integer concurrencyFrom) {
		this.concurrencyFrom = concurrencyFrom;
	}

	public Integer getConcurrencyTo() {
		return concurrencyTo;
	}

	public void setConcurrencyTo(Integer concurrencyTo) {
		this.concurrencyTo = concurrencyTo;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public String getRegUser() {
		return regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	public String getRegDateFrom() {
		return regDateFrom;
	}

	public void setRegDateFrom(String regDateFrom) {
		this.regDateFrom = regDateFrom;
	}

	public String getRegDateTo() {
		return regDateTo;
	}

	public void setRegDateTo(String regDateTo) {
		this.regDateTo = regDateTo;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public String getUpdateDateFrom() {
		return updateDateFrom;
	}

	public void setUpdateDateFrom(String updateDateFrom) {
		this.updateDateFrom = updateDateFrom;
	}

	public String getUpdateDateTo() {
		return updateDateTo;
	}

	public void setUpdateDateTo(String updateDateTo) {
		this.updateDateTo = updateDateTo;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
