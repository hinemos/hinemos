/*

Copyright (C) since 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.monitor.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;


/**
 * Hinemosのステータス情報の検索条件を格納するクラスです。<BR>
 * DTOクラスとしてマネージャ、クライアント間の通信で利用します。
 *
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class StatusFilterInfo implements Serializable {

	private static final long serialVersionUID = 526029970791845324L;
	private Long outputDateFrom = null;		//更新日時（自）
	private Long outputDateTo = null;		//更新日時（至）
	private Long generationDateFrom = null;	//出力日時（自）
	private Long generationDateTo = null;	//出力日時（至）
	private String monitorId = null;		//監視項目ID
	private String monitorDetailId = null;	//監視詳細
	private Integer facilityType = null;		//対象ファシリティ種別
	private String application = null;		//アプリケーション
	private String message = null;			//メッセージ
	private Long outputDate = null;			//更新日時
	private Long generationDate = null;		//出力日時
	private String ownerRoleId = null;		//オーナーロールID
	private Integer[] priorityList = null;	//重要度リスト 
	
	public void setOutputDateFrom(Long outputDateFrom) {
		this.outputDateFrom = outputDateFrom;
	}
	public Long getOutputDateFrom() {
		return outputDateFrom;
	}
	public void setOutputDateTo(Long outputDateTo) {
		this.outputDateTo = outputDateTo;
	}
	public Long getOutputDateTo() {
		return outputDateTo;
	}
	public void setGenerationDateFrom(Long generationDateFrom) {
		this.generationDateFrom = generationDateFrom;
	}
	public Long getGenerationDateFrom() {
		return generationDateFrom;
	}
	public void setGenerationDateTo(Long generationDateTo) {
		this.generationDateTo = generationDateTo;
	}
	public Long getGenerationDateTo() {
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
	public void setFacilityType(Integer facilityType) {
		this.facilityType = facilityType;
	}
	public Integer getFacilityType() {
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
	public void setOutputDate(Long outputDate) {
		this.outputDate = outputDate;
	}
	public Long getOutputDate() {
		return outputDate;
	}
	public void setGenerationDate(Long generationDate) {
		this.generationDate = generationDate;
	}
	public Long getGenerationDate() {
		return generationDate;
	}
	public String getOwnerRoleId() {
		return ownerRoleId;
	}
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}
	public Integer[] getPriorityList() {
		return priorityList;
	}
	public void setPriorityList(Integer[] priorityList) {
		this.priorityList = priorityList;
	}

}
