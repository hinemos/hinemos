/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * 
 * スコープ情報を保持するDTOです。<BR>
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class ScopeDataInfo implements Serializable {

	private static final long serialVersionUID = -3884583445078991224L;
	private String facilityId = null;	// ファシリティID
	private String facilityPath = null;	// ファシリティパス
	private Integer priority = null;		// 重要度
	private Long outputDate = null;		// 最終変更日時
	private Integer sortValue = 100;			// ソート用パラメータ。デフォルト値を100とする。

	public ScopeDataInfo(){
		super();
	}

	public ScopeDataInfo(String facilityId, String facilityPath, Integer priority, Long outputDate, Integer sortValue){
		this.facilityId = facilityId;
		this.facilityPath = facilityPath;
		this.priority = priority;
		this.outputDate = outputDate;
		this.sortValue = sortValue;
	}

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
	public Long getOutputDate() {
		return outputDate;
	}

	/**
	 * 最終変更日時を設定します。
	 * 
	 * @param outputDate
	 */
	public void setOutputDate(Long outputDate) {
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
