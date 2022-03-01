/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.ConvertFlagEnum;

public class JmxCheckInfoRequest implements RequestDto {
	private String authUser;
	private String authPassword;
	private Integer port;
	@RestBeanConvertEnum
	private ConvertFlagEnum convertFlg;
	private String masterId;
	private String urlFormatName;
	public JmxCheckInfoRequest() {
	}
	public String getAuthUser() {
		return authUser;
	}
	public void setAuthUser(String authUser) {
		this.authUser = authUser;
	}
	public String getAuthPassword() {
		return authPassword;
	}
	public void setAuthPassword(String authPassword) {
		this.authPassword = authPassword;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public ConvertFlagEnum getConvertFlg() {
		return convertFlg;
	}
	public void setConvertFlg(ConvertFlagEnum convertFlg) {
		this.convertFlg = convertFlg;
	}
	public String getMasterId() {
		return masterId;
	}
	public void setMasterId(String masterId) {
		this.masterId = masterId;
	}
	public String getUrlFormatName() {
		return urlFormatName;
	}
	public void setUrlFormatName(String urlFormatName) {
		this.urlFormatName = urlFormatName;
	}
	@Override
	public String toString() {
		return "JmxCheckInfo [authUser="
				+ authUser + ", authPassword=" + authPassword + ", port=" + port + ", convertFlg=" + convertFlg
				+ ", masterId=" + masterId + ", urlFormatName=" + urlFormatName + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}