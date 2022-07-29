/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorresult.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIgnore;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.monitorresult.dto.enumtype.FacilityTypeEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityEnum;

public class StatusFilterInfoRequest implements RequestDto{
	@RestBeanConvertDatetime
	private String outputDateFrom = null;		//更新日時（自）
	@RestBeanConvertDatetime
	private String outputDateTo = null;		//更新日時（至）
	@RestBeanConvertDatetime
	private String generationDateFrom = null;	//出力日時（自）
	@RestBeanConvertDatetime
	private String generationDateTo = null;	//出力日時（至）
	private String monitorId = null;		//監視項目ID
	private String monitorDetailId = null;	//監視詳細
	@RestBeanConvertEnum
	private FacilityTypeEnum facilityType = null;		//対象ファシリティ種別
	private String application = null;		//アプリケーション
	private String message = null;			//メッセージ
	@RestBeanConvertDatetime
	private String outputDate = null;			//更新日時
	@RestBeanConvertDatetime
	private String generationDate = null;		//出力日時
	private String ownerRoleId = null;		//オーナーロールID
	@RestBeanConvertIgnore
	private List<PriorityEnum> priorityList = null;	//重要度リスト 
	
	public void setOutputDateFrom(String outputDateFrom) {
		this.outputDateFrom = outputDateFrom;
	}
	public String getOutputDateFrom() {
		return outputDateFrom;
	}
	public void setOutputDateTo(String outputDateTo) {
		this.outputDateTo = outputDateTo;
	}
	public String getOutputDateTo() {
		return outputDateTo;
	}
	public void setGenerationDateFrom(String generationDateFrom) {
		this.generationDateFrom = generationDateFrom;
	}
	public String getGenerationDateFrom() {
		return generationDateFrom;
	}
	public void setGenerationDateTo(String generationDateTo) {
		this.generationDateTo = generationDateTo;
	}
	public String getGenerationDateTo() {
		return generationDateTo;
	}
	public String getMonitorId() {
		return monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}
	public String getMonitorDetailId() {
		return monitorDetailId;
	}
	public void setMonitorDetailId(String monitorDetailId) {
		this.monitorDetailId = monitorDetailId;
	}
	public void setFacilityType(FacilityTypeEnum facilityType) {
		this.facilityType = facilityType;
	}
	public FacilityTypeEnum getFacilityType() {
		return facilityType;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public String getApplication() {
		return application;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getMessage() {
		return message;
	}
	public void setOutputDate(String outputDate) {
		this.outputDate = outputDate;
	}
	public String getOutputDate() {
		return outputDate;
	}
	public void setGenerationDate(String generationDate) {
		this.generationDate = generationDate;
	}
	public String getGenerationDate() {
		return generationDate;
	}
	public String getOwnerRoleId() {
		return ownerRoleId;
	}
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}
	public List<PriorityEnum> getPriorityList() {
		return priorityList;
	}
	public void setPriorityList(List<PriorityEnum> priorityList) {
		this.priorityList = priorityList;
	}
	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
