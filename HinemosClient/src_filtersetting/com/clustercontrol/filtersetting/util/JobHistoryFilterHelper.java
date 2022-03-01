/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openapitools.client.model.JobHistoryFilterBaseRequest;
import org.openapitools.client.model.JobHistoryFilterBaseResponse;
import org.openapitools.client.model.JobHistoryFilterConditionRequest;
import org.openapitools.client.model.JobHistoryFilterConditionRequest.TriggerTypeEnum;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.StatusMessage;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.jobmanagement.bean.HistoryFilterPropertyConstant;
import com.clustercontrol.jobmanagement.bean.JobTriggerTypeMessage;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.EndStatusEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JobTriggerTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.StatusEnum;
import com.clustercontrol.util.FilterConstant;
import com.clustercontrol.util.PropertyBuilder;
import com.clustercontrol.util.PropertyWrapper;
import com.clustercontrol.util.RestClientBeanUtil;

/**
 * ジョブ実行履歴フィルタ設定関連のヘルパーです。
 */
public class JobHistoryFilterHelper {

	/**
	 * RequestDTO を複製して返します。
	 */
	public static JobHistoryFilterBaseRequest duplicate(JobHistoryFilterBaseRequest req) {
		JobHistoryFilterBaseRequest req2 = new JobHistoryFilterBaseRequest();
		try {
			RestClientBeanUtil.convertBean(req, req2);
		} catch (HinemosUnknown e) {
			// logic error
			throw new RuntimeException("Failed to duplicate JobHistoryFilterBaseRequest.\n"
					+ "req=" + req.toString());
		}
		return req2;
	}

	/**
	 * RequestDTO から ResponseDTO へ変換して返します。
	 */
	public static JobHistoryFilterBaseResponse convertToResponse(JobHistoryFilterBaseRequest req) {
		JobHistoryFilterBaseResponse response = new JobHistoryFilterBaseResponse();
		try {
			RestClientBeanUtil.convertBean(req, response);
		} catch (HinemosUnknown e) {
			// logic error
			throw new RuntimeException("Failed to convert JobHistoryFilterBaseRequest to JobHistoryFilterBaseResponse.\n"
					+ "req=" + req.toString() + "\nres=" + response.toString());
		}
		return response;
	}

	/**
	 * ResponseDTO から RequestDTO へ変換して返します。
	 */
	public static JobHistoryFilterBaseRequest convertToRequest(JobHistoryFilterBaseResponse res) {
		JobHistoryFilterBaseRequest request = new JobHistoryFilterBaseRequest();
		try {
			RestClientBeanUtil.convertBean(res, request);
		} catch (HinemosUnknown e) {
			// logic error
			throw new RuntimeException("Failed to convert JobHistoryFilterBaseResponse to JobHistoryFilterBaseRequest.\n"
					+ "res=" + res.toString() + "\nreq=" + request.toString());
		}
		return request;
	}

	/**
	 * デフォルトのフィルタ条件(基本条件＋詳細条件1シート)を返します。
	 */
	public static JobHistoryFilterBaseRequest createDefaultFilter() {
		JobHistoryFilterBaseRequest filter = new JobHistoryFilterBaseRequest();
		filter.addConditionsItem(createDefaultCondition());
		return filter;
	}

	/**
	 * デフォルトのフィルタ詳細条件(複数指定可能な条件)の1シート分を返します。
	 */
	public static JobHistoryFilterConditionRequest createDefaultCondition() {
		JobHistoryFilterConditionRequest o = new JobHistoryFilterConditionRequest();
		return o;
	}

