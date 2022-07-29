/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.dto;

import java.util.List;

public abstract class AbstractImportRequest<T extends AbstractImportRecordRequest<?>> {
	
	private List<T> recordList;

	private Boolean isRollbackIfAbnormal ;

	public Boolean isRollbackIfAbnormal() {
		return isRollbackIfAbnormal;
	}

	public void setRollbackIfAbnormal( Boolean isRollbackIfAbnormal) {
		this.isRollbackIfAbnormal = isRollbackIfAbnormal;
	}
	
	public List<T> getRecordList() {
		return this.recordList;
	}

	public void setRecordList(List<T> recordList) {
		this.recordList = recordList;
	}
}
