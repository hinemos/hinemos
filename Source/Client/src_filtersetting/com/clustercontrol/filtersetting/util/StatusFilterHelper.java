/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.util;

import org.openapitools.client.model.StatusFilterBaseRequest;
import org.openapitools.client.model.StatusFilterBaseRequest.FacilityTargetEnum;
import org.openapitools.client.model.StatusFilterBaseResponse;
import org.openapitools.client.model.StatusFilterConditionRequest;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.monitor.bean.StatusFilterConstant;
import com.clustercontrol.monitor.util.MonitorResultRestClientWrapper;
import com.clustercontrol.util.FilterConstant;
import com.clustercontrol.util.PropertyBuilder;
import com.clustercontrol.util.PropertyWrapper;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.TimezoneUtil;

/**
 * ステータス通知結果フィルタ設定関連のヘルパーです。
 */
public class StatusFilterHelper {

	/**
	 * RequestDTO を複製して返します。
	 */
	public static StatusFilterBaseRequest duplicate(StatusFilterBaseRequest req) {
		StatusFilterBaseRequest req2 = new StatusFilterBaseRequest();
		try {
			RestClientBeanUtil.convertBean(req, req2);
		} catch (HinemosUnknown e) {
			// logic error
			throw new RuntimeException("Failed to duplicate StatusFilterBaseRequest.\n"
					+ "req=" + req.toString());
		}
		return req2;
	}

	/**
	 * RequestDTO から ResponseDTO へ変換して返します。
	 */
	public static StatusFilterBaseResponse convertToResponse(StatusFilterBaseRequest req) {
		StatusFilterBaseResponse response = new StatusFilterBaseResponse();
		try {
			RestClientBeanUtil.convertBean(req, response);
		} catch (HinemosUnknown e) {
			// logic error
			throw new RuntimeException("Failed to convert StatusFilterBaseRequest to StatusFilterBaseResponse.\n"
					+ "req=" + req.toString() + "\nres=" + response.toString());
		}
		return response;
	}

	/**
	 * ResponseDTO から RequestDTO へ変換して返します。
	 */
	public static StatusFilterBaseRequest convertToRequest(StatusFilterBaseResponse res) {
		StatusFilterBaseRequest request = new StatusFilterBaseRequest();
		try {
			RestClientBeanUtil.convertBean(res, request);
		} catch (HinemosUnknown e) {
			// logic error
			throw new RuntimeException("Failed to convert StatusFilterBaseResponse to StatusFilterBaseRequest.\n"
					+ "res=" + res.toString() + "\nreq=" + request.toString());
		}
		return request;
	}

	/**
	 * デフォルトのフィルタ条件(基本条件＋詳細条件1シート)を返します。
	 */
	public static StatusFilterBaseRequest createDefaultFilter(String facilityId) {
		StatusFilterBaseRequest filter = new StatusFilterBaseRequest();
		filter.setFacilityId(facilityId);
		filter.setFacilityTarget(FacilityTargetEnum.ALL);
		filter.addConditionsItem(createDefaultCondition());
		return filter;
	}

	/**
	 * デフォルトのフィルタ詳細条件(複数指定可能な条件)の1シート分を返します。
	 */
	public static StatusFilterConditionRequest createDefaultCondition() {
		StatusFilterConditionRequest o = new StatusFilterConditionRequest();
		o.setPriorityInfo(true);
		o.setPriorityWarning(true);
		o.setPriorityCritical(true);
		o.setPriorityUnknown(true);
		return o;
	}

