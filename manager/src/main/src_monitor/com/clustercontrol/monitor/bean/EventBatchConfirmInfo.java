/*

Copyright (C) since 2010 NTT DATA Corporation

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
 * 
 * イベント一括確認情報を保持するDTOです。<BR>
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class EventBatchConfirmInfo implements Serializable {

	private static final long serialVersionUID = 8427283406919364254L;
	private Integer[] priorityList = null;		//重要度リスト
	private Long outputFromDate = null;		//更新日時（自）取得
	private Long outputToDate = null;			//更新日時（至）取得
	private Long generationFromDate = null;	//出力日時（自）取得
	private Long generationToDate = null;		//出力日時（至）取得
	private String monitorId = null;			//監視項目ID
	private String monitorDetailId = null;		//監視詳細
	private Integer facilityType = null;		//対象ファシリティ種別
	private String application = null;			//アプリケーション
	private String message = null;				//メッセージ
	private String comment = null;				//コメント
	private String commentUser = null;			//コメントユーザ
	private Boolean collectGraphFlg = null;	//性能グラフ用フラグ

	public void setPriorityList(Integer[] priorityList) {
		this.priorityList = priorityList;
	}
	public Integer[] getPriorityList() {
		return priorityList;
	}
	public void setOutputFromDate(Long outputFromDate) {
		this.outputFromDate = outputFromDate;
	}
	public Long getOutputFromDate() {
		return outputFromDate;
	}
	public void setOutputToDate(Long outputToDate) {
		this.outputToDate = outputToDate;
	}
	public Long getOutputToDate() {
		return outputToDate;
	}
	public void setGenerationFromDate(Long generationFromDate) {
		this.generationFromDate = generationFromDate;
	}
	public Long getGenerationFromDate() {
		return generationFromDate;
	}
	public void setGenerationToDate(Long generationToDate) {
		this.generationToDate = generationToDate;
	}
	public Long getGenerationToDate() {
		return generationToDate;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}
	public String getMonitorId() {
		return monitorId;
	}
	public void setMonitorDetailId(String monitorDetailId) {
		this.monitorDetailId = monitorDetailId;
	}
	public String getMonitorDetailId() {
		return monitorDetailId;
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
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getComment() {
		return comment;
	}
	public void setCommentUser(String commentUser) {
		this.commentUser = commentUser;
	}
	public String getCommentUser() {
		return commentUser;
	}
	public Boolean getCollectGraphFlg() {
		return collectGraphFlg;
	}
	public void setCollectGraphFlg(Boolean collectGraphFlg) {
		this.collectGraphFlg = collectGraphFlg;
	}

}
