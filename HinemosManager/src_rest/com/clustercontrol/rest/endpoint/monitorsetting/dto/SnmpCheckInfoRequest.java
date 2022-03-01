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

public class SnmpCheckInfoRequest implements RequestDto {
	@RestBeanConvertEnum
	private ConvertFlagEnum convertFlg;
	private String snmpOid;
	public SnmpCheckInfoRequest() {
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
	@Override
	public String toString() {
		return "SnmpCheckInfo [convertFlg=" + convertFlg + ", snmpOid=" + snmpOid + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}