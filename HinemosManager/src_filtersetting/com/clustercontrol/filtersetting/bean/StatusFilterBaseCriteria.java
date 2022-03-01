/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.filtersetting.bean;

import com.clustercontrol.commons.util.QueryCriteria;

/**
 * {@link StatusFilterBaseInfo} に対応する {@link QueryCriteria} です。
 */
public class StatusFilterBaseCriteria extends QueryCriteria {
	public In<String> facilityIds;

	public StatusFilterBaseCriteria(String uniqueId, String statusInfoAlias) {
		super(uniqueId);

		facilityIds = new In<>(statusInfoAlias + ".id.facilityId");
	}
}