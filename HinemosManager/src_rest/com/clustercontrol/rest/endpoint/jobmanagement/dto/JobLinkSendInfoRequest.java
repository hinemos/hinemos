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
import com.clustercontrol.jobmanagement.bean.JobLinkConstant;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.EndStatusSelectEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.OperationFailureEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.PriorityRequiredEnum;
import com.clustercontrol.rest.util.RestItemNameResolver;
import com.clustercontrol.util.MessageConstant;

/* 
 * 本クラスのRestXXアノテーション、correlationCheckを修正する場合は、Infoクラスも同様に修正すること。
 * (ジョブユニットの登録/更新はInfoクラス、ジョブ単位の登録/更新の際はRequestクラスが使用される。)
 * refs #13882
 */
public class JobLinkSendInfoRequest implements RequestDto {

	/** 送信に失敗した場合に再送する */
	@RestItemName(value=MessageConstant.JOBLINK_SEND_FAILURE_RETRY)
	private Boolean retryFlg = Boolean.FALSE;

	/** 再送回数 */
	@RestItemName(value=MessageConstant.RESEND_COUNT)
	@RestValidateInteger(minVal = 0, maxVal = 32767)
	private Integer retryCount;

	/** 送信失敗時の操作 */
	@RestItemName(value=MessageConstant.SEND_FAILURE_OPERATION)
	@RestBeanConvertEnum
	@RestValidateObject(notNull = true)
	private OperationFailureEnum failureOperation;

	/** 送信失敗時の終了状態 */
	@RestItemName(value=MessageConstant.END_STATUS_SEND_FAILURE)
	@RestBeanConvertEnum
	private EndStatusSelectEnum failureEndStatus;

