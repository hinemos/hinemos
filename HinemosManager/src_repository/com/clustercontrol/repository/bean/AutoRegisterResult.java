/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://repository.ws.clustercontrol.com")
public class AutoRegisterResult {

	/** 処理結果ステータス */
	private AutoRegisterStatus resultStatus;

	/** 自動登録済FacilityID */
	private String facilityId;

	public AutoRegisterResult() {
	}

	public AutoRegisterResult(AutoRegisterStatus resultStatus, String facilityId) {
		this.setResultStatus(resultStatus);
		this.setFacilityId(facilityId);
	}

	public AutoRegisterStatus getResultStatus() {
		return resultStatus;
	}

	public void setResultStatus(AutoRegisterStatus resultStatus) {
		this.resultStatus = resultStatus;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

}
