/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.dto;

import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;

public class RecordRegistrationResponse {
	private ImportResultEnum  result = ImportResultEnum.NORMAL;
	private String importKeyValue ;
	private RecordRegistrationExceptionResponse exceptionInfo;

	public RecordRegistrationResponse() {
	}

	public ImportResultEnum getResult() {
		return result;
	}

	public void setResult(ImportResultEnum result) {
		this.result = result;
	}
	
	public String getImportKeyValue() {
		return this.importKeyValue;
	}

	public void setImportKeyValue(String keyValue) {
		this.importKeyValue = keyValue;
	}

	public RecordRegistrationExceptionResponse getExceptionInfo() {
		return exceptionInfo;
	}

	public void setExceptionInfo(RecordRegistrationExceptionResponse exception) {
		this.exceptionInfo = exception;
	}
}
