/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.dto;

public interface InterfaceImportRecordRequest {
	public Boolean getIsNewRecord();
	public void setIsNewRecord(Boolean isNewRecord) ;
	public String getImportKeyValue();
	public void setImportKeyValue(String keyValue) ;
}
