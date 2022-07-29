/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.annotation.EnumerateConstant;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.deserializer.EnumToConstantDeserializer;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.EndStatusSelectEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.OperationFailureEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.PrioritySelectEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ProcessingMethodEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.serializer.ConstantToEnumSerializer;
import com.clustercontrol.rest.util.RestItemNameResolver;
import com.clustercontrol.util.MessageConstant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * ジョブ連携送信ジョブに関する情報を保持するクラス
 *
 */
/* 
 * 本クラスのRestXXアノテーション、correlationCheckを修正する場合は、Requestクラスも同様に修正すること。
 * (ジョブユニットの登録/更新はInfoクラス、ジョブ単位の登録/更新の際はRequestクラスが使用される。)
 * refs #13882
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobLinkSendInfo implements Serializable, RequestDto {

	/** シリアライズ可能クラスに定義するUID */
	@JsonIgnore
	private static final long serialVersionUID = 7216413607782857671L;

	@JsonIgnore
	private static Log m_log = LogFactory.getLog( JobLinkSendInfo.class );

	/** 送信に失敗した場合再送する */
	private Boolean retryFlg;

	/** 再送回数 */
	@RestItemName(value=MessageConstant.RESEND_COUNT)
	@RestValidateInteger(minVal = 0, maxVal = 32767)
	private Integer retryCount;

	/** 送信失敗時の操作 */
	@RestItemName(value=MessageConstant.SEND_FAILURE_OPERATION)
	@RestValidateInteger(notNull = true)
	@JsonDeserialize(using=EnumToConstantDeserializer.class)
	@JsonSerialize(using=ConstantToEnumSerializer.class)
	@EnumerateConstant(enumDto=OperationFailureEnum.class)
	private Integer failureOperation;

	/** 送信失敗時の終了状態 */
	@RestItemName(value=MessageConstant.END_STATUS_SEND_FAILURE)
	@JsonDeserialize(using=EnumToConstantDeserializer.class)
	@JsonSerialize(using=ConstantToEnumSerializer.class)
	@EnumerateConstant(enumDto=EndStatusSelectEnum.class)
	private Integer failureEndStatus;

	/** 送信失敗時の終了値 */
	@RestItemName(value=MessageConstant.END_VALUE_SEND_FAILURE)
	@RestValidateInteger(minVal = -32768, maxVal = 32767)
	private Integer failureEndValue;

	/** 送信成功時の終了値 */
	@RestItemName(value=MessageConstant.END_VALUE_SEND_SUCCESS)
	@RestValidateInteger(notNull = true, minVal = -32768, maxVal = 32767)
	private Integer successEndValue;

	/** ジョブ連携メッセージID */
	@RestItemName(value=MessageConstant.JOBLINK_MESSAGE_ID)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 508, type = CheckType.ID)
	private String joblinkMessageId;

	/** 重要度 */
	@RestItemName(value=MessageConstant.PRIORITY)
	@RestValidateInteger(notNull = true)
	@JsonDeserialize(using=EnumToConstantDeserializer.class)
	@JsonSerialize(using=ConstantToEnumSerializer.class)
	@EnumerateConstant(enumDto=PrioritySelectEnum.class)
	private Integer priority;

	/** メッセージ */
	@RestItemName(value=MessageConstant.MESSAGE)
	@RestValidateString(maxLen = 4096)
	private String message;

	/** ジョブ連携送信設定ID */
	@RestItemName(value=MessageConstant.JOBLINK_SEND_SETTING_ID)
	@RestValidateString(notNull = true)
	private String joblinkSendSettingId;

	/** ファシリティID */
	private String facilityID;

	/** スコープ */
	private String scope;

	/** スコープ処理 */
	@JsonSerialize(using=ConstantToEnumSerializer.class)
	@EnumerateConstant(enumDto=ProcessingMethodEnum.class)
	private Integer processingMethod = ProcessingMethodConstant.TYPE_ALL_NODE;

	/** 送信先プロトコル */
	private String protocol;

	/** 送信先ポート */
	private Integer port;

	/** HinemosユーザID */
	private String hinemosUserId;

	/** Hinemosパスワード */
	private String hinemosPassword;

	/** HTTP Proxyを使用する */
	private Boolean proxyFlg;

	/** HTTP Proxyホスト */
	private String proxyHost;

	/** HTTP Proxyポート */
	private Integer proxyPort; 

	/** HTTP Proxyユーザ */
	private String proxyUser;

	/** HTTP Proxyパスワード */
	private String proxyPassword;

	/** ジョブ連携メッセージの拡張情報設定 */
	@RestItemName(value = MessageConstant.EXTENDED_INFO)
	private ArrayList<JobLinkExpInfo> jobLinkExpList;

	public Boolean getRetryFlg() {
		return retryFlg;
	}

	public void setRetryFlg(Boolean retryFlg) {
		this.retryFlg = retryFlg;
	}

	public Integer getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(Integer retryCount) {
		this.retryCount = retryCount;
	}

	public Integer getFailureOperation() {
		return failureOperation;
	}

	public void setFailureOperation(Integer failureOperation) {
		this.failureOperation = failureOperation;
	}

	public String getJoblinkMessageId() {
		return joblinkMessageId;
	}

	public void setJoblinkMessageId(String joblinkMessageId) {
		this.joblinkMessageId = joblinkMessageId;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getFacilityID() {
		return facilityID;
	}

	public void setFacilityID(String facilityID) {
		this.facilityID = facilityID;
	}

	public Integer getProcessingMethod() {
		return processingMethod;
	}

	public void setProcessingMethod(Integer processingMethod) {
		this.processingMethod = processingMethod;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Integer getSuccessEndValue() {
		return successEndValue;
	}

	public void setSuccessEndValue(Integer successEndValue) {
		this.successEndValue = successEndValue;
	}

	public Integer getFailureEndValue() {
		return failureEndValue;
	}

	public void setFailureEndValue(Integer failureEndValue) {
		this.failureEndValue = failureEndValue;
	}

	public ArrayList<JobLinkExpInfo> getJobLinkExpList() {
		return jobLinkExpList;
	}

	public void setJobLinkExpList(ArrayList<JobLinkExpInfo> jobLinkExpList) {
		this.jobLinkExpList = jobLinkExpList;
	}

	public String getJoblinkSendSettingId() {
		return joblinkSendSettingId;
	}

	public void setJoblinkSendSettingId(String joblinkSendSettingId) {
		this.joblinkSendSettingId = joblinkSendSettingId;
	}

	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}

	public String getHinemosUserId() {
		return hinemosUserId;
	}
	public void setHinemosUserId(String hinemosUserId) {
		this.hinemosUserId = hinemosUserId;
	}

	public String getHinemosPassword() {
		return hinemosPassword;
	}
	public void setHinemosPassword(String hinemosPassword) {
		this.hinemosPassword = hinemosPassword;
	}

	public Boolean getProxyFlg() {
		return proxyFlg;
	}
	public void setProxyFlg(Boolean proxyFlg) {
		this.proxyFlg = proxyFlg;
	}

	public String getProxyHost() {
		return proxyHost;
	}
	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public Integer getProxyPort() {
		return proxyPort;
	}
	public void setProxyPort(Integer proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getProxyUser() {
		return proxyUser;
	}
	public void setProxyUser(String proxyUser) {
		this.proxyUser = proxyUser;
	}

	public String getProxyPassword() {
		return proxyPassword;
	}
	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}

	public Integer getFailureEndStatus() {
		return failureEndStatus;
	}

	public void setFailureEndStatus(Integer failureEndStatus) {
		this.failureEndStatus = failureEndStatus;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((failureEndValue == null) ? 0 : failureEndValue.hashCode());
		result = prime * result + ((failureEndStatus == null) ? 0 : failureEndStatus.hashCode());
		result = prime * result + ((failureOperation == null) ? 0 : failureOperation.hashCode());
		result = prime * result + ((jobLinkExpList == null) ? 0 : jobLinkExpList.hashCode());
		result = prime * result + ((joblinkMessageId == null) ? 0 : joblinkMessageId.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((priority == null) ? 0 : priority.hashCode());
		result = prime * result + ((retryCount == null) ? 0 : retryCount.hashCode());
		result = prime * result + ((retryFlg == null) ? 0 : retryFlg.hashCode());
		result = prime * result + ((successEndValue == null) ? 0 : successEndValue.hashCode());
		result = prime * result + ((joblinkSendSettingId == null) ? 0 : joblinkSendSettingId.hashCode());
		
		
		result = prime * result + ((facilityID == null) ? 0 : facilityID.hashCode());
		result = prime * result + ((processingMethod == null) ? 0 : processingMethod.hashCode());
		result = prime * result + ((protocol == null) ? 0 : protocol.hashCode());
		result = prime * result + ((port == null) ? 0 : port.hashCode());
		result = prime * result + ((hinemosUserId == null) ? 0 : hinemosUserId.hashCode());
		result = prime * result + ((hinemosPassword == null) ? 0 : hinemosPassword.hashCode());
		result = prime * result + ((proxyFlg == null) ? 0 : proxyFlg.hashCode());
		result = prime * result + ((proxyHost == null) ? 0 : proxyHost.hashCode());
		result = prime * result + ((proxyPort == null) ? 0 : proxyPort.hashCode());
		result = prime * result + ((proxyUser == null) ? 0 : proxyUser.hashCode());
		result = prime * result + ((proxyPassword == null) ? 0 : proxyPassword.hashCode());

		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof JobLinkSendInfo)) {
			return false;
		}
		JobLinkSendInfo o1 = this;
		JobLinkSendInfo o2 = (JobLinkSendInfo) obj;

		boolean ret = false;
		ret = 	equalsSub(o1.getFailureEndValue(), o2.getFailureEndValue()) &&
				equalsSub(o1.getFailureEndStatus(), o2.getFailureEndStatus()) &&
				equalsSub(o1.getFailureOperation(), o2.getFailureOperation()) &&
				equalsSub(o1.getMessage(), o2.getMessage()) &&
				equalsSub(o1.getJoblinkMessageId(), o2.getJoblinkMessageId()) &&
				equalsSub(o1.getPriority(), o2.getPriority()) &&
				equalsSub(o1.getRetryCount(), o2.getRetryCount()) &&
				equalsSub(o1.getRetryFlg(), o2.getRetryFlg()) &&
				equalsSub(o1.getSuccessEndValue(), o2.getSuccessEndValue()) &&
				equalsSub(o1.getJoblinkSendSettingId(), o2.getJoblinkSendSettingId()) &&
				equalsSub(o1.getFacilityID(), o2.getFacilityID()) &&
				equalsSub(o1.getProcessingMethod(), o2.getProcessingMethod()) &&
				equalsSub(o1.getProtocol(), o2.getProtocol()) &&
				equalsSub(o1.getPort(), o2.getPort()) &&
				equalsSub(o1.getHinemosUserId(), o2.getHinemosUserId()) &&
				equalsSub(o1.getHinemosPassword(), o2.getHinemosPassword()) &&
				equalsSub(o1.getProxyFlg(), o2.getProxyFlg()) &&
				equalsSub(o1.getProxyHost(), o2.getProxyHost()) &&
				equalsSub(o1.getProxyPort(), o2.getProxyPort()) &&
				equalsSub(o1.getProxyUser(), o2.getProxyUser()) &&
				equalsSub(o1.getProxyPassword(), o2.getProxyPassword()) &&
				equalsArray(o1.getJobLinkExpList(), o2.getJobLinkExpList());
		return ret;
	}

	private boolean equalsSub(Object o1, Object o2) {
		if (o1 == o2) {
			return true;
		}
		if (o1 == null) {
			return false;
		}
		boolean ret = o1.equals(o2);
		if (!ret) {
			if (m_log.isTraceEnabled()) {
				m_log.trace("equalsSub : " + o1 + "!=" + o2);
			}
		}
		return ret;
	}

	private boolean equalsArray(ArrayList<?> list1, ArrayList<?> list2) {
		if (list1 != null && !list1.isEmpty()) {
			if (list2 != null && list1.size() == list2.size()) {
				Object[] ary1 = list1.toArray();
				Object[] ary2 = list2.toArray();
				Arrays.sort(ary1);
				Arrays.sort(ary2);

				for (int i = 0; i < ary1.length; i++) {
					if (!ary1[i].equals(ary2[i])) {
						if (m_log.isTraceEnabled()) {
							m_log.trace("equalsArray : " + ary1[i] + "!=" + ary2[i]);
						}
						return false;
					}
				}
				return true;
			}
		} else if (list2 == null || list2.isEmpty()) {
			return true;
		}
		return false;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		// [送信に失敗した場合に再送する]がtrueの場合、[再送回数]必須
		if (retryFlg && retryCount == null) {
			String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "retryCount");
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
		}

		// [送信失敗時の操作]が「停止[状態指定]」の場合、[終了状態][終了値]必須
		if (failureOperation.equals(OperationFailureEnum.STOP_SET_END_VALUE.getCode())) {
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
			for (JobLinkExpInfo exp : jobLinkExpList) {
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