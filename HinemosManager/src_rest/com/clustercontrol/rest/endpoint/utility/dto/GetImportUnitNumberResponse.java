/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.dto;


public class GetImportUnitNumberResponse {
	/** 機能ID  */
	private String functionId;
	/** 一括処理単位数  */
	private Integer importUnitNumber;
	
	public GetImportUnitNumberResponse() {
	}

	public String getFunctionId() {
		return functionId;
	}

	public void setFunctionId(String functionId) {
		this.functionId = functionId;
	}

	public Integer getImportUnitNumber() {
		return importUnitNumber;
	}

	public void setImportUnitNumber(Integer importUnitNumber) {
		this.importUnitNumber = importUnitNumber;
	}

}