	/** ジョブ連携メッセージID */
	@RestItemName(value=MessageConstant.JOBLINK_MESSAGE_ID)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 508, type = CheckType.ID)
	private String joblinkMessageId;

	/** 重要度 */
	@RestItemName(value=MessageConstant.PRIORITY)
	@RestBeanConvertEnum
	@RestValidateObject(notNull = true)
	private PriorityRequiredEnum priority;

	/** メッセージ */
	@RestItemName(value=MessageConstant.MESSAGE)
	@RestValidateString(maxLen = 4096)
	private String message;

	/** 終了値（送信成功） */
	@RestItemName(value=MessageConstant.END_VALUE_SEND_SUCCESS)
	@RestValidateInteger(notNull = true, minVal = -32768, maxVal = 32767)
	private Integer successEndValue;

	/** 終了値（送信失敗） */
	@RestItemName(value=MessageConstant.END_VALUE_SEND_FAILURE)
	@RestValidateInteger(minVal = -32768, maxVal = 32767)
	private Integer failureEndValue;

	/** ジョブ連携送信設定ID */
	@RestItemName(value=MessageConstant.JOBLINK_SEND_SETTING_ID)
	@RestValidateString(notNull = true)
	private String joblinkSendSettingId;

	/** ジョブ連携メッセージの拡張情報設定 */
	@RestItemName(value = MessageConstant.EXTENDED_INFO)
	private ArrayList<JobLinkExpInfoRequest> jobLinkExpList;

	/**
	 * 送信に失敗した場合再送するを返す。<BR>
	 * @return
	 */
	public Boolean getRetryFlg() {
		return retryFlg;
	}

	/**
	 * 送信に失敗した場合再送するを設定する。<BR>
	 * @param retryFlg
	 */
	public void setRetryFlg(Boolean retryFlg) {
		this.retryFlg = retryFlg;
	}

	/**
	 * 再送回数を返す。<BR>
	 * @return
	 */
	public Integer getRetryCount() {
		return retryCount;
	}

	/**
	 * 再送回数を設定する。<BR>
	 * @param retryCount
	 */
	public void setRetryCount(Integer retryCount) {
		this.retryCount = retryCount;
	}

	/**
	 * 送信失敗時の操作を返す。<BR>
	 * @return
	 */
	public OperationFailureEnum getFailureOperation() {
		return failureOperation;
	}

	/**
	 * 送信失敗時の操作を設定する。<BR>
	 * @param failureOperation
	 */
	public void setFailureOperation(OperationFailureEnum failureOperation) {
		this.failureOperation = failureOperation;
	}

	/**
	 * 送信失敗時の終了状態を返す。<BR>
	 * @return
	 */
	public EndStatusSelectEnum getFailureEndStatus() {
		return failureEndStatus;
	}

	/**
	 * 送信失敗時の終了状態を設定する。<BR>
	 * @param failureOperation
	 */
	public void setFailureEndStatus(EndStatusSelectEnum failureEndStatus) {
		this.failureEndStatus = failureEndStatus;
	}

	/**
	 * ジョブ連携メッセージIDを返す。<BR>
	 * @return
	 */
	public String getJoblinkMessageId() {
		return joblinkMessageId;
	}

	/**
	 * ジョブ連携メッセージIDを設定する。<BR>
	 * @param joblinkMessageId
	 */
	public void setJoblinkMessageId(String joblinkMessageId) {
		this.joblinkMessageId = joblinkMessageId;
	}

	/**
	 * 重要度を返す。<BR>
	 * @return
	 */
	public PriorityRequiredEnum getPriority() {
		return priority;
	}

	/**
	 * 重要度を設定する。<BR>
	 * @param priority
	 */
	public void setPriority(PriorityRequiredEnum priority) {
		this.priority = priority;
	}

	/**
	 * メッセージを返す。<BR>
	 * @return
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * メッセージを設定する。<BR>
	 * @param message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * 終了値（送信成功）を返す。<BR>
	 * @return
	 */
	public Integer getSuccessEndValue() {
		return successEndValue;
	}

	/**
	 * 終了値（送信成功）を設定する。<BR>
	 * @param successEndValue
	 */
	public void setSuccessEndValue(Integer successEndValue) {
		this.successEndValue = successEndValue;
	}

	/**
	 * 終了値（送信失敗）を返す。<BR>
	 * @return
	 */
	public Integer getFailureEndValue() {
		return failureEndValue;
	}

	/**
	 * 終了値（送信失敗）を設定する。<BR>
	 * @param failureEndValue
	 */
	public void setFailureEndValue(Integer failureEndValue) {
		this.failureEndValue = failureEndValue;
	}

	/**
	 * ジョブ連携送信設定IDを返す。<BR>
	 * @return
	 */
	public String getJoblinkSendSettingId() {
		return joblinkSendSettingId;
	}

	/**
	 * ジョブ連携送信設定IDを設定する。<BR>
	 * @param joblinkSendSettingId
	 */
	public void setJoblinkSendSettingId(String joblinkSendSettingId) {
		this.joblinkSendSettingId = joblinkSendSettingId;
	}

	/**
	 * ジョブ連携メッセージの拡張情報設定を返す。<BR>
	 * @return
	 */
	public ArrayList<JobLinkExpInfoRequest> getJobLinkExpList() {
		return jobLinkExpList;
	}

	/**
	 * ジョブ連携メッセージの拡張情報設定を設定する。<BR>
	 * @param jobLinkJobExpList
	 */
	public void setJobLinkExpList(ArrayList<JobLinkExpInfoRequest> jobLinkExpList) {
		this.jobLinkExpList = jobLinkExpList;
	}


	@Override
	public void correlationCheck() throws InvalidSetting {

		// [送信に失敗した場合に再送する]がNullの場合はエラー
		if (retryFlg == null) {
			String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "retryFlg");
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
		}

		// [送信に失敗した場合に再送する]がtrueの場合、[再送回数]必須
		if (retryFlg && retryCount == null) {
			String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "retryCount");
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
		}

		// [送信失敗時の操作]が「停止[状態指定]」の場合、[終了状態][終了値]必須
		if (failureOperation == OperationFailureEnum.STOP_SET_END_VALUE) {
			if (failureEndStatus == null) {
				String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "failureEndStatus");
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
			}
			if (failureEndValue == null) {
				String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "failureEndValue");
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
			}
		}

		if (jobLinkExpList != null && !jobLinkExpList.isEmpty()) {
			// [拡張情報]が最大件数を超えて指定されている場合はエラー
			if (jobLinkExpList.size() > JobLinkConstant.EXP_INFO_MAX_COUNT) {
				String[] r1 = {RestItemNameResolver.resolveItenName(this.getClass(), "jobLinkExpList"),
						Integer.toString(JobLinkConstant.EXP_INFO_MAX_COUNT)};
				throw new InvalidSetting(MessageConstant.MESSAGE_ERROR_IN_EXCEEDED.getMessage(r1));
			}

			HashSet<String> exps = new HashSet<>();
			for (JobLinkExpInfoRequest exp : jobLinkExpList) {
				// [拡張情報]チェック
				exp.correlationCheck();

				// [拡張情報]でキーが重複して存在する場合エラー
				if (exps.contains(exp.getKey())) {
					String[] r1 = {RestItemNameResolver.resolveItenName(this.getClass(), "jobLinkExpList"),
							exp.getKey()};
					throw new InvalidSetting(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(r1));
				}
				exps.add(exp.getKey());
			}
		}
	}

}
