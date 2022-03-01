/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.ArrayList;
import java.util.HashSet;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.PriorityRequiredEnum;
import com.clustercontrol.rest.util.RestItemNameResolver;
import com.clustercontrol.util.MessageConstant;

public class SendJobLinkMessageManualRequest implements RequestDto {

	/** ジョブ連携送信設定ID */
	@RestItemName(value = MessageConstant.JOBLINK_SEND_SETTING_ID)
	@RestValidateString(notNull = true)
	private String joblinkSendSettingId;

	/** ジョブ連携メッセージID */
	@RestItemName(value = MessageConstant.JOBLINK_MESSAGE_ID)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 512, type = CheckType.ID)
	private String joblinkMessageId;

	/** 重要度 */
	@RestItemName(value = MessageConstant.PRIORITY)
	@RestBeanConvertEnum
	@RestValidateObject(notNull = true)
	private PriorityRequiredEnum priority;

	/** メッセージ */
	@RestItemName(value = MessageConstant.MESSAGE)
	@RestValidateString(maxLen = 4096)
	private String message;

	// 監視詳細
	@RestItemName(value = MessageConstant.MONITOR_DETAIL_ID)
	@RestValidateString(maxLen = 1024)
	private String monitorDetailId;

	// アプリケーション
	@RestItemName(value = MessageConstant.APPLICATION)
	@RestValidateString(maxLen = 64)
	private String application;

	/** ジョブ連携メッセージの拡張情報設定 */
	@RestItemName(value = MessageConstant.JOB_LINK_MESSAGE_EXPANSION_INFO)
	private ArrayList<JobLinkExpInfoRequest> jobLinkExpList;

	public SendJobLinkMessageManualRequest() {
	}

	public String getJoblinkSendSettingId() {
		return joblinkSendSettingId;
	}

	public void setJoblinkSendSettingId(String joblinkSendSettingId) {
		this.joblinkSendSettingId = joblinkSendSettingId;
	}

	public String getJoblinkMessageId() {
		return joblinkMessageId;
	}

	public void setJoblinkMessageId(String joblinkMessageId) {
		this.joblinkMessageId = joblinkMessageId;
	}

	public PriorityRequiredEnum getPriority() {
		return priority;
	}

	public void setPriority(PriorityRequiredEnum priority) {
		this.priority = priority;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMonitorDetailId() {
		return monitorDetailId;
	}

	public void setMonitorDetailId(String monitorDetailId) {
		this.monitorDetailId = monitorDetailId;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public ArrayList<JobLinkExpInfoRequest> getJobLinkExpList() {
		return jobLinkExpList;
	}

	public void setJobLinkExpList(ArrayList<JobLinkExpInfoRequest> jobLinkExpList) {
		this.jobLinkExpList = jobLinkExpList;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		if (jobLinkExpList != null && !jobLinkExpList.isEmpty()) {
			HashSet<String> expSet = new HashSet<String>();
			for (JobLinkExpInfoRequest exp : jobLinkExpList) {
				// [拡張情報]チェック
				exp.correlationCheck();

				// [拡張情報]でキーが重複して存在する場合エラー
				if (expSet.contains(exp.getKey())) {
					String[] r1 = { RestItemNameResolver.resolveItenName(this.getClass(), "jobLinkExpList"),
							String.format("%s", exp.getKey()) };
					throw new InvalidSetting(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(r1));
				}
				expSet.add(exp.getKey());
			}
		}
	}
}
