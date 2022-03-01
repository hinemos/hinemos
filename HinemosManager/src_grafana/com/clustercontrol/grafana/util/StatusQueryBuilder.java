/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.grafana.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.endpoint.grafana.dto.GetStatusAggregationConditionsRequest;
import com.clustercontrol.rest.endpoint.grafana.dto.GetStatusAggregationFilterRequest;
import com.clustercontrol.rest.endpoint.grafana.dto.GetStatusAggregationRequest;
import com.clustercontrol.rest.endpoint.grafana.dto.GetStatusAggregationSortRequest;
import com.clustercontrol.rest.endpoint.grafana.dto.enumtype.StatusGroupByEnum;
import com.clustercontrol.rest.util.RestCommonConverter;
import com.clustercontrol.util.MessageConstant;

/**
 * ステータス集計のSQLを組み立てます
 *
 */
public class StatusQueryBuilder {

	private static Logger logger = Logger.getLogger(StatusQueryBuilder.class);

	/** 重要度の種類の総数 */
	public static final int PRIORITY_VARIATION = 4;

	public String build(GetStatusAggregationRequest dtoReq, List<String> roleIds, boolean useTmpTable) throws HinemosUnknown, InvalidSetting {
		// GetStatusAggregationRequestを利用してクエリを組み立てる
		StringBuilder sbSql = new StringBuilder();
		List<StatusGroupByEnum> groupBy = dtoReq.getGroupBy();
		List<String> selectElem = new ArrayList<String>();
		List<String> groupByElem = new ArrayList<String>();
		
		if (!groupBy.isEmpty()) {
			for (StatusGroupByEnum g : groupBy) {
				selectElem.add(g.getCode());
				groupByElem.add(g.getCode());
			}
		}

		// SELECT 句
		sbSql.append("SELECT ");
		if (!selectElem.isEmpty()) {
			sbSql.append(String.join(", ", selectElem));
			sbSql.append(", ");
		}
		sbSql.append("count(*)");

		// FROM 句
		sbSql.append(" FROM log.cc_status_info s");

		// WHERE 句
		List<String> whereElem = new ArrayList<String>();
		GetStatusAggregationFilterRequest filter = dtoReq.getFilter();

		// facility ID
		if (useTmpTable) {
			whereElem.add("s.facility_id IN (SELECT f.facility_id FROM cc_target_facility_ids f)");
		}

		// conditions
		GetStatusAggregationConditionsRequest condition = filter.getCondition();

		// priority
		List<Integer> priorityElem = condition.getPriorityCodes();
		if (!priorityElem.isEmpty()) {
			if (priorityElem.size() < PRIORITY_VARIATION) {
				whereElem.add(QueryBuildHelper.buildInClause("s.priority", priorityElem));
			}
		} else {
			whereElem.add("1 = 0");
		}

		// outputDateFrom
		String outputDateFrom = condition.getOutputDateFrom();
		if (!StringUtils.isEmpty(outputDateFrom)) {
			Long from = RestCommonConverter.convertDTStringToHinemosTime(outputDateFrom, MessageConstant.UPDATE_DATE_FROM.getMessage());
			whereElem.add(QueryBuildHelper.buildDateCondition("s.output_date", ">=", from));
		}

		// outputDateTo
		String outputDateTo = condition.getOutputDateTo();
		if (!StringUtils.isEmpty(outputDateTo)) {
			Long to = RestCommonConverter.convertDTStringToHinemosTime(outputDateTo, MessageConstant.UPDATE_DATE_TO.getMessage());
			whereElem.add(QueryBuildHelper.buildDateCondition("s.output_date", "<=", to));
		}

		// generationDateFrom
		String generationDateFrom = condition.getGenerationDateFrom();
		if (!StringUtils.isEmpty(generationDateFrom)) {
			Long from = RestCommonConverter.convertDTStringToHinemosTime(generationDateFrom, MessageConstant.GENERATION_DATE_FROM.getMessage());
			whereElem.add(QueryBuildHelper.buildDateCondition("s.generation_date", ">=", from));
		}

		// generationDateTo
		String generationDateTo = condition.getGenerationDateTo();
		if (!StringUtils.isEmpty(generationDateTo)) {
			Long to = RestCommonConverter.convertDTStringToHinemosTime(generationDateTo, MessageConstant.GENERATION_DATE_TO.getMessage());
			whereElem.add(QueryBuildHelper.buildDateCondition("s.generation_date", "<=", to));
		}

		// monitor_id
		String monitorId = condition.getMonitorId();
		if (!StringUtils.isEmpty(monitorId)) {
			whereElem.add(QueryBuildHelper.buildLikeClause("s.monitor_id", QueryBuildHelper.escapeQuote(monitorId)));
		}

		// monitor_detail
		String monitorDetail = condition.getMonitorDetail();
		if (!StringUtils.isEmpty(monitorDetail)) {
			whereElem.add(QueryBuildHelper.buildLikeClause("s.monitor_detail_id", QueryBuildHelper.escapeQuote(monitorDetail)));
		}

		// application
		String application = condition.getApplication();
		if (!StringUtils.isEmpty(application)) {
			whereElem.add(QueryBuildHelper.buildLikeClause("s.application", QueryBuildHelper.escapeQuote(application)));
		}

		// message
		String message = condition.getMessage();
		if (!StringUtils.isEmpty(message)) {
			whereElem.add(QueryBuildHelper.buildLikeClause("s.message", QueryBuildHelper.escapeQuote(message)));
		}

		// ownerRoleId
		String ownerRoleId = condition.getOwnerRoleId();
		if (!StringUtils.isEmpty(ownerRoleId)) {
			whereElem.add(QueryBuildHelper.buildLikeClause("s.owner_role_id", QueryBuildHelper.escapeQuote(ownerRoleId)));
		}

		// オブジェクト権限
		if (!roleIds.isEmpty()) {
			StringBuilder op = new StringBuilder();
			op.append("(");

			op.append(QueryBuildHelper.buildInClauseWithString("s.owner_role_id", roleIds));

			op.append(" OR ");

			op.append("EXISTS ");

			op.append("(");
			op.append("SELECT 1 FROM setting.cc_object_privilege op");
			op.append(" WHERE op.object_type = ");
			op.append(QueryBuildHelper.quoteString(HinemosModuleConstant.MONITOR));
			op.append(" AND op.object_privilege = ");
			op.append(QueryBuildHelper.quoteString(ObjectPrivilegeMode.READ.name()));
			op.append(" AND op.object_id = s.monitor_id");
			op.append(" AND ");
			op.append(QueryBuildHelper.buildInClauseWithString("op.role_id", roleIds));
			op.append(")");

			op.append(")");

			whereElem.add(op.toString());
		}

		if (!whereElem.isEmpty()) {
			sbSql.append(" WHERE ");
			sbSql.append(String.join(" AND ", whereElem));
		}

		// GROUP BY 句
		if (!groupByElem.isEmpty()) {
			sbSql.append(" GROUP BY ");
			sbSql.append(String.join(", ", groupByElem));
		}

		
		// ORDER BY 句
		List<GetStatusAggregationSortRequest> sortList = dtoReq.getSort();
		if (!sortList.isEmpty()) {
			List<String> orderByElem = new ArrayList<String>();
			for (GetStatusAggregationSortRequest s : sortList) {
				StringBuilder sbOrderBy = new StringBuilder();
				sbOrderBy.append(s.getSortKey().getCode());
				sbOrderBy.append(" ");
				sbOrderBy.append(s.getOrder());
				orderByElem.add(sbOrderBy.toString());
			}
			sbSql.append(" ORDER BY ");
			sbSql.append(String.join(", ", orderByElem));
		}

		// LIMIT 句
		Integer size = dtoReq.getSize();
		long MAX_STATUS_SIZE = HinemosPropertyCommon.grafana_query_status_aggregation_max_count.getNumericValue();
		sbSql.append(" LIMIT ");
		if (size != null && size != 0) {
			sbSql.append(Math.min((long)size, MAX_STATUS_SIZE));
		} else {
			sbSql.append(MAX_STATUS_SIZE);
		}

		sbSql.append(";");

		return sbSql.toString();
	}

}
