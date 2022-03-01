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
 * {@link StatusFilterConditionInfo} に対応する {@link QueryCriteria} です。
 */
public class StatusFilterConditionCriteria extends QueryCriteria {
	public In<Integer> priority;
	public Period outputDate;
	public Period generationDate;
	public Like monitorId;
	public Like monitorDetail;
	public Like application;
	public Like message;
	public Like ownerRoleId;

	public StatusFilterConditionCriteria(String uniqueId, String statusInfoAlias) {
		super(uniqueId);

		// 短い名前の変数へ
		String a = statusInfoAlias;

		priority = new In<>(a + ".priority", StatusFilterConditionInfo.PRIORITY_VARIATION);
		outputDate = new Period(a + ".outputDate");
		generationDate = new Period(a + ".generationDate");
		monitorId = new Like(a + ".id.monitorId");
		monitorDetail = new Like(a + ".id.monitorDetailId");
		application = new Like(a + ".application");
		message = new Like(a + ".message");
		ownerRoleId = new Like(a + ".ownerRoleId");
	}
}
