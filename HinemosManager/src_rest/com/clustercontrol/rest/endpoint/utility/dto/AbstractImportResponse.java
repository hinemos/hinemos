/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.dto;

import java.util.List;

public abstract class AbstractImportResponse<T extends RecordRegistrationResponse> {
	private Boolean isOccurException;

	private List<T> resultList;

	public AbstractImportResponse() {
	}

	public List<T> getResultList() {
		return resultList;
	}

	public void setResultList(List<T> resultList) {
		this.resultList = resultList;
	}

	public Boolean getIsOccurException() {
		return isOccurException;
	}

	public void setIsOccurException(Boolean isOccurException) {
		this.isOccurException = isOccurException;
	}


}
