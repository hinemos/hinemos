/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.clustercontrol.util.HinemosTime;

/**
 * ジョブキュー(同時実行制御キュー)の設定ビューのフィルタです。
 * 
 * @since 6.2.0
 */
public class JobQueueSettingViewFilter {

	private String queueId;
	private String queueName;
	private Integer concurrencyFrom;
	private Integer concurrencyTo;
	private String ownerRoleId;
	private String regUser;
	private Long regDateFrom;
	private Long regDateTo;
	private String updateUser;
	private Long updateDateFrom;
	private Long updateDateTo;

	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		sdf.setTimeZone(HinemosTime.getTimeZone());
		return "queueId=" + queueId
				+ ", queueName=" + queueName
				+ ", concurrencyFrom=" + concurrencyFrom
				+ ", concurrencyTo=" + concurrencyTo
				+ ", ownerRoleId=" + ownerRoleId
				+ ", regUser=" + regUser
				+ ", regDateFrom=" + dateStr(sdf, regDateFrom)
				+ ", regDateTo=" + dateStr(sdf, regDateTo)
				+ ", updateUser=" + updateUser
				+ ", updateDateFrom=" + dateStr(sdf, updateDateFrom)
				+ ", updateDateTo=" + dateStr(sdf, updateDateTo);
	}
	
	private String dateStr(DateFormat df, Long date) {
		if (date == null) return "null";
		return df.format(new Date(date));
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

	public Long getRegDateFrom() {
		return regDateFrom;
	}

	public void setRegDateFrom(Long regDateFrom) {
		this.regDateFrom = regDateFrom;
	}

	public Long getRegDateTo() {
		return regDateTo;
	}

	public void setRegDateTo(Long regDateTo) {
		this.regDateTo = regDateTo;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public Long getUpdateDateFrom() {
		return updateDateFrom;
	}

	public void setUpdateDateFrom(Long updateDateFrom) {
		this.updateDateFrom = updateDateFrom;
	}

	public Long getUpdateDateTo() {
		return updateDateTo;
	}

	public void setUpdateDateTo(Long updateDateTo) {
		this.updateDateTo = updateDateTo;
	}
}
