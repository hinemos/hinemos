/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorresult.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;

public class ScopeDataInfoResponse {

	public ScopeDataInfoResponse(){
	}
	
	private String facilityId = null;	// ファシリティID
	@RestPartiallyTransrateTarget
	private String facilityPath = null;	// ファシリティパス
	private Integer priority = null;		// 重要度
	@RestBeanConvertDatetime
	private String outputDate = null;		// 最終変更日時
	private Integer sortValue = 100;
	
	/**
	 * ファシリティIDを返します。
	 * 
	 * @return ファシリティID
	 */
	public String getFacilityId() {
		return facilityId;
	}

	/**
	 * ファシリティIDを設定します。
	 * 
	 * @param facilityId
	 */
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	/**
	 * ファシリティパスを返します。
	 * @return
	 */
	public String getFacilityPath() {
		return facilityPath;
	}

	/**
	 * ファシリティパスを設定します
	 * 
	 * @param facilityPath
	 */
	public void setFacilityPath(String facilityPath) {
		this.facilityPath = facilityPath;
	}

	/**
	 * 最終変更日時を返します。
	 * 
	 * @return
	 */
	public String getOutputDate() {
		return outputDate;
	}

	/**
	 * 最終変更日時を設定します。
	 * 
	 * @param outputDate
	 */
	public void setOutputDate(String outputDate) {
		this.outputDate = outputDate;
	}

	/**
	 * 重要度を返します。
	 * 
	 * @return
	 */
	public Integer getPriority() {
		return priority;
	}

	/**
	 * 重要度を設定します。
	 * 
	 * @param priority
	 */
	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	/**
	 * ソート用パラメータを返します。
	 * 
	 * @return
	 */
	public Integer getSortValue() {
		return sortValue;
	}

	/**
	 * ソート用パラメータを設定します。
	 * 
	 * @param sortValue
	 */
	public void setSortValue(Integer sortValue) {
		this.sortValue = sortValue;
	}
}
