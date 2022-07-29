/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorresult.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;

public class GetEventListResponse {

	@RestBeanConvertDatetime
	private String fromOutputDate = null;
	@RestBeanConvertDatetime
	private String toOutputDate = null;
	
	private Integer critical = 0;			//重要度:危険数
	private Integer warning = 0;			//重要度:警告数
	private Integer info = 0;				//重要度:通知数
	private Integer unKnown = 0;			//重要度:不明数
	private Integer total = 0;			//合計数
	
	public GetEventListResponse(){
	}

	/**
	 *
	 * イベント一覧
	 * 
	 */
	private List<EventLogInfoResponse> eventList = new ArrayList<>();

	/**
	 * @return the fromOutputDate
	 */
	public String getFromOutputDate() {
		return fromOutputDate;
	}

	/**
	 * @param fromOutputDate the fromOutputDate to set
	 */
	public void setFromOutputDate(String fromOutputDate) {
		this.fromOutputDate = fromOutputDate;
	}

	/**
	 * @return the toOutputDate
	 */
	public String getToOutputDate() {
		return toOutputDate;
	}

	/**
	 * @param toOutputDate the toOutputDate to set
	 */
	public void setToOutputDate(String toOutputDate) {
		this.toOutputDate = toOutputDate;
	}

	/**
	 * @return
	 */
	public Integer getCritical() {
		return critical;
	}

	/**
	 * @param critical
	 */
	public void setCritical(Integer critical) {
		this.critical = critical;
	}

	public List<EventLogInfoResponse> getEventList() {
		return this.eventList;
	}

	public void setEventList(List<EventLogInfoResponse> eventList) {
		this.eventList = eventList;
	}

	/**
	 * @return
	 */
	public Integer getInfo() {
		return info;
	}

	/**
	 * @param info
	 */
	public void setInfo(Integer info) {
		this.info = info;
	}

	/**
	 * @return
	 */
	public Integer getTotal() {
		return total;
	}

	/**
	 * @param total
	 */
	public void setTotal(Integer total) {
		this.total = total;
	}

	/**
	 * @return
	 */
	public Integer getUnKnown() {
		return unKnown;
	}

	/**
	 * @param unKnown
	 */
	public void setUnKnown(Integer unKnown) {
		this.unKnown = unKnown;
	}

	/**
	 * @return
	 */
	public Integer getWarning() {
		return warning;
	}

	/**
	 * @param warning
	 */
	public void setWarning(Integer warning) {
		this.warning = warning;
	}
}
