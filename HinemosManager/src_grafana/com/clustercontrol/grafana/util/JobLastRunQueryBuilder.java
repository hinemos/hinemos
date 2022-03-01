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
import com.clustercontrol.rest.endpoint.grafana.dto.GetJobLastRunTimeListRequest;
import com.clustercontrol.rest.util.RestCommonConverter;
import com.clustercontrol.util.MessageConstant;

/**
 * ジョブ最終実行時刻取得のSQLを組み立てます
 *
 */
public class JobLastRunQueryBuilder {

	private static Logger logger = Logger.getLogger(JobLastRunQueryBuilder.class);

	public String build(GetJobLastRunTimeListRequest dtoReq, List<String> roleIds) throws HinemosUnknown, InvalidSetting {
		// GetJobLastRunTimeRequestを利用してクエリを組み立てる
		StringBuilder sbSql = new StringBuilder();
		StringBuilder sbSubQuery = new StringBuilder();

		// サブクエリ
		// SELECT 句
		sbSubQuery.append("SELECT trigger_info, max(session_id) AS session_id");

		// FROM 句
		sbSubQuery.append(" FROM log.cc_job_session js JOIN log.cc_job_session_job jsj USING(session_id)");

		// WHERE 句
		List<String> whereElem = new ArrayList<String>();

		// jobKickIds
		List<String> jobKickIds = dtoReq.getJobKickIds();
		if (!jobKickIds.isEmpty()) {
			whereElem.add(buildJobKickIdsCondition(jobKickIds));
		}

		// startDateFrom
		String startDateFrom = dtoReq.getStartDateFrom();
		if (!StringUtils.isEmpty(startDateFrom)) {
			Long from = RestCommonConverter.convertDTStringToHinemosTime(startDateFrom, MessageConstant.JOB_START_DATE_FROM.getMessage());
			whereElem.add(QueryBuildHelper.buildDateCondition("jsj.start_date", ">=", from));
		}

		// startDateTo
		String startDateTo = dtoReq.getStartDateTo();
		if (!StringUtils.isEmpty(startDateTo)) {
			Long to = RestCommonConverter.convertDTStringToHinemosTime(startDateTo, MessageConstant.JOB_START_DATE_TO.getMessage());
			whereElem.add(QueryBuildHelper.buildDateCondition("jsj.start_date", "<=", to));
		}

		// endDateFrom
		String endDateFrom = dtoReq.getEndDateFrom();
		if (!StringUtils.isEmpty(endDateFrom)) {
			Long from = RestCommonConverter.convertDTStringToHinemosTime(endDateFrom, MessageConstant.JOB_END_DATE_FROM.getMessage());
			whereElem.add(QueryBuildHelper.buildDateCondition("jsj.end_date", ">=", from));
		}

		// endDateTo
		String endDateTo = dtoReq.getEndDateTo();
		if (!StringUtils.isEmpty(endDateTo)) {
			Long to = RestCommonConverter.convertDTStringToHinemosTime(endDateTo, MessageConstant.JOB_END_DATE_TO.getMessage());
			whereElem.add(QueryBuildHelper.buildDateCondition("jsj.end_date", "<=", to));
		}

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

		sbSubQuery.append(" WHERE parent_job_id = 'TOP'");

		if (!whereElem.isEmpty()) {
			sbSubQuery.append(" AND ");
			sbSubQuery.append(String.join(" AND ", whereElem));
		}

		// GROUP BY 句
		sbSubQuery.append(" GROUP BY trigger_info");

		
		// ORDER BY 句
		sbSubQuery.append(" ORDER BY session_id DESC");

		// LIMIT 句
		Integer size = dtoReq.getSize();
		long MAX_JOB_LAST_RUN_SIZE = HinemosPropertyCommon.grafana_query_job_last_run_time_max_count.getNumericValue();
		sbSubQuery.append(" LIMIT ");
		if (size != null && size != 0) {
			sbSubQuery.append(Math.min((long)size, MAX_JOB_LAST_RUN_SIZE));
		} else {
			sbSubQuery.append(MAX_JOB_LAST_RUN_SIZE);
		}

		// main query
		// SELECT 句
		sbSql.append("SELECT trigger_info, job_id, to_timestamp(start_date/1000) AS latest_start_date, to_timestamp(end_date/1000) AS latest_end_date");
		
		// FROM 句
		sbSql.append(" FROM (");
		sbSql.append(sbSubQuery.toString());
		sbSql.append(") sq JOIN log.cc_job_session_job USING(session_id)");
		
		// WHERE 句
		sbSql.append(" WHERE parent_job_id = 'TOP'");

		// ORDER BY 句
		sbSql.append(" ORDER BY session_id DESC");

		sbSql.append(";");

		return sbSql.toString();
	}
	
	/**
	 * jobkickId の WHERE 句の条件を生成します。
	 * 
	 * @param jobKickIds
	 * @return
	 */
	private String buildJobKickIdsCondition(List<String> jobKickIds) {
		StringBuilder sb = new StringBuilder();
		List<String> jobKickIdArray = new ArrayList<String>();

		sb.append("trigger_info ~~* ANY(ARRAY[");

		for (String jobKickId : jobKickIds) {
			StringBuilder sbJobKickId = new StringBuilder();
			sbJobKickId.append("'%(");
			sbJobKickId.append(QueryBuildHelper.escapeQuote(jobKickId));
			sbJobKickId.append(")%'");
			jobKickIdArray.add(sbJobKickId.toString());
		}
		sb.append(String.join(",", jobKickIdArray));
		sb.append("])");
		
		return sb.toString();
	}

}
