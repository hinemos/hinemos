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
 * {@link JobHistoryFilterConditionInfo} に対応する {@link QueryCriteria} です。
 */
public class JobHistoryFilterConditionCriteria extends QueryCriteria {
	public final Period startDate;
	public final Period endDate;
	public final Like sessionId;
	public final Like jobId;
	public final Equal<Integer> status;
	public final Equal<Integer> endStatus;
	public final Like ownerRoleId;
	public final Equal<Integer> triggerType;
	public final Like triggerInfo;

	public JobHistoryFilterConditionCriteria(String uniqueId, String jobSessiobJobAlias, String jobSessionAlias) {
		super(uniqueId);
		// 短い名前の変数へ
		String a = jobSessiobJobAlias;
		String b = jobSessionAlias;

		startDate = new Period(a + ".startDate");
		endDate = new Period(a + ".endDate");
		sessionId = new Like(a + ".id.sessionId");
		jobId = new Like(a + ".id.jobId");
		status = new Equal<>(a + ".status");
		endStatus = new Equal<>(a + ".endStatus");
		ownerRoleId = new Like(a + ".ownerRoleId");
		triggerType = new Equal<>(b + ".triggerType");
		triggerInfo = new Like(b + ".triggerInfo");
	}
}