	/**
	 * フィルタ詳細条件を {@link Property} へ変換して返します。
	 * 
	 * @param cnd フィルタ詳細条件DTO。
	 * @return
	 */
	public static Property convertConditionToProperty(StatusFilterConditionRequest cnd) {
		String SEPARATOR = FilterConstant.AND_SEPARATOR;

		Property property = new Property(null, null, "");
		property.removeChildren();

		// 重要度
		Property priority = new PropertyBuilder(StatusFilterConstant.PRIORITY, "priority").build();
		priority.addChildren(new PropertyBuilder(StatusFilterConstant.PRIORITY_CRITICAL, "critical").buildBool(cnd.getPriorityCritical()));
		priority.addChildren(new PropertyBuilder(StatusFilterConstant.PRIORITY_WARNING, "warning").buildBool(cnd.getPriorityWarning()));
		priority.addChildren(new PropertyBuilder(StatusFilterConstant.PRIORITY_INFO, "info").buildBool(cnd.getPriorityInfo()));
		priority.addChildren(new PropertyBuilder(StatusFilterConstant.PRIORITY_UNKNOWN, "unknown").buildBool(cnd.getPriorityUnknown()));

		property.addChildren(priority);

		// 最終変更日時
		Property outputDate = new PropertyBuilder(StatusFilterConstant.OUTPUT_DATE, "update.time").build();
		outputDate.addChildren(new PropertyBuilder(StatusFilterConstant.OUTPUT_FROM_DATE, "start").buildDateTime(cnd.getOutputDateFrom()));
		outputDate.addChildren(new PropertyBuilder(StatusFilterConstant.OUTPUT_TO_DATE, "end").buildDateTime(cnd.getOutputDateTo()));

		property.addChildren(outputDate);

		// 出力日時
		Property generationDate = new PropertyBuilder(StatusFilterConstant.GENERATION_DATE, "output.time").build();
		generationDate.addChildren(new PropertyBuilder(StatusFilterConstant.GENERATION_FROM_DATE, "start").buildDateTime(cnd.getGenerationDateFrom()));
		generationDate.addChildren(new PropertyBuilder(StatusFilterConstant.GENERATION_TO_DATE, "end").buildDateTime(cnd.getGenerationDateTo()));

		property.addChildren(generationDate);

		// 監視項目ID
		property.addChildren(new PropertyBuilder(StatusFilterConstant.MONITOR_ID, "monitor.id")
				.setCopiable(true).buildMultipleTexts(cnd.getMonitorId(), SEPARATOR, DataRangeConstant.VARCHAR_64));

		// 監視項目詳細
		property.addChildren(new PropertyBuilder(StatusFilterConstant.MONITOR_DETAIL_ID, "monitor.detail.id")
				.setCopiable(true).buildMultipleTexts(cnd.getMonitorDetail(), SEPARATOR, DataRangeConstant.VARCHAR_1024));

		// アプリケーション
		property.addChildren(new PropertyBuilder(StatusFilterConstant.APPLICATION, "application")
				.setCopiable(true).buildMultipleTexts(cnd.getApplication(), SEPARATOR, DataRangeConstant.VARCHAR_64));

		// メッセージ
		property.addChildren(new PropertyBuilder(StatusFilterConstant.MESSAGE, "message")
				.setCopiable(true).buildMultipleTexts(cnd.getMessage(), SEPARATOR, DataRangeConstant.VARCHAR_256));

		// オーナーロールID
		property.addChildren(new PropertyBuilder(StatusFilterConstant.OWNER_ROLE_ID, "owner.role.id")
				.setCopiable(true).buildMultipleTexts(cnd.getOwnerRoleId(), SEPARATOR, DataRangeConstant.VARCHAR_64));

		return property;
	}

	/**
	 * {@link Property} をフィルタ詳細条件へ変換して返します。
	 * 
	 * @param property フィルタ詳細条件の入力値を保持するプロパティ。
	 * @return フィルタ詳細条件を保持したDTO。
	 */
	public static StatusFilterConditionRequest convertPropertyToCondition(Property property) {
		StatusFilterConditionRequest cnd = new StatusFilterConditionRequest();

		PropertyWrapper p = new PropertyWrapper(property);
		p.setSimpleDateFormat(TimezoneUtil.getSimpleDateFormat(MonitorResultRestClientWrapper.DATETIME_FORMAT));
		String SEPARATOR = FilterConstant.AND_SEPARATOR;

		// 重要度
		cnd.setPriorityCritical(p.findBoolean(StatusFilterConstant.PRIORITY_CRITICAL));
		cnd.setPriorityWarning(p.findBoolean(StatusFilterConstant.PRIORITY_WARNING));
		cnd.setPriorityInfo(p.findBoolean(StatusFilterConstant.PRIORITY_INFO));
		cnd.setPriorityUnknown(p.findBoolean(StatusFilterConstant.PRIORITY_UNKNOWN));

		// 最終変更日時
		cnd.setOutputDateFrom(p.findTimeString(StatusFilterConstant.OUTPUT_FROM_DATE));
		cnd.setOutputDateTo(p.findTimeString(StatusFilterConstant.OUTPUT_TO_DATE));

		// 出力日時
		cnd.setGenerationDateFrom(p.findTimeString(StatusFilterConstant.GENERATION_FROM_DATE));
		cnd.setGenerationDateTo(p.findTimeString(StatusFilterConstant.GENERATION_TO_DATE));

		// 監視項目ID
		cnd.setMonitorId(p.joinNonEmptyStrings(StatusFilterConstant.MONITOR_ID, SEPARATOR));

		// 監視詳細
		cnd.setMonitorDetail(p.joinNonEmptyStrings(StatusFilterConstant.MONITOR_DETAIL_ID, SEPARATOR));

		// アプリケーション
		cnd.setApplication(p.joinNonEmptyStrings(StatusFilterConstant.APPLICATION, SEPARATOR));

		// メッセージ
		cnd.setMessage(p.joinNonEmptyStrings(StatusFilterConstant.MESSAGE, SEPARATOR));

		// オーナーロールID
		cnd.setOwnerRoleId(p.joinNonEmptyStrings(StatusFilterConstant.OWNER_ROLE_ID, SEPARATOR));

		return cnd;
	}

}
