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
import com.clustercontrol.rest.endpoint.grafana.dto.GetJobHistoryAggregationConditionsRequest;
import com.clustercontrol.rest.endpoint.grafana.dto.GetJobHistoryAggregationFilterRequest;
import com.clustercontrol.rest.endpoint.grafana.dto.GetJobHistoryAggregationRequest;
import com.clustercontrol.rest.endpoint.grafana.dto.GetJobHistoryAggregationSortRequest;
import com.clustercontrol.rest.endpoint.grafana.dto.enumtype.JobHistoryEndStatusEnum;
import com.clustercontrol.rest.endpoint.grafana.dto.enumtype.JobHistoryGroupByEnum;
import com.clustercontrol.rest.endpoint.grafana.dto.enumtype.JobHistoryJobTriggerTypeEnum;
import com.clustercontrol.rest.endpoint.grafana.dto.enumtype.JobHistoryStatusEnum;
import com.clustercontrol.rest.util.RestCommonConverter;
import com.clustercontrol.util.MessageConstant;

/**
 * ジョブ実行履歴集計のSQLを組み立てます
 *
 */
public class JobHistoryQueryBuilder {

	private static Logger logger = Logger.getLogger(JobHistoryQueryBuilder.class);

	public String build(GetJobHistoryAggregationRequest dtoReq, List<String> roleIds) throws HinemosUnknown, InvalidSetting {
		// GetJobHistoryAggregationRequestを利用してクエリを組み立てる
		StringBuilder sbSql = new StringBuilder();
		List<JobHistoryGroupByEnum> groupBy = dtoReq.getGroupBy();
		List<String> selectElem = new ArrayList<String>();
		List<String> groupByElem = new ArrayList<String>();
		
		if (!groupBy.isEmpty()) {
			for (JobHistoryGroupByEnum g : groupBy) {
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
		sbSql.append(" FROM log.cc_job_session_job jsj JOIN log.cc_job_session js USING(session_id)");

		// WHERE 句
		List<String> whereElem = new ArrayList<String>();
		GetJobHistoryAggregationFilterRequest filter = dtoReq.getFilter();

		// conditions
		GetJobHistoryAggregationConditionsRequest condition = filter.getCondition();

		// startDateFrom
		String startDateFrom = condition.getStartDateFrom();
		if (!StringUtils.isEmpty(startDateFrom)) {
			Long from = RestCommonConverter.convertDTStringToHinemosTime(startDateFrom, MessageConstant.JOB_START_DATE_FROM.getMessage());
			whereElem.add(QueryBuildHelper.buildDateCondition("jsj.start_date", ">=", from));
		}

		// startDateTo
		String startDateTo = condition.getStartDateTo();
		if (!StringUtils.isEmpty(startDateTo)) {
			Long to = RestCommonConverter.convertDTStringToHinemosTime(startDateTo, MessageConstant.JOB_START_DATE_FROM.getMessage());
			whereElem.add(QueryBuildHelper.buildDateCondition("jsj.start_date", "<=", to));
		}

		// endDateFrom
		String endDateFrom = condition.getEndDateFrom();
		if (!StringUtils.isEmpty(endDateFrom)) {
			Long from = RestCommonConverter.convertDTStringToHinemosTime(endDateFrom, MessageConstant.JOB_END_DATE_FROM.getMessage());
			whereElem.add(QueryBuildHelper.buildDateCondition("jsj.end_date", ">=", from));
		}

		// endDateTo
		String endDateTo = condition.getEndDateTo();
		if (!StringUtils.isEmpty(endDateTo)) {
			Long to = RestCommonConverter.convertDTStringToHinemosTime(endDateTo, MessageConstant.JOB_END_DATE_TO.getMessage());
			whereElem.add(QueryBuildHelper.buildDateCondition("jsj.end_date", "<=", to));
		}

		// job_id
		String jobId = condition.getJobId();
		if (!StringUtils.isEmpty(jobId)) {
			whereElem.add(QueryBuildHelper.buildLikeClause("jsj.job_id", QueryBuildHelper.escapeQuote(jobId)));
		}

		// status
		JobHistoryStatusEnum status = condition.getStatus();
		if (status != null) {
			whereElem.add(QueryBuildHelper.buildCondition("jsj.status", "=", status.getCode()));
		}

		// end_status
		JobHistoryEndStatusEnum endStatus = condition.getEndStatus();
		if (endStatus != null) {
			whereElem.add(QueryBuildHelper.buildCondition("jsj.end_status", "=", endStatus.getCode()));
		}

		// trigger_type
		JobHistoryJobTriggerTypeEnum triggerType = condition.getTriggerType();
		if (triggerType != null) {
			whereElem.add(QueryBuildHelper.buildCondition("js.trigger_type", "=", triggerType.getCode()));
		}

		// trigger_info
		String triggerInfo = condition.getTriggerInfo();
		if (!StringUtils.isEmpty(triggerInfo)) {
			whereElem.add(QueryBuildHelper.buildLikeClause("js.trigger_info", QueryBuildHelper.escapeQuote(triggerInfo)));
		}

		// ownerRoleId
		String ownerRoleId = condition.getOwnerRoleId();
		if (!StringUtils.isEmpty(ownerRoleId)) {
			whereElem.add(QueryBuildHelper.buildLikeClause("jsj.owner_role_id", QueryBuildHelper.escapeQuote(ownerRoleId)));
		}

		// parent_job_id
		whereElem.add("jsj.parent_job_id = 'TOP'");

		// オブジェクト権限
		if (!roleIds.isEmpty()) {
			StringBuilder op = new StringBuilder();
			op.append("(");

			op.append(QueryBuildHelper.buildInClauseWithString("jsj.owner_role_id", roleIds));

			op.append(" OR ");

			op.append("EXISTS ");

			op.append("(");
			op.append("SELECT 1 FROM setting.cc_object_privilege op");
			op.append(" WHERE op.object_type = ");
			op.append(QueryBuildHelper.quoteString(HinemosModuleConstant.JOB));
			op.append(" AND op.object_privilege = ");
			op.append(QueryBuildHelper.quoteString(ObjectPrivilegeMode.READ.name()));
			op.append(" AND op.object_id = jsj.jobunit_id");
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
		List<GetJobHistoryAggregationSortRequest> sortList = dtoReq.getSort();
		if (!sortList.isEmpty()) {
			List<String> orderByElem = new ArrayList<String>();
			for (GetJobHistoryAggregationSortRequest s : sortList) {
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
		long MAX_JOB_HISTORY_SIZE = HinemosPropertyCommon.grafana_query_job_history_aggregation_max_count.getNumericValue();
		sbSql.append(" LIMIT ");
		if (size != null && size != 0) {
			sbSql.append(Math.min((long)size, MAX_JOB_HISTORY_SIZE));
		} else {
			sbSql.append(MAX_JOB_HISTORY_SIZE);
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
	private String buildSelectTarget(JobHistoryGroupByEnum value) {
		StringBuilder sbTarget = new StringBuilder();

		switch (value) {
		case START_HOUR:
			sbTarget.append("date_trunc('hour', to_timestamp(jsj.start_date / 1000)) AS start_hour");
			break;
		case START_DAY:
			sbTarget.append("date_trunc('day', to_timestamp(jsj.start_date / 1000)) AS start_day");
			break;
		case START_MONTH:
			sbTarget.append("date_trunc('month', to_timestamp(jsj.start_date / 1000)) AS start_month");
			break;
		case START_YEAR:
			sbTarget.append("date_trunc('year', to_timestamp(jsj.start_date / 1000)) AS start_year");
			break;
		case END_HOUR:
			sbTarget.append("date_trunc('hour', to_timestamp(jsj.end_date / 1000)) AS end_hour");
			break;
		case END_DAY:
			sbTarget.append("date_trunc('day', to_timestamp(jsj.end_date / 1000)) AS end_day");
			break;
		case END_MONTH:
			sbTarget.append("date_trunc('month', to_timestamp(jsj.end_date / 1000)) AS end_month");
			break;
		case END_YEAR:
			sbTarget.append("date_trunc('year', to_timestamp(jsj.end_date / 1000)) AS end_year");
			break;
		case TRIGGER_TYPE:
		case TRIGGER_INFO:
			sbTarget.append("js.");
			sbTarget.append(value.getCode());
			break;
		default:
			sbTarget.append("jsj.");
			sbTarget.append(value.getCode());
		}

		return sbTarget.toString();
	}

	/**
	 * GROUP BY の対象を構築します
	 * 
	 * @param value
	 * @return
	 */
	private String buildGroupByTarget(JobHistoryGroupByEnum value) {
		StringBuilder sb = new StringBuilder();

		switch (value) {
		case START_HOUR:
		case START_DAY:
		case START_MONTH:
		case START_YEAR:
		case END_HOUR:
		case END_DAY:
		case END_MONTH:
		case END_YEAR:
			sb.append(value.getCode());
			break;
		case TRIGGER_TYPE:
		case TRIGGER_INFO:
			sb.append("js.");
			sb.append(value.getCode());
			break;
		default:
			sb.append("jsj.");
			sb.append(value.getCode());
		}

		return sb.toString();
	}
}
