/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.ArrayList;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.FileCheckEventTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.FileCheckModifyTypeEnum;

public class AddFileCheckRequest implements RequestDto{

	/** 実行契機ID */
	private String id;

	/** 実行契機名 */
	private String name;

	/** ジョブID */
	private String jobId;

	/** ジョブユニットID */
	private String jobunitId;

	/** オーナーロールID */
	private String ownerRoleId;

	/** 有効/無効 */
	private Boolean valid = false;

	/** カレンダID */
	private String calendarId;

	/** ランタイムジョブ変数情報 */
	private ArrayList<JobRuntimeParamRequest> jobRuntimeParamList;

	/** ファイルチェック種別 */
	private String facilityId;

	private String directory;

	private String fileName;

	@RestBeanConvertEnum
	private FileCheckEventTypeEnum eventType;

	@RestBeanConvertEnum
	private FileCheckModifyTypeEnum modifyType;

	private Boolean carryOverJudgmentFlg;

	public AddFileCheckRequest(){
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean isValid() {
		return valid;
	}

	public void setValid(Boolean valid) {
		this.valid = valid;
	}

	public String getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	public String getJobunitId() {
		return jobunitId;
	}

	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public ArrayList<JobRuntimeParamRequest> getJobRuntimeParamList() {
		return jobRuntimeParamList;
	}

	public void setJobRuntimeParamList(ArrayList<JobRuntimeParamRequest> jobRuntimeParamList) {
		this.jobRuntimeParamList = jobRuntimeParamList;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public FileCheckEventTypeEnum getEventType() {
		return eventType;
	}

	public void setEventType(FileCheckEventTypeEnum eventType) {
		this.eventType = eventType;
	}

	public FileCheckModifyTypeEnum getModifyType() {
		return modifyType;
	}

	public void setModifyType(FileCheckModifyTypeEnum modifyType) {
		this.modifyType = modifyType;
	}

	public Boolean getCarryOverJudgmentFlg() {
		return carryOverJudgmentFlg;
	}

	public void setCarryOverJudgmentFlg(Boolean carryOverJudgmentFlg) {
		this.carryOverJudgmentFlg = carryOverJudgmentFlg;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
