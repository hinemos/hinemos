/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

public class MessageNotifyDetailInfoResponse {
	private Boolean infoValidFlg;

	private Boolean warnValidFlg;

	private Boolean criticalValidFlg;

	private Boolean unknownValidFlg;

	private String infoRulebaseId;

	private String warnRulebaseId;

	private String criticalRulebaseId;

	private String unknownRulebaseId;

	public MessageNotifyDetailInfoResponse() {
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

	public String getInfoRulebaseId() {
		return infoRulebaseId;
	}

	public void setInfoRulebaseId(String infoRulebaseId) {
		this.infoRulebaseId = infoRulebaseId;
	}

	public String getWarnRulebaseId() {
		return warnRulebaseId;
	}

	public void setWarnRulebaseId(String warnRulebaseId) {
		this.warnRulebaseId = warnRulebaseId;
	}

	public String getCriticalRulebaseId() {
		return criticalRulebaseId;
	}

	public void setCriticalRulebaseId(String criticalRulebaseId) {
		this.criticalRulebaseId = criticalRulebaseId;
	}

	public String getUnknownRulebaseId() {
		return unknownRulebaseId;
	}

	public void setUnknownRulebaseId(String unknownRulebaseId) {
		this.unknownRulebaseId = unknownRulebaseId;
	}
}
