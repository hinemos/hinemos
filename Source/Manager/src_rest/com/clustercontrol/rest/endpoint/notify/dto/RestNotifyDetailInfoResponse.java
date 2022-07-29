/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

public class RestNotifyDetailInfoResponse {

	private Boolean infoValidFlg;
	private Boolean warnValidFlg;
	private Boolean criticalValidFlg;
	private Boolean unknownValidFlg;

	private String infoRestAccessId;
	private String warnRestAccessId;
	private String criticalRestAccessId;
	private String unknownRestAccessId;

	public RestNotifyDetailInfoResponse() {
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

	public String getInfoRestAccessId() {
		return infoRestAccessId;
	}

	public void setInfoRestAccessId(String infoRestAccessId) {
		this.infoRestAccessId = infoRestAccessId;
	}

	public String getWarnRestAccessId() {
		return warnRestAccessId;
	}

	public void setWarnRestAccessId(String warnRestAccessId) {
		this.warnRestAccessId = warnRestAccessId;
	}

	public String getCriticalRestAccessId() {
		return criticalRestAccessId;
	}

	public void setCriticalRestAccessId(String criticalRestAccessId) {
		this.criticalRestAccessId = criticalRestAccessId;
	}

	public String getUnknownRestAccessId() {
		return unknownRestAccessId;
	}

	public void setUnknownRestAccessId(String unknownRestAccessId) {
		this.unknownRestAccessId = unknownRestAccessId;
	}

}
 