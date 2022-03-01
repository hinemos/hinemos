/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.ArrayList;
import java.util.HashMap;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.util.RestItemNameResolver;
import com.clustercontrol.util.MessageConstant;

public abstract class AbstractJobLinkRcvRequest implements RequestDto {

	/** 実行契機名 */
	@RestItemName(value = MessageConstant.JOBKICK_NAME)
	private String name;

	/** ジョブID */
	@RestItemName(value = MessageConstant.JOB_ID)
	private String jobId;

	/** ジョブユニットID */
	@RestItemName(value = MessageConstant.JOBUNIT_ID)
	private String jobunitId;

	/** 有効/無効 */
	private Boolean valid = Boolean.FALSE;

	/** カレンダID */
	@RestItemName(value = MessageConstant.CALENDAR_ID)
	private String calendarId;

	/** ランタイムジョブ変数情報 */
	private ArrayList<JobRuntimeParamRequest> jobRuntimeParamList;

	/** 送信元ファシリティID */
	@RestItemName(value = MessageConstant.SCOPE)
	@RestValidateString(notNull = false)
	private String facilityId;

	/** ジョブ連携メッセージID */
	@RestItemName(value = MessageConstant.JOBLINK_MESSAGE_ID)
	@RestValidateString(notNull = false, minLen = 1, maxLen = 512)
	private String joblinkMessageId;

	/** 重要度（情報） */
	private Boolean infoValidFlg = Boolean.FALSE;

	/** 重要度（警告） */
	private Boolean warnValidFlg = Boolean.FALSE;

	/** 重要度（危険） */
	private Boolean criticalValidFlg = Boolean.FALSE;

	/** 重要度（不明） */
	private Boolean unknownValidFlg = Boolean.FALSE;

	/** アプリケーションフラグ */
	private Boolean applicationFlg = Boolean.FALSE;

	/** アプリケーション */
	@RestItemName(value = MessageConstant.APPLICATION)
	@RestValidateString(maxLen = 64)
	private String application;

	/** 監視詳細フラグ */
	private Boolean monitorDetailIdFlg = Boolean.FALSE;

	/** 監視詳細 */
	@RestItemName(value = MessageConstant.MONITOR_DETAIL_ID)
	@RestValidateString(maxLen = 1024)
	private String monitorDetailId;

	/** メッセージフラグ */
	private Boolean messageFlg = Boolean.FALSE;

	/** メッセージ */
	@RestItemName(value = MessageConstant.MESSAGE)
	@RestValidateString(maxLen = 4096)
	private String message;

	/** 拡張情報フラグ */
	private Boolean expFlg = Boolean.FALSE;

	/** ジョブ連携メッセージの拡張情報設定 */
	@RestItemName(value = MessageConstant.EXTENDED_INFO)
	private ArrayList<JobLinkExpInfoRequest> jobLinkExpList;

	public AbstractJobLinkRcvRequest() {
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean isValid() {
		return valid;
	}

	public void setValid(Boolean valid) {
		this.valid = valid;
	}

	public String getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	public String getJobunitId() {
		return jobunitId;
	}

	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}

	public ArrayList<JobRuntimeParamRequest> getJobRuntimeParamList() {
		return jobRuntimeParamList;
	}

	public void setJobRuntimeParamList(ArrayList<JobRuntimeParamRequest> jobRuntimeParamList) {
		this.jobRuntimeParamList = jobRuntimeParamList;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getJoblinkMessageId() {
		return joblinkMessageId;
	}

	public void setJoblinkMessageId(String joblinkMessageId) {
		this.joblinkMessageId = joblinkMessageId;
	}

	public Boolean getInfoValidFlg() {
		return infoValidFlg;
	}

	public void setInfoValidFlg(Boolean infoValidFlg) {
		this.infoValidFlg = infoValidFlg;
	}

	public Boolean getWarnValidFlg() {
		return warnValidFlg;
	}

	public void setWarnValidFlg(Boolean warnValidFlg) {
		this.warnValidFlg = warnValidFlg;
	}

	public Boolean getCriticalValidFlg() {
		return criticalValidFlg;
	}

	public void setCriticalValidFlg(Boolean criticalValidFlg) {
		this.criticalValidFlg = criticalValidFlg;
	}

	public Boolean getUnknownValidFlg() {
		return unknownValidFlg;
	}

	public void setUnknownValidFlg(Boolean unknownValidFlg) {
		this.unknownValidFlg = unknownValidFlg;
	}

	public Boolean getApplicationFlg() {
		return applicationFlg;
	}

	public void setApplicationFlg(Boolean applicationFlg) {
		this.applicationFlg = applicationFlg;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public Boolean getMonitorDetailIdFlg() {
		return monitorDetailIdFlg;
	}

	public void setMonitorDetailIdFlg(Boolean monitorDetailIdFlg) {
		this.monitorDetailIdFlg = monitorDetailIdFlg;
	}

	public String getMonitorDetailId() {
		return monitorDetailId;
	}

	public void setMonitorDetailId(String monitorDetailId) {
		this.monitorDetailId = monitorDetailId;
	}

	public Boolean getMessageFlg() {
		return messageFlg;
	}

	public void setMessageFlg(Boolean messageFlg) {
		this.messageFlg = messageFlg;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Boolean getExpFlg() {
		return expFlg;
	}

	public void setExpFlg(Boolean expFlg) {
		this.expFlg = expFlg;
	}

	public ArrayList<JobLinkExpInfoRequest> getJobLinkExpList() {
		return jobLinkExpList;
	}

	public void setJobLinkExpList(ArrayList<JobLinkExpInfoRequest> jobLinkExpList) {
		this.jobLinkExpList = jobLinkExpList;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {

		// 重要度のいずれかが選択されていなければエラー
		if (!infoValidFlg && !warnValidFlg && !criticalValidFlg && !unknownValidFlg) {
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SELECT_ONE_OR_MORE
					.getMessage(MessageConstant.PRIORITY.getMessage()));
		}
		// [アプリケーションフラグ]がtrueの場合、[アプリケーション]必須
		if (applicationFlg && (application == null || application.isEmpty())) {
			String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "application");
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
		}

		// [監視詳細フラグ]がtrueの場合、[監視詳細]必須
		if (monitorDetailIdFlg && (monitorDetailId == null || monitorDetailId.isEmpty())) {
			String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "monitorDetailId");
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
		}

		// [メッセージフラグ]がtrueの場合、[メッセージ]必須
		if (messageFlg && (message == null || message.isEmpty())) {
			String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "message");
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
		}

		// [拡張情報フラグ]がtrueの場合、[拡張情報]必須
		if (expFlg && (jobLinkExpList == null || jobLinkExpList.isEmpty())) {
			String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "jobLinkExpList");
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
		}

		if (jobLinkExpList != null && !jobLinkExpList.isEmpty()) {
			HashMap<String, String> expMap = new HashMap<>();
			for (JobLinkExpInfoRequest exp : jobLinkExpList) {
				// [拡張情報]チェック
				exp.correlationCheck();

				// [拡張情報]でキーと値が重複して存在する場合エラー
				if (expMap.containsKey(exp.getKey()) && expMap.containsValue(exp.getValue())) {
					String[] r1 = { RestItemNameResolver.resolveItenName(this.getClass(), "jobLinkExpList"),
							String.format("%s,%s", exp.getKey(), exp.getValue()) };
					throw new InvalidSetting(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(r1));
				}
				expMap.put(exp.getKey(), exp.getValue());
			}
		}

	}
}
