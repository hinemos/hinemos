/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.bean;

import javax.xml.bind.annotation.XmlType;

/**
*
* イベントカスタムコマンドの実行結果情報を保持するDTOです。<BR>
*
*/
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class EventCustomCommandResult {
	private EventDataInfo event = null;//イベント情報
	private Integer status = null;//ステータス（1：正常、2：警告、3：エラー、9：キャンセル）
	private Integer returnCode = null;//リターンコード
	private String message = null;//メッセージ
	private Long startTime = null;//開始時間
	private Long endTime = null;//終了時間
	
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
	public Long getStartTime() {
		return startTime;
	}
	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}
	public Long getEndTime() {
		return endTime;
	}
	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}
}
