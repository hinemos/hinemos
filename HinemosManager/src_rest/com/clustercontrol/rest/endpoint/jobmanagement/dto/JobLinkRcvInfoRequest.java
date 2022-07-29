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
import java.util.HashSet;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.util.RestItemNameResolver;
import com.clustercontrol.util.MessageConstant;

/* 
 * 本クラスのRestXXアノテーション、correlationCheckを修正する場合は、Infoクラスも同様に修正すること。
 * (ジョブユニットの登録/更新はInfoクラス、ジョブ単位の登録/更新の際はRequestクラスが使用される。)
 * refs #13882
 */
public class JobLinkRcvInfoRequest implements RequestDto {

	/** ファシリティID */
	@RestItemName(value = MessageConstant.SOURCE_SCOPE)
	@RestValidateString(notNull = true)
	private String facilityID;

	/** 終了値 - 「情報」 */
	@RestItemName(value = MessageConstant.END_VALUE_INFO)
	@RestValidateInteger(minVal = -32768, maxVal = 32767)
	private Integer monitorInfoEndValue;

	/** 終了値 - 「警告」 */
	@RestItemName(value = MessageConstant.END_VALUE_WARNING)
	@RestValidateInteger(minVal = -32768, maxVal = 32767)
	private Integer monitorWarnEndValue;

	/** 終了値 - 「危険」 */
	@RestItemName(value = MessageConstant.END_VALUE_CRITICAL)
	@RestValidateInteger(minVal = -32768, maxVal = 32767)
	private Integer monitorCriticalEndValue;

	/** 終了値 - 「不明」 */
	@RestItemName(value = MessageConstant.END_VALUE_UNKNOWN)
	@RestValidateInteger(minVal = -32768, maxVal = 32767)
	private Integer monitorUnknownEndValue;

	/** メッセージが得られなかった場合に終了する */
	private Boolean failureEndFlg = Boolean.FALSE;

	/** メッセージが得られなかった場合に終了する - タイムアウト */
	@RestItemName(value = MessageConstant.TIME_OUT)
	@RestValidateInteger(minVal = 0, maxVal = 32767)
	private Integer monitorWaitTime;

	/** メッセージが得られなかった場合に終了する - 終了値 */
	@RestItemName(value = MessageConstant.END_VALUE_JOBLINKRCV_FAILURE)
	@RestValidateInteger(minVal = -32768, maxVal = 32767)
	private Integer monitorWaitEndValue;

