/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.bean;

import java.util.List;

import javax.xml.bind.annotation.XmlType;

/**
*
* イベントカスタムコマンドの実行結果情報を保持するDTOです。<BR>
*
*/
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class EventCustomCommandResultRoot {
	
	private Integer commandNo = null;//イベント番号
	private Integer count = null;//実行件数
	private Long commandKickTime = null;//コマンド起動日時
	private Long commandStartTime = null;//開始日時
	private Long commandEndTime = null;//終了日時
	private List<EventCustomCommandResult> eventResultList;//イベント情報
	
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
	public Long getCommandKickTime() {
		return commandKickTime;
	}
	public void setCommandKickTime(Long commandKickTime) {
		this.commandKickTime = commandKickTime;
	}
	public Long getCommandStartTime() {
		return commandStartTime;
	}
	public void setCommandStartTime(Long commandStartTime) {
		this.commandStartTime = commandStartTime;
	}
	public Long getCommandEndTime() {
		return commandEndTime;
	}
	public void setCommandEndTime(Long commandEndTime) {
		this.commandEndTime = commandEndTime;
	}
	public List<EventCustomCommandResult> getEventResultList() {
		return eventResultList;
	}
	public void setEventResultList(List<EventCustomCommandResult> eventResultList) {
		this.eventResultList = eventResultList;
	}
}
