/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.EventNormalStateEnum;

public class EventNotifyDetailInfoRequest implements RequestDto {

	private Boolean infoValidFlg;

	private Boolean warnValidFlg;

	private Boolean criticalValidFlg;

	private Boolean unknownValidFlg;

	@RestBeanConvertEnum
	private EventNormalStateEnum infoEventNormalState;

	@RestBeanConvertEnum
	private EventNormalStateEnum warnEventNormalState;

	@RestBeanConvertEnum
	private EventNormalStateEnum criticalEventNormalState;

	@RestBeanConvertEnum
	private EventNormalStateEnum unknownEventNormalState;

	public EventNotifyDetailInfoRequest() {
	}

	public Boolean getInfoValidFlg() {
		return infoValidFlg;
	}

	public void setInfoValidFlg(Boolean infoValidFlg) {
		this.infoValidFlg = infoValidFlg;
	}

	public Boolean getWarnValidFlg() {
		return warnValidFlg;
	}

	public void setWarnValidFlg(Boolean warnValidFlg) {
		this.warnValidFlg = warnValidFlg;
	}

	public Boolean getCriticalValidFlg() {
		return criticalValidFlg;
	}

	public void setCriticalValidFlg(Boolean criticalValidFlg) {
		this.criticalValidFlg = criticalValidFlg;
	}

	public Boolean getUnknownValidFlg() {
		return unknownValidFlg;
	}

	public void setUnknownValidFlg(Boolean unknownValidFlg) {
		this.unknownValidFlg = unknownValidFlg;
	}

	public EventNormalStateEnum getInfoEventNormalState() {
		return infoEventNormalState;
	}

	public void setInfoEventNormalState(EventNormalStateEnum infoEventNormalState) {
		this.infoEventNormalState = infoEventNormalState;
	}

	public EventNormalStateEnum getWarnEventNormalState() {
		return warnEventNormalState;
	}

	public void setWarnEventNormalState(EventNormalStateEnum warnEventNormalState) {
		this.warnEventNormalState = warnEventNormalState;
	}

	public EventNormalStateEnum getCriticalEventNormalState() {
		return criticalEventNormalState;
	}

	public void setCriticalEventNormalState(EventNormalStateEnum criticalEventNormalState) {
		this.criticalEventNormalState = criticalEventNormalState;
	}

	public EventNormalStateEnum getUnknownEventNormalState() {
		return unknownEventNormalState;
	}

	public void setUnknownEventNormalState(EventNormalStateEnum unknownEventNormalState) {
		this.unknownEventNormalState = unknownEventNormalState;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