	/** ジョブ連携メッセージID */
	@RestItemName(value = MessageConstant.JOBLINK_MESSAGE_ID)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 512)
	private String joblinkMessageId;

	/** 確認期間フラグ */
	@RestItemName(value=MessageConstant.JOBLINK_CHECK_PAST_MESSAGE)
	private Boolean pastFlg = Boolean.FALSE;

	/** 確認期間（分） */
	@RestItemName(value = MessageConstant.TARGET_PERIOD)
	@RestValidateInteger(minVal = 0, maxVal = 32767)
	private Integer pastMin;

	/** 重要度（情報）有効/無効 */
	private Boolean infoValidFlg = Boolean.FALSE;

	/** 重要度（警告）有効/無効 */
	private Boolean warnValidFlg = Boolean.FALSE;

	/** 重要度（危険）有効/無効 */
	private Boolean criticalValidFlg = Boolean.FALSE;

	/** 重要度（不明）有効/無効 */
	private Boolean unknownValidFlg = Boolean.FALSE;

	/** アプリケーションフラグ */
	private Boolean applicationFlg = Boolean.FALSE;

	/** アプリケーション */
	@RestItemName(value=MessageConstant.APPLICATION)
	@RestValidateString(maxLen = 64)
	private String application;

	/** 監視詳細フラグ */
	private Boolean monitorDetailIdFlg = Boolean.FALSE;

	/** 監視詳細 */
	@RestItemName(value=MessageConstant.MONITOR_DETAIL_ID)
	@RestValidateString(maxLen = 1024)
	private String monitorDetailId;

	/** メッセージフラグ */
	private Boolean messageFlg = Boolean.FALSE;

	/** メッセージ */
	@RestItemName(value=MessageConstant.MESSAGE)
	@RestValidateString(maxLen = 4096)
	private String message;

	/** 拡張情報フラグ */
	private Boolean expFlg = Boolean.FALSE;

	/** 終了値 - 「常に」フラグ */
	private Boolean monitorAllEndValueFlg = Boolean.FALSE;

	/** 終了値 - 「常に」 */
	@RestItemName(value = MessageConstant.END_VALUE_JOBLINKRCV_ALL)
	@RestValidateInteger(minVal = -32768, maxVal = 32767)
	private Integer monitorAllEndValue;

	/** ジョブ連携メッセージの拡張情報設定 */
	@RestItemName(value = MessageConstant.EXTENDED_INFO)
	private ArrayList<JobLinkExpInfoRequest> jobLinkExpList;

	/** メッセージの引継ぎ情報設定 */
	private ArrayList<JobLinkInheritInfoRequest> jobLinkInheritList;

	/**
	 * ファシリティIDを返す。<BR>
	 * @return ファシリティID
	 */
	public String getFacilityID() {
		return facilityID;
	}

	/**
	 * ファシリティIDを設定する。<BR>
	 * @param facilityID ファシリティID
	 */
	public void setFacilityID(String facilityID) {
		this.facilityID = facilityID;
	}

	/**
	 *  終了値 - 「情報」を返す。<BR>
	 * @return  終了値 - 「情報」
	 */
	public Integer getMonitorInfoEndValue() {
		return monitorInfoEndValue;
	}

	/**
	 *  終了値 - 「情報」を設定する。<BR>
	 * @param monitorInfoEndValue  終了値 - 「情報」
	 */
	public void setMonitorInfoEndValue(Integer monitorInfoEndValue) {
		this.monitorInfoEndValue = monitorInfoEndValue;
	}

	/**
	 *  終了値 - 「警告」を返す。<BR>
	 * @return 終了値 - 「警告」
	 */
	public Integer getMonitorWarnEndValue() {
		return monitorWarnEndValue;
	}

	/**
	 * 終了値 - 「警告」を設定する。<BR>
	 * @param monitorWarnEndValue 終了値 - 「警告」
	 */
	public void setMonitorWarnEndValue(Integer monitorWarnEndValue) {
		this.monitorWarnEndValue = monitorWarnEndValue;
	}

	/**
	 * 終了値 - 「危険」を返す。<BR>
	 * @return 終了値 - 「危険」
	 */
	public Integer getMonitorCriticalEndValue() {
		return monitorCriticalEndValue;
	}

	/**
	 * 終了値 - 「危険」を設定する。<BR>
	 * @param monitorCriticalEndValue 終了値 - 「危険」
	 */
	public void setMonitorCriticalEndValue(Integer monitorCriticalEndValue) {
		this.monitorCriticalEndValue = monitorCriticalEndValue;
	}

	/**
	 * 終了値 - 「不明」を返す。<BR>
	 * @return 終了値 - 「不明」
	 */
	public Integer getMonitorUnknownEndValue() {
		return monitorUnknownEndValue;
	}

	/**
	 * 終了値 - 「不明」を設定する。<BR>
	 * @param monitorUnknownEndValue 終了値 - 「不明」
	 */
	public void setMonitorUnknownEndValue(Integer monitorUnknownEndValue) {
		this.monitorUnknownEndValue = monitorUnknownEndValue;
	}

	/**
	 * メッセージが得られなかった場合に終了する<BR>
	 * @return true:メッセージが得られなかった場合に終了する
	 */
	public Boolean getFailureEndFlg() {
		return failureEndFlg;
	}

	/**
	 * メッセージが得られなかった場合に終了する<BR>
	 * @param failureEndFlg true:メッセージが得られなかった場合に終了する
	 */
	public void setFailureEndFlg(Boolean failureEndFlg) {
		this.failureEndFlg = failureEndFlg;
	}

	/**
	 * メッセージが得られなかった場合に終了する - タイムアウトを返す。<BR>
	 * @return メッセージが得られなかった場合に終了する - タイムアウト
	 */
	public Integer getMonitorWaitTime() {
		return monitorWaitTime;
	}

	/**
	 * メッセージが得られなかった場合に終了する - タイムアウトを設定する。<BR>
	 * @param monitorWaitTime メッセージが得られなかった場合に終了する - タイムアウト
	 */
	public void setMonitorWaitTime(Integer monitorWaitTime) {
		this.monitorWaitTime = monitorWaitTime;
	}

	/**
	 * メッセージが得られなかった場合に終了する - 終了値を返す。<BR>
	 * @return メッセージが得られなかった場合に終了する - 終了値
	 */
	public Integer getMonitorWaitEndValue() {
		return monitorWaitEndValue;
	}

	/**
	 * メッセージが得られなかった場合に終了する - 終了値を設定する。<BR>
	 * @param monitorWaitEndValue メッセージが得られなかった場合に終了する - 終了値
	 */
	public void setMonitorWaitEndValue(Integer monitorWaitEndValue) {
		this.monitorWaitEndValue = monitorWaitEndValue;
	}

	/**
	 * ジョブ連携メッセージIDを返す。<BR>
	 * @return ジョブ連携メッセージID
	 */
	public String getJoblinkMessageId() {
		return joblinkMessageId;
	}

	/**
	 * ジョブ連携メッセージIDを設定する。<BR>
	 * @param joblinkMessageId ジョブ連携メッセージID
	 */
	public void setJoblinkMessageId(String joblinkMessageId) {
		this.joblinkMessageId = joblinkMessageId;
	}

	/**
	 * メッセージを返す。<BR>
	 * @return メッセージ
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * メッセージを設定する。<BR>
	 * @param message メッセージ
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * 確認期間フラグを返す。<BR>
	 * @return 確認期間フラグ
	 */
	public Boolean getPastFlg() {
		return pastFlg;
	}

	/**
	 * 確認期間フラグを設定する。<BR>
	 * @param pastFlg 確認期間フラグ
	 */
	public void setPastFlg(Boolean pastFlg) {
		this.pastFlg = pastFlg;
	}

	/**
	 * 確認期間（分）を返す。<BR>
	 * @return 確認期間（分）
	 */
	public Integer getPastMin() {
		return pastMin;
	}

	/**
	 * 確認期間（分）を設定する。<BR>
	 * @param pastMin 確認期間（分）
	 */
	public void setPastMin(Integer pastMin) {
		this.pastMin = pastMin;
	}

	/**
	 * 重要度（情報）有効/無効を返す。<BR>
	 * @return 重要度（情報）有効/無効
	 */
	public Boolean getInfoValidFlg() {
		return infoValidFlg;
	}

	/**
	 * 重要度（情報）有効/無効を設定する。<BR>
	 * @param infoValidFlg 重要度（情報）有効/無効
	 */
	public void setInfoValidFlg(Boolean infoValidFlg) {
		this.infoValidFlg = infoValidFlg;
	}

	/**
	 * 重要度（警告）有効/無効を返す。<BR>
	 * @return 重要度（警告）有効/無効
	 */
	public Boolean getWarnValidFlg() {
		return warnValidFlg;
	}

	/**
	 * 重要度（警告）有効/無効を設定する。<BR>
	 * @param warnValidFlg 重要度（警告）有効/無効
	 */
	public void setWarnValidFlg(Boolean warnValidFlg) {
		this.warnValidFlg = warnValidFlg;
	}

	/**
	 * 重要度（危険）有効/無効を返す。<BR>
	 * @return 重要度（危険）有効/無効
	 */
	public Boolean getCriticalValidFlg() {
		return criticalValidFlg;
	}

	/**
	 * 重要度（危険）有効/無効を設定する。<BR>
	 * @param criticalValidFlg 重要度（危険）有効/無効
	 */
	public void setCriticalValidFlg(Boolean criticalValidFlg) {
		this.criticalValidFlg = criticalValidFlg;
	}

	/**
	 * 重要度（不明）有効/無効を返す。<BR>
	 * @return 重要度（不明）有効/無効
	 */
	public Boolean getUnknownValidFlg() {
		return unknownValidFlg;
	}

	/**
	 * 重要度（不明）有効/無効を設定する。<BR>
	 * @param unknownValidFlg 重要度（不明）有効/無効
	 */
	public void setUnknownValidFlg(Boolean unknownValidFlg) {
		this.unknownValidFlg = unknownValidFlg;
	}

	/**
	 * アプリケーションフラグを返す。<BR>
	 * @return アプリケーションフラグ
	 */
	public Boolean getApplicationFlg() {
		return applicationFlg;
	}

	/**
	 * アプリケーションフラグを設定する。<BR>
	 * @param applicationFlg アプリケーションフラグ
	 */
	public void setApplicationFlg(Boolean applicationFlg) {
		this.applicationFlg = applicationFlg;
	}

	/**
	 * アプリケーションを返す。<BR>
	 * @return アプリケーション
	 */
	public String getApplication() {
		return application;
	}

	/**
	 * アプリケーションを設定する。<BR>
	 * @param application アプリケーション
	 */
	public void setApplication(String application) {
		this.application = application;
	}

	/**
	 * 監視詳細フラグを返す。<BR>
	 * @return 監視詳細フラグ
	 */
	public Boolean getMonitorDetailIdFlg() {
		return monitorDetailIdFlg;
	}

	/**
	 * 監視詳細フラグを設定する。<BR>
	 * @param monitorDetailIdFlg 監視詳細フラグ
	 */
	public void setMonitorDetailIdFlg(Boolean monitorDetailIdFlg) {
		this.monitorDetailIdFlg = monitorDetailIdFlg;
	}

	/**
	 * 監視詳細を返す。<BR>
	 * @return 監視詳細
	 */
	public String getMonitorDetailId() {
		return monitorDetailId;
	}

	/**
	 * 監視詳細を設定する。<BR>
	 * @param monitorDetailId 監視詳細
	 */
	public void setMonitorDetailId(String monitorDetailId) {
		this.monitorDetailId = monitorDetailId;
	}

	/**
	 * メッセージフラグを返す。<BR>
	 * @return メッセージフラグ
	 */
	public Boolean getMessageFlg() {
		return messageFlg;
	}

	/**
	 * メッセージフラグを設定する。<BR>
	 * @param messageFlg メッセージフラグ
	 */
	public void setMessageFlg(Boolean messageFlg) {
		this.messageFlg = messageFlg;
	}

	/**
	 * 拡張情報フラグを返す。<BR>
	 * @return 拡張情報フラグ
	 */
	public Boolean getExpFlg() {
		return expFlg;
	}

	/**
	 * 拡張情報フラグを設定する。<BR>
	 * @param expFlg 拡張情報フラグ
	 */
	public void setExpFlg(Boolean expFlg) {
		this.expFlg = expFlg;
	}

	/**
	 * 終了値 - 「常に」フラグを返す。<BR>
	 * @return 終了値 - 「常に」フラグ
	 */
	public Boolean getMonitorAllEndValueFlg() {
		return monitorAllEndValueFlg;
	}

	/**
	 * 終了値 - 「常に」フラグを設定する。<BR>
	 * @param monitorAllEndValueFlg 終了値 - 「常に」フラグ
	 */
	public void setMonitorAllEndValueFlg(Boolean monitorAllEndValueFlg) {
		this.monitorAllEndValueFlg = monitorAllEndValueFlg;
	}

	/**
	 * 終了値 - 「常に」 を返す。<BR>
	 * @return  終了値 - 「常に」
	 */
	public Integer getMonitorAllEndValue() {
		return monitorAllEndValue;
	}

	/**
	 *  終了値 - 「常に」を設定する。<BR>
	 * @param monitorAllEndValue  終了値 - 「常に」
	 */
	public void setMonitorAllEndValue(Integer monitorAllEndValue) {
		this.monitorAllEndValue = monitorAllEndValue;
	}

	/**
	 * ジョブ連携メッセージの拡張情報設定を返す。<BR>
	 * @return ジョブ連携メッセージの拡張情報設定
	 */
	public ArrayList<JobLinkInheritInfoRequest> getJobLinkInheritList() {
		return jobLinkInheritList;
	}

	/**
	 * ジョブ連携メッセージの拡張情報設定を設定する。<BR>
	 * @param jobLinkInheritList ジョブ連携メッセージの拡張情報設定
	 */
	public void setJobLinkInheritList(ArrayList<JobLinkInheritInfoRequest> jobLinkInheritList) {
		this.jobLinkInheritList = jobLinkInheritList;
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

		// [確認期間フラグ]がNullの場合はエラー
		if (pastFlg == null) {
			String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "pastFlg");
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
		}

		// [確認期間フラグ]がtrueの場合、[確認期間（分）]必須
		if (pastFlg && pastMin == null) {
			String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "pastMin");
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
		}

		// 重要度のいずれかが選択されていなければエラー
		if (!infoValidFlg && !warnValidFlg && !criticalValidFlg && !unknownValidFlg) {
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SELECT_ONE_OR_MORE.getMessage(
					MessageConstant.PRIORITY.getMessage()));
		}

		// [アプリケーションフラグ]がtrueの場合、[アプリケーション]必須
		if (applicationFlg
				&& (application == null || application.isEmpty())) {
			String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "application");
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
		}

		// [監視詳細フラグ]がtrueの場合、[監視詳細]必須
		if (monitorDetailIdFlg
				&& (monitorDetailId == null || monitorDetailId.isEmpty())) {
			String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "monitorDetailId");
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
		}

		// [メッセージフラグ]がtrueの場合、[メッセージ]必須
		if (messageFlg
				&& (message == null || message.isEmpty())) {
			String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "message");
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
		}

		// [拡張情報フラグ]がtrueの場合、[拡張情報]必須
		if (expFlg
				&& (jobLinkExpList == null || jobLinkExpList.isEmpty())) {
			String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "jobLinkExpList");
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
		}

		if (jobLinkExpList != null && !jobLinkExpList.isEmpty()) {
			HashMap<String, String> expMap = new HashMap<>();
			for (JobLinkExpInfoRequest exp : jobLinkExpList) {
				// [拡張情報]チェック
				exp.correlationCheck();

				// [拡張情報]でキーと値が重複して存在する場合エラー
				if (expMap.containsKey(exp.getKey())
						&& expMap.containsValue(exp.getValue())) {
					String[] r1 = {RestItemNameResolver.resolveItenName(this.getClass(), "jobLinkExpList"),
							String.format("%s,%s", exp.getKey(), exp.getValue())};
					throw new InvalidSetting(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(r1));
				}
				expMap.put(exp.getKey(), exp.getValue());
			}
		}

		if (monitorAllEndValueFlg) {
			// [メッセージが得られたら常に]がtrueの場合
			if (monitorAllEndValue == null) {
				String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "monitorAllEndValue");
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
			}
		} else {
			// [メッセージが得られたら常に]がfalseの場合
			if (monitorInfoEndValue == null) {
				String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "monitorInfoEndValue");
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
			}
			if (monitorWarnEndValue == null) {
				String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "monitorWarnEndValue");
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
			}
			if (monitorCriticalEndValue == null) {
				String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "monitorCriticalEndValue");
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
			}
			if (monitorUnknownEndValue == null) {
				String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "monitorUnknownEndValue");
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
			}
		}

		// [メッセージが得られなかった場合に終了する]がtrueの場合
		if (failureEndFlg) {
			if (monitorWaitTime == null) {
				String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "monitorWaitTime");
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
			}
			if (monitorWaitEndValue == null) {
				String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "monitorWaitEndValue");
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
			}
		}

		// [引継ぎ情報]
		if (jobLinkInheritList != null && !jobLinkInheritList.isEmpty()) {
			HashSet<String> inherits = new HashSet<>();
			for (JobLinkInheritInfoRequest inherit : jobLinkInheritList) {
				// [引継ぎ情報]チェック
				inherit.correlationCheck();

				// [引継ぎ情報]で[ジョブ変数名]が重複して存在する場合エラー
				if (inherits.contains(inherit.getParamId())) {
					String[] r1 = {RestItemNameResolver.resolveItenName(this.getClass(), "jobLinkInheritList"),
							inherit.getParamId()};
					throw new InvalidSetting(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(r1));
				}
				inherits.add(inherit.getParamId());
			}
		}
	}
}
