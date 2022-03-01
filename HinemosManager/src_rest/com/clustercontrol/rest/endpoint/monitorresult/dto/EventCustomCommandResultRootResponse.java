/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorresult.dto;

import java.util.List;

import com.clustercontrol.monitor.bean.EventCustomCommandResult;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;

public class EventCustomCommandResultRootResponse {

	private Integer commandNo = null;//イベント番号
	private Integer count = null;//実行件数
	@RestBeanConvertDatetime
	private String commandKickTime = null;//コマンド起動日時
	@RestBeanConvertDatetime
	private String commandStartTime = null;//開始日時
	@RestBeanConvertDatetime
	private String commandEndTime = null;//終了日時
	private List<EventCustomCommandResultResponse> eventResultList;//イベント情報
	
	public Integer getCommandNo() {
		return commandNo;
	}
	public void setCommandNo(Integer commandNo) {
		this.commandNo = commandNo;
	}
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public String getCommandKickTime() {
		return commandKickTime;
	}
	public void setCommandKickTime(String commandKickTime) {
		this.commandKickTime = commandKickTime;
	}
	public String getCommandStartTime() {
		return commandStartTime;
	}
	public void setCommandStartTime(String commandStartTime) {
		this.commandStartTime = commandStartTime;
	}
	public String getCommandEndTime() {
		return commandEndTime;
	}
	public void setCommandEndTime(String commandEndTime) {
		this.commandEndTime = commandEndTime;
	}
	public List<EventCustomCommandResultResponse> getEventResultList() {
		return eventResultList;
	}
	public void setEventResultList(List<EventCustomCommandResultResponse> eventResultList) {
		this.eventResultList = eventResultList;
	}
}
