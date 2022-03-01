/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@XmlType(namespace = "http://notify.ws.clustercontrol.com")
@Entity
@Table(name = "cc_notify_msg_info", schema = "setting")
@Cacheable(true)
public class NotifyMessageInfo extends NotifyInfoDetail implements Serializable {

	private static final long serialVersionUID = 1L;

	private String infoRulebaseId;
	private String warnRulebaseId;
	private String criticalRulebaseId;
	private String unknownRulebaseId;

	public NotifyMessageInfo() {
	}

	@Column(name = "info_rulebase_id")
	public String getInfoRulebaseId() {
		return infoRulebaseId;
	}

	public void setInfoRulebaseId(String infoRulebaseId) {
		this.infoRulebaseId = infoRulebaseId;
	}

	@Column(name = "warn_rulebase_id")
	public String getWarnRulebaseId() {
		return warnRulebaseId;
	}

	public void setWarnRulebaseId(String warnRulebaseId) {
		this.warnRulebaseId = warnRulebaseId;
	}

	@Column(name = "critical_rulebase_id")
	public String getCriticalRulebaseId() {
		return criticalRulebaseId;
	}

	public void setCriticalRulebaseId(String criticalRulebaseId) {
		this.criticalRulebaseId = criticalRulebaseId;
	}

	@Column(name = "unknown_rulebase_id")
	public String getUnknownRulebaseId() {
		return unknownRulebaseId;
	}

	public void setUnknownRulebaseId(String unknownRulebaseId) {
		this.unknownRulebaseId = unknownRulebaseId;
	}

	@Override
	protected void chainNotifyInfo() {
		getNotifyInfoEntity().setNotifyMessageInfo(this);
	}

	@Override
	protected void unchainNotifyInfo() {
		getNotifyInfoEntity().setNotifyMessageInfo(null);
	}
}
