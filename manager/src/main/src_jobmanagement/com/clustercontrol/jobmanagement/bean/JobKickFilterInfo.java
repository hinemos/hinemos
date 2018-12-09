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
 * ジョブ実行契機のフィルタ設定を格納するクラス<BR>
 * 
 * @version 5.1.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobKickFilterInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String jobkickId = null;		// 実行契機ID
	private String jobkickName = null;		// 実行契機名
	private Integer jobkickType = null;	// 実行契機種別
	private String jobunitId = null;		// ジョブユニットID
	private String jobId = null;			// ジョブID
	private String calendarId = null;		// カレンダID
	private Boolean validFlg = null;		// 有効フラグ
	private String ownerRoleId = null;		// オーナーロールID
	private String regUser = null;			// 新規作成者
	private Long regFromDate = 0l;			// 作成日時(From)
	private Long regToDate = 0l;			// 作成日時(To)
	private String updateUser = null;		// 最終変更者
	private Long updateFromDate = 0l;		// 最終変更日時(From)
	private Long updateToDate = 0l;		// 最終変更日時(To)

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
	public Integer getJobkickType() {
		return jobkickType;
	}
	public void setJobkickType(Integer jobkickType) {
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
	public Long getRegFromDate() {
		return regFromDate;
	}
	public void setRegFromDate(Long regFromDate) {
		this.regFromDate = regFromDate;
	}
	public Long getRegToDate() {
		return regToDate;
	}
	public void setRegToDate(Long regToDate) {
		this.regToDate = regToDate;
	}
	public String getUpdateUser() {
		return updateUser;
	}
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}
	public Long getUpdateFromDate() {
		return updateFromDate;
	}
	public void setUpdateFromDate(Long updateFromDate) {
		this.updateFromDate = updateFromDate;
	}
	public Long getUpdateToDate() {
		return updateToDate;
	}
	public void setUpdateToDate(Long updateToDate) {
		this.updateToDate = updateToDate;
	}

}
