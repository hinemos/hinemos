/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorresult.dto;

import com.clustercontrol.monitor.bean.EventDataInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;

public class EventCustomCommandResultResponse {
	private EventDataInfo event = null;//イベント情報
	private Integer status = null;//ステータス（1：正常、2：警告、3：エラー、9：キャンセル）
	private Integer returnCode = null;//リターンコード
	@RestPartiallyTransrateTarget
	private String message = null;//メッセージ
	@RestBeanConvertDatetime
	private String startTime = null;//開始時間
	@RestBeanConvertDatetime
	private String endTime = null;//終了時間
	public EventDataInfo getEvent() {
		return event;
	}
	public void setEvent(EventDataInfo event) {
		this.event = event;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getReturnCode() {
		return returnCode;
	}
	public void setReturnCode(Integer returnCode) {
		this.returnCode = returnCode;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
}