	/**
	 * フィルタ詳細条件を {@link Property} へ変換して返します。
	 * 
	 * @param cnd フィルタ詳細条件DTO。
	 * @return
	 */
	public static Property convertConditionToProperty(JobHistoryFilterConditionRequest cnd) {
		String SEPARATOR = FilterConstant.AND_SEPARATOR;

		Property property = new Property(null, null, "");
		property.removeChildren();

		// 開始・再実行日時
		Property startDate = new PropertyBuilder(HistoryFilterPropertyConstant.START_DATE, "start.rerun.time").build();
		startDate.addChildren(new PropertyBuilder(HistoryFilterPropertyConstant.START_FROM_DATE, "start")
				.buildDateTime(cnd.getStartDateFrom()));
		startDate.addChildren(new PropertyBuilder(HistoryFilterPropertyConstant.START_TO_DATE, "end")
				.buildDateTime(cnd.getStartDateTo()));

		property.addChildren(startDate);

		// 終了・中断日時（自）
		Property endDate = new PropertyBuilder(HistoryFilterPropertyConstant.END_DATE, "end.suspend.time").build();
		endDate.addChildren(new PropertyBuilder(HistoryFilterPropertyConstant.END_FROM_DATE, "start")
				.buildDateTime(cnd.getEndDateFrom()));
		endDate.addChildren(new PropertyBuilder(HistoryFilterPropertyConstant.END_TO_DATE, "end")
				.buildDateTime(cnd.getEndDateTo()));

		property.addChildren(endDate);

		// セッションID
		property.addChildren(new PropertyBuilder(HistoryFilterPropertyConstant.SESSION_ID, "session.id")
				.setCopiable(true).buildMultipleTexts(cnd.getSessionId(), SEPARATOR, DataRangeConstant.VARCHAR_64));

		// ジョブID
		property.addChildren(new PropertyBuilder(HistoryFilterPropertyConstant.JOB_ID, "job.id")
				.setCopiable(true).buildMultipleTexts(cnd.getJobId(), SEPARATOR, DataRangeConstant.VARCHAR_64));

		// 実行状態
		List<StatusEnum> optsStatus = new ArrayList<>(Arrays.asList(
				StatusEnum.SCHEDULED,
				StatusEnum.RUNNING,
				StatusEnum.RUNNING_QUEUE,
				StatusEnum.STOPPING,
				StatusEnum.SUSPEND,
				StatusEnum.SUSPEND_QUEUE,
				StatusEnum.STOP,
				StatusEnum.END,
				StatusEnum.END_QUEUE_LIMIT,
				StatusEnum.MODIFIED));

		property.addChildren(new PropertyBuilder(HistoryFilterPropertyConstant.STATUS, "run.status")
				.buildSelect(cnd.getStatus(), optsStatus, StatusMessage::typeToString));

		// 終了状態
		List<EndStatusEnum> optsEndStatus = new ArrayList<>(Arrays.asList(
				EndStatusEnum.NORMAL,
				EndStatusEnum.ABNORMAL,
				EndStatusEnum.WARNING));

		property.addChildren(new PropertyBuilder(HistoryFilterPropertyConstant.END_STATUS, "end.status")
				.buildSelect(cnd.getEndStatus(), optsEndStatus, EndStatusMessage::typeToString));

		// 実行契機
		List<JobTriggerTypeEnum> optsTriggerType = new ArrayList<>(Arrays.asList(JobTriggerTypeEnum.values()));
		optsTriggerType.remove(JobTriggerTypeEnum.UNKOWN); // 不要

		Property trigger = new PropertyBuilder(HistoryFilterPropertyConstant.TRIGGER, "trigger").build();
		trigger.addChildren(new PropertyBuilder(HistoryFilterPropertyConstant.TRIGGER_TYPE, "trigger.type")
				.buildSelect(cnd.getTriggerType(), optsTriggerType, JobTriggerTypeMessage::typeToString));
		trigger.addChildren(new PropertyBuilder(HistoryFilterPropertyConstant.TRIGGER_INFO, "trigger.info")
				.buildText(cnd.getTriggerInfo(), DataRangeConstant.VARCHAR_128));

		property.addChildren(trigger);

		// オーナーロールID
		property.addChildren(new PropertyBuilder(HistoryFilterPropertyConstant.OWNER_ROLE_ID, "owner.role.id")
				.setCopiable(true).buildMultipleTexts(cnd.getOwnerRoleId(), SEPARATOR, DataRangeConstant.VARCHAR_64));

		return property;
	}

	/**
	 * {@link Property} をフィルタ詳細条件へ変換して返します。
	 * 
	 * @param property フィルタ詳細条件の入力値を保持するプロパティ。
	 * @return フィルタ詳細条件を保持したDTO。
	 */
	public static JobHistoryFilterConditionRequest convertPropertyToCondition(Property property) {
		JobHistoryFilterConditionRequest cnd = new JobHistoryFilterConditionRequest();

		PropertyWrapper p = new PropertyWrapper(property);
		String SEPARATOR = FilterConstant.AND_SEPARATOR;

		// 開始・再実行日時
		cnd.setStartDateFrom(p.findTimeString(HistoryFilterPropertyConstant.START_FROM_DATE));
		cnd.setStartDateTo(p.findTimeString(HistoryFilterPropertyConstant.START_TO_DATE));

		// 終了・中断日時
		cnd.setEndDateFrom(p.findTimeString(HistoryFilterPropertyConstant.END_FROM_DATE));
		cnd.setEndDateTo(p.findTimeString(HistoryFilterPropertyConstant.END_TO_DATE));

		// セッションID
		cnd.setSessionId(p.joinNonEmptyStrings(HistoryFilterPropertyConstant.SESSION_ID, SEPARATOR));

		// ジョブID
		List<String> jobIds = new ArrayList<>();
		for (JobTreeItemWrapper v : p.findValue(HistoryFilterPropertyConstant.JOB_ID, JobTreeItemWrapper.class)) {
			jobIds.add(v.getData().getId());
		}
		jobIds.addAll(p.findNonEmptyStrings(HistoryFilterPropertyConstant.JOB_ID));

		cnd.setJobId(jobIds.size() == 0 ? null : String.join(SEPARATOR, jobIds));

		// 実行状態
		cnd.setStatus(p.findEnum(HistoryFilterPropertyConstant.STATUS,
				StatusMessage::stringToType,
				StatusEnum.class,
				org.openapitools.client.model.JobHistoryFilterConditionRequest.StatusEnum.class));

		// 終了状態
		cnd.setEndStatus(p.findEnum(HistoryFilterPropertyConstant.END_STATUS,
				EndStatusMessage::stringToType,
				EndStatusEnum.class,
				org.openapitools.client.model.JobHistoryFilterConditionRequest.EndStatusEnum.class));

		// 実行契機
		cnd.setTriggerType(p.findEnum(HistoryFilterPropertyConstant.TRIGGER_TYPE,
				JobTriggerTypeMessage::stringToType,
				JobTriggerTypeEnum.class,
				TriggerTypeEnum.class));

		cnd.setTriggerInfo(p.joinNonEmptyStrings(HistoryFilterPropertyConstant.TRIGGER_INFO, SEPARATOR));

		// オーナーロールID
		cnd.setOwnerRoleId(p.joinNonEmptyStrings(HistoryFilterPropertyConstant.OWNER_ROLE_ID, SEPARATOR));

		return cnd;
	}

}
