/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;

public class JobQueueActivityViewInfoListItemResponse {
	private String queueId;
	private String name;
	private Integer concurrency;
	private String ownerRoleId;
	@RestBeanConvertDatetime
	private String regDate;
	private String regUser;
	@RestBeanConvertDatetime
	private String updateDate;
	private String updateUser;
	private Long count;
	private Long activeCount;

	public JobQueueActivityViewInfoListItemResponse() {
	}

	public String getQueueId() {
		return queueId;
	}

	public void setQueueId(String queueId) {
		this.queueId = queueId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getConcurrency() {
		return concurrency;
	}

	public void setConcurrency(Integer concurrency) {
		this.concurrency = concurrency;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public String getRegDate() {
		return regDate;
	}

	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}

	public String getRegUser() {
		return regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	/**
	 * 現在、キューの制御下にあるジョブの数を返します。
	 */
	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	/**
	 * 現在、キューの制御下で「実行中」状態のジョブの数を返します。
	 */
	public Long getActiveCount() {
		return activeCount;
	}

	public void setActiveCount(Long activeCount) {
		this.activeCount = activeCount;
	}
}
