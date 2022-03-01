/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.dto;

public abstract class AbstractImportRecordRequest<T> {
	private Boolean isNewRecord;
	private String importKeyValue;
	private T importData ;
	
	public Boolean getIsNewRecord() {
		return isNewRecord;
	}
	public void setIsNewRecord(Boolean isNewRecord) {
		this.isNewRecord = isNewRecord;
	}
	public String getImportKeyValue() {
		return importKeyValue;
	}
	public void setImportKeyValue(String importKeyValue) {
		this.importKeyValue = importKeyValue;
	}
	public T getImportData() {
		return importData;
	}
	public void setImportData(T importData) {
		this.importData = importData;
	}

}
