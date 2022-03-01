/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.filtersetting.bean;

import com.clustercontrol.commons.util.QueryCriteria;
import com.clustercontrol.notify.monitor.model.EventLogEntity;

/**
 * {@link EventFilterBaseInfo} に対応する {@link QueryCriteria} です。
 */
public class EventFilterBaseCriteria extends QueryCriteria {
	public In<String> facilityIds;

	public EventFilterBaseCriteria(String uniqueId, String eventLogAlias) {
		super(uniqueId);

		facilityIds = new In<>(eventLogAlias + ".id.facilityId");
	}

	/**
	 * イベント履歴のエンティティがこの条件を満たす場合は true を、そうでなければ false を返します。
	 */
	public boolean matches(EventLogEntity event) {
		return facilityIds.contains(event.getId().getFacilityId());
	}
}