/*
 * 

 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.ConvertFlagEnum;

public class SnmpCheckInfoResponse {
	@RestBeanConvertEnum
	private ConvertFlagEnum convertFlg;
	private String snmpOid;
	private String communityName;
	private Integer snmpPort;

	public SnmpCheckInfoResponse() {
	}

	public ConvertFlagEnum getConvertFlg() {
		return convertFlg;
	}
	public void setConvertFlg(ConvertFlagEnum convertFlg) {
		this.convertFlg = convertFlg;
	}
	public String getSnmpOid() {
		return snmpOid;
	}
	public void setSnmpOid(String snmpOid) {
		this.snmpOid = snmpOid;
	}
	public String getCommunityName() {
		return communityName;
	}
	public void setCommunityName(String communityName) {
		this.communityName = communityName;
	}
	public Integer getSnmpPort() {
		return snmpPort;
	}
	public void setSnmpPort(Integer snmpPort) {
		this.snmpPort = snmpPort;
	}
	@Override
	public String toString() {
		return "SnmpCheckInfo [convertFlg="
				+ convertFlg + ", snmpOid=" + snmpOid + ", communityName=" + communityName
				+ ", snmpPort=" + snmpPort + "]";
	}

}