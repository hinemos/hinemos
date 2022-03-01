/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.StatusInvalidFlgEnum;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.NotifyPriorityEnum;

public class StatusNotifyDetailInfoResponse {
	private Boolean infoValidFlg;
	private Boolean warnValidFlg;
	private Boolean criticalValidFlg;
	private Boolean unknownValidFlg;

	@RestBeanConvertEnum
	private StatusInvalidFlgEnum statusInvalidFlg;
	@RestBeanConvertEnum
	private NotifyPriorityEnum statusUpdatePriority;
	private Integer statusValidPeriod;

	public StatusNotifyDetailInfoResponse() {
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

	public StatusInvalidFlgEnum getStatusInvalidFlg() {
		return statusInvalidFlg;
	}

	public void setStatusInvalidFlg(StatusInvalidFlgEnum statusInvalidFlg) {
		this.statusInvalidFlg = statusInvalidFlg;
	}

	public NotifyPriorityEnum getStatusUpdatePriority() {
		return statusUpdatePriority;
	}

	public void setStatusUpdatePriority(NotifyPriorityEnum statusUpdatePriority) {
		this.statusUpdatePriority = statusUpdatePriority;
	}

	public Integer getStatusValidPeriod() {
		return statusValidPeriod;
	}

	public void setStatusValidPeriod(Integer statusValidPeriod) {
		this.statusValidPeriod = statusValidPeriod;
	}
}
