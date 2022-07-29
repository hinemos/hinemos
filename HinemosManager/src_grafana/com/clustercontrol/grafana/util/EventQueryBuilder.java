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
import com.clustercontrol.rest.endpoint.grafana.dto.GetEventAggregationConditionsRequest;
import com.clustercontrol.rest.endpoint.grafana.dto.GetEventAggregationFilterRequest;
import com.clustercontrol.rest.endpoint.grafana.dto.GetEventAggregationRequest;
import com.clustercontrol.rest.endpoint.grafana.dto.GetEventAggregationSortRequest;
import com.clustercontrol.rest.endpoint.grafana.dto.enumtype.EventGroupByEnum;
import com.clustercontrol.rest.util.RestCommonConverter;
import com.clustercontrol.util.MessageConstant;

/**
 * イベント集計のSQLを組み立てます
 *
 */
public class EventQueryBuilder {

	private static Logger logger = Logger.getLogger(EventQueryBuilder.class);

	/** 重要度の種類の総数 */
	public static final int PRIORITY_VARIATION = 4;

	/** 確認フラグの種類の総数 */
	public static final int CONFIRM_VARIATION = 3;

	public String build(GetEventAggregationRequest dtoReq, List<String> roleIds, boolean useTmpTable) throws HinemosUnknown, InvalidSetting {
		// GetEventAggregationRequestを利用してクエリを組み立てる
		StringBuilder sbSql = new StringBuilder();
		List<EventGroupByEnum> groupBy = dtoReq.getGroupBy();
		List<String> selectElem = new ArrayList<String>();
		List<String> groupByElem = new ArrayList<String>();

		if (!groupBy.isEmpty()) {
			for (EventGroupByEnum g : groupBy) {
				selectElem.add(buildSelectTarget(g));
				groupByElem.add(buildGroupByTarget(g));
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
		sbSql.append(" FROM log.cc_event_log e");

		// WHERE 句
		List<String> whereElem = new ArrayList<String>();
		GetEventAggregationFilterRequest filter = dtoReq.getFilter();

		// facility ID
		if (useTmpTable) {
			whereElem.add("e.facility_id IN (SELECT f.facility_id FROM cc_target_facility_ids f)");
		}

		// conditions
		GetEventAggregationConditionsRequest condition = filter.getCondition();

		// priority
		List<Integer> priorityElem = condition.getPriorityCodes();
		if (!priorityElem.isEmpty()) {
			if (priorityElem.size() < PRIORITY_VARIATION) {
				whereElem.add(QueryBuildHelper.buildInClause("e.priority", priorityElem));
			}
		} else {
			whereElem.add("1 = 0");
		}

		// outputDateFrom
		String outputDateFrom = condition.getOutputDateFrom();
		if (!StringUtils.isEmpty(outputDateFrom)) {
			Long from = RestCommonConverter.convertDTStringToHinemosTime(outputDateFrom, MessageConstant.OUTPUT_DATE_FROM.getMessage());
			whereElem.add(QueryBuildHelper.buildDateCondition("e.output_date", ">=", from));
		}

		// outputDateTo
		String outputDateTo = condition.getOutputDateTo();
		if (!StringUtils.isEmpty(outputDateTo)) {
			Long to = RestCommonConverter.convertDTStringToHinemosTime(outputDateTo, MessageConstant.OUTPUT_DATE_TO.getMessage());
			whereElem.add(QueryBuildHelper.buildDateCondition("e.output_date", "<=", to));
		}

		// generationDateFrom
		String generationDateFrom = condition.getGenerationDateFrom();
		if (!StringUtils.isEmpty(generationDateFrom)) {
			Long from = RestCommonConverter.convertDTStringToHinemosTime(generationDateFrom, MessageConstant.GENERATION_DATE_FROM.getMessage());
			whereElem.add(QueryBuildHelper.buildDateCondition("e.generation_date", ">=", from));
		}

		// generationDateTo
		String generationDateTo = condition.getGenerationDateTo();
		if (!StringUtils.isEmpty(generationDateTo)) {
			Long to = RestCommonConverter.convertDTStringToHinemosTime(generationDateTo, MessageConstant.GENERATION_DATE_TO.getMessage());
			whereElem.add(QueryBuildHelper.buildDateCondition("e.generation_date", "<=", to));
		}

		// monitor_id
		String monitorId = condition.getMonitorId();
		if (!StringUtils.isEmpty(monitorId)) {
			whereElem.add(QueryBuildHelper.buildLikeClause("e.monitor_id", QueryBuildHelper.escapeQuote(monitorId)));
		}

		// monitor_detail
		String monitorDetail = condition.getMonitorDetail();
		if (!StringUtils.isEmpty(monitorDetail)) {
			whereElem.add(QueryBuildHelper.buildLikeClause("e.monitor_detail_id", QueryBuildHelper.escapeQuote(monitorDetail)));
		}

		// application
		String application = condition.getApplication();
		if (!StringUtils.isEmpty(application)) {
			whereElem.add(QueryBuildHelper.buildLikeClause("e.application", QueryBuildHelper.escapeQuote(application)));
		}

		// message
		String message = condition.getMessage();
		if (!StringUtils.isEmpty(message)) {
			whereElem.add(QueryBuildHelper.buildLikeClause("e.message", QueryBuildHelper.escapeQuote(message)));
		}

		// confirm
		List<Integer> confirmElem = condition.getConfirmFlagCodes();
		if (!confirmElem.isEmpty()) {
			if (confirmElem.size() < CONFIRM_VARIATION) {
				whereElem.add(QueryBuildHelper.buildInClause("e.confirm_flg", confirmElem));
			}
		} else {
			whereElem.add("1 = 0");
		}

		// confirmUser
		String confirmUser = condition.getConfirmUser();
		if (!StringUtils.isEmpty(confirmUser)) {
			whereElem.add(QueryBuildHelper.buildLikeClause("e.confirm_user", QueryBuildHelper.escapeQuote(confirmUser)));
		}

		// comment
		String comment = condition.getComment();
		if (!StringUtils.isEmpty(comment)) {
			whereElem.add(QueryBuildHelper.buildLikeClause("e.comment", QueryBuildHelper.escapeQuote(comment)));
		}

		// commentUser
		String commentUser = condition.getCommentUser();
		if (!StringUtils.isEmpty(commentUser)) {
			whereElem.add(QueryBuildHelper.buildLikeClause("e.comment_user", QueryBuildHelper.escapeQuote(commentUser)));
		}

		// ownerRoleId
		String ownerRoleId = condition.getOwnerRoleId();
		if (!StringUtils.isEmpty(ownerRoleId)) {
			whereElem.add(QueryBuildHelper.buildLikeClause("e.owner_role_id", QueryBuildHelper.escapeQuote(ownerRoleId)));
		}

		// オブジェクト権限
		if (!roleIds.isEmpty()) {
			StringBuilder op = new StringBuilder();
			op.append("(");

			op.append(QueryBuildHelper.buildInClauseWithString("e.owner_role_id", roleIds));

			op.append(" OR ");

			op.append("EXISTS ");

			op.append("(");
			op.append("SELECT 1 FROM setting.cc_object_privilege op");
			op.append(" WHERE op.object_type = ");
			op.append(QueryBuildHelper.quoteString(HinemosModuleConstant.MONITOR));
			op.append(" AND op.object_privilege = ");
			op.append(QueryBuildHelper.quoteString(ObjectPrivilegeMode.READ.name()));
			op.append(" AND op.object_id = e.monitor_id");
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
		if (!groupBy.isEmpty()) {
			sbSql.append(" GROUP BY ");
			sbSql.append(String.join(", ", groupByElem));
		}

		
		// ORDER BY 句
		List<GetEventAggregationSortRequest> sortList = dtoReq.getSort();
		if (!sortList.isEmpty()) {
			List<String> orderByElem = new ArrayList<String>();
			for (GetEventAggregationSortRequest s : sortList) {
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
		long MAX_EVENT_SIZE = HinemosPropertyCommon.grafana_query_event_aggregation_max_count.getNumericValue();
		sbSql.append(" LIMIT ");
		if (size != null && size != 0) {
			sbSql.append(Math.min((long)size, MAX_EVENT_SIZE));
		} else {
			sbSql.append(MAX_EVENT_SIZE);
		}

		sbSql.append(";");

		return sbSql.toString();
	}

	/**
	 * SELECT の対象を構築します
	 * 
	 * @param value
	 * @return
	 */
	private String buildSelectTarget(EventGroupByEnum value) {
		StringBuilder sb = new StringBuilder();

		switch (value) {
		case OUTPUT_HOUR:
			sb.append("date_trunc('hour', to_timestamp(e.output_date / 1000)) AS output_hour");
			break;
		case OUTPUT_DAY:
			sb.append("date_trunc('day', to_timestamp(e.output_date / 1000)) AS output_day");
			break;
		case OUTPUT_MONTH:
			sb.append("date_trunc('month', to_timestamp(e.output_date / 1000)) AS output_month");
			break;
		case OUTPUT_YEAR:
			sb.append("date_trunc('year', to_timestamp(e.output_date / 1000)) AS output_year");
			break;
		case GENERATION_HOUR:
			sb.append("date_trunc('hour', to_timestamp(e.generation_date / 1000)) AS generation_hour");
			break;
		case GENERATION_DAY:
			sb.append("date_trunc('day', to_timestamp(e.generation_date / 1000)) AS generation_day");
			break;
		case GENERATION_MONTH:
			sb.append("date_trunc('month', to_timestamp(e.generation_date / 1000)) AS generation_month");
			break;
		case GENERATION_YEAR:
			sb.append("date_trunc('year', to_timestamp(e.generation_date / 1000)) AS generation_year");
			break;
		default:
			sb.append("e.");
			sb.append(value.getCode());
		}
		
		return sb.toString();
	}

	/**
	 * GROUP BY の対象を構築します
	 * 
	 * @param value
	 * @return
	 */
	private String buildGroupByTarget(EventGroupByEnum value) {
		StringBuilder sb = new StringBuilder();

		switch (value) {
		case OUTPUT_HOUR:
		case OUTPUT_DAY:
		case OUTPUT_MONTH:
		case OUTPUT_YEAR:
		case GENERATION_HOUR:
		case GENERATION_DAY:
		case GENERATION_MONTH:
		case GENERATION_YEAR:
			sb.append(value.getCode());
			break;
		default:
			sb.append("e.");
			sb.append(value.getCode());
		}
		
		return sb.toString();
	}

}
