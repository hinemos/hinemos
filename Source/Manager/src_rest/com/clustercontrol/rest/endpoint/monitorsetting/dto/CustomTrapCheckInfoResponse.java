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

public class CustomTrapCheckInfoResponse {
	private String targetKey;
	@RestBeanConvertEnum
	private ConvertFlagEnum convertFlg;

	public CustomTrapCheckInfoResponse() {
	}

	public String getTargetKey() {
		return targetKey;
	}
	public void setTargetKey(String targetKey) {
		this.targetKey = targetKey;
	}
	public ConvertFlagEnum getConvertFlg() {
		return convertFlg;
	}
	public void setConvertFlg(ConvertFlagEnum convertFlg) {
		this.convertFlg = convertFlg;
	}
	@Override
	public String toString() {
		return "CustomTrapCheckInfo [targetKey="
				+ targetKey + ", convertFlg=" + convertFlg + "]";
	}

}