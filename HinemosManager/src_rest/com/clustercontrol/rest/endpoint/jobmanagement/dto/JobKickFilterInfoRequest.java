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
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JobKickTypeEnum;

public class JobKickFilterInfoRequest implements RequestDto {
	// 実行契機ID
	private String jobkickId;
	// 実行契機名
	private String jobkickName;
	// 実行契機種別
	@RestBeanConvertEnum
	private JobKickTypeEnum jobkickType;
	// ジョブユニットID
	private String jobunitId;
	// ジョブID
	private String jobId;
	// カレンダID
	private String calendarId;
	// 有効フラグ
	private Boolean validFlg;
	// オーナーロールID
	private String ownerRoleId;
	// 新規作成者
	private String regUser;
	// 作成日時(From)
	@RestBeanConvertDatetime
	private String regFromDate;
	// 作成日時(To)
	@RestBeanConvertDatetime
	private String regToDate;
	// 最終変更者
	private String updateUser;
	// 最終変更日時(From)
	@RestBeanConvertDatetime
	private String updateFromDate;
	// 最終変更日時(To)
	@RestBeanConvertDatetime
	private String updateToDate;

	public JobKickFilterInfoRequest(){
	}

	public String getJobkickId() {
		return jobkickId;
	}

	public void setJobkickId(String jobkickId) {
		this.jobkickId = jobkickId;
	}

	public String getJobkickName() {
		return jobkickName;
	}

	public void setJobkickName(String jobkickName) {
		this.jobkickName = jobkickName;
	}

	public JobKickTypeEnum getJobkickType() {
		return jobkickType;
	}

	public void setJobkickType(JobKickTypeEnum jobkickType) {
		this.jobkickType = jobkickType;
	}

	public String getJobunitId() {
		return jobunitId;
	}

	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	public Boolean getValidFlg() {
		return validFlg;
	}

	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
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

	public String getRegFromDate() {
		return regFromDate;
	}

	public void setRegFromDate(String regFromDate) {
		this.regFromDate = regFromDate;
	}

	public String getRegToDate() {
		return regToDate;
	}

	public void setRegToDate(String regToDate) {
		this.regToDate = regToDate;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public String getUpdateFromDate() {
		return updateFromDate;
	}

	public void setUpdateFromDate(String updateFromDate) {
		this.updateFromDate = updateFromDate;
	}

	public String getUpdateToDate() {
		return updateToDate;
	}

	public void setUpdateToDate(String updateToDate) {
		this.updateToDate = updateToDate;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
