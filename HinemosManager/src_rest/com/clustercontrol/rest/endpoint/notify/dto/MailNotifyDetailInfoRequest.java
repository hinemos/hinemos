/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class MailNotifyDetailInfoRequest implements RequestDto {

	private Boolean infoValidFlg;

	private Boolean warnValidFlg;

	private Boolean criticalValidFlg;

	private Boolean unknownValidFlg;

	private String infoMailAddress;

	private String warnMailAddress;

	private String criticalMailAddress;

	private String unknownMailAddress;

	@RestItemName(value = MessageConstant.MAIL_TEMPLATE_ID)
	private String mailTemplateId;


	public MailNotifyDetailInfoRequest() {
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

	public String getInfoMailAddress() {
		return infoMailAddress;
	}

	public void setInfoMailAddress(String infoMailAddress) {
		this.infoMailAddress = infoMailAddress;
	}

	public String getWarnMailAddress() {
		return warnMailAddress;
	}

	public void setWarnMailAddress(String warnMailAddress) {
		this.warnMailAddress = warnMailAddress;
	}

	public String getCriticalMailAddress() {
		return criticalMailAddress;
	}

	public void setCriticalMailAddress(String criticalMailAddress) {
		this.criticalMailAddress = criticalMailAddress;
	}

	public String getUnknownMailAddress() {
		return unknownMailAddress;
	}

	public void setUnknownMailAddress(String unknownMailAddress) {
		this.unknownMailAddress = unknownMailAddress;
	}

	public String getMailTemplateId() {
		return mailTemplateId;
	}

	public void setMailTemplateId(String mailTemplateId) {
		this.mailTemplateId = mailTemplateId;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
