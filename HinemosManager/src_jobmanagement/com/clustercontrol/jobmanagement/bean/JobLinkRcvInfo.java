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

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ジョブ連携送信ジョブに関する情報を保持するクラス
 *
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobLinkRcvInfo implements Serializable {

	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = 7216413607782857671L;

	private static Log m_log = LogFactory.getLog( JobLinkRcvInfo.class );

	/** スコープ処理 */
	private Integer processingMethod = 0;

	/** ファシリティID */
	private String facilityID;

	/** スコープ */
	private String scope;

	/** 終了値 - 「情報」 */
	private Integer monitorInfoEndValue;

	/** 終了値 - 「警告」 */
	private Integer monitorWarnEndValue;

	/** 終了値 - 「危険」 */
	private Integer monitorCriticalEndValue;

	/** 終了値 - 「不明」 */
	private Integer monitorUnknownEndValue;

	/** メッセージが得られなかった場合に終了する */
	private Boolean failureEndFlg;

	/** メッセージが得られなかった場合に終了する - タイムアウト */
	private Integer monitorWaitTime;

	/** メッセージが得られなかった場合に終了する - 終了値 */
	private Integer monitorWaitEndValue;

	/** ジョブ連携メッセージID */
	private String joblinkMessageId;

	/** メッセージ */
	private String message;

	/** 確認期間フラグ */
	private Boolean pastFlg;

	/** 確認期間（分） */
	private Integer pastMin;

	/** 重要度（情報）有効/無効 */
	private Boolean infoValidFlg;

	/** 重要度（警告）有効/無効 */
	private Boolean warnValidFlg;

	/** 重要度（危険）有効/無効 */
	private Boolean criticalValidFlg;

	/** 重要度（不明）有効/無効 */
	private Boolean unknownValidFlg;

	/** アプリケーションフラグ */
	private Boolean applicationFlg;

	/** アプリケーション */
	private String application;

	/** 監視詳細フラグ */
	private Boolean monitorDetailIdFlg;

	/** 監視詳細 */
	private String monitorDetailId;

	/** メッセージフラグ */
	private Boolean messageFlg;

	/** 拡張情報フラグ */
	private Boolean expFlg;

	/** 終了値 - 「常に」フラグ */
	private Boolean monitorAllEndValueFlg;

	/** 終了値 - 「常に」 */
	private Integer monitorAllEndValue;

	/** ジョブ連携メッセージの拡張情報設定 */
	private ArrayList<JobLinkExpInfo> jobLinkExpList;

	/** メッセージの引継ぎ情報設定 */
	private ArrayList<JobLinkInheritInfo> jobLinkInheritList;

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
	 * スコープ処理を返す。<BR>
	 * @return スコープ処理
	 * @see com.clustercontrol.bean.ProcessingMethodConstant
	 */
	public Integer getProcessingMethod() {
		return processingMethod;
	}

	/**
	 * スコープ処理を設定する。<BR>
	 * @param processingMethod スコープ処理
	 * @see com.clustercontrol.bean.ProcessingMethodConstant
	 */
	public void setProcessingMethod(Integer processingMethod) {
		this.processingMethod = processingMethod;
	}

	/**
	 * スコープを返す。<BR>
	 * @return スコープ
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * スコープを設定する。<BR>
	 * @param scope スコープ
	 */
	public void setScope(String scope) {
		this.scope = scope;
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
	public ArrayList<JobLinkInheritInfo> getJobLinkInheritList() {
		return jobLinkInheritList;
	}

	/**
	 * ジョブ連携メッセージの拡張情報設定を設定する。<BR>
	 * @param jobLinkInheritList ジョブ連携メッセージの拡張情報設定
	 */
	public void setJobLinkInheritList(ArrayList<JobLinkInheritInfo> jobLinkInheritList) {
		this.jobLinkInheritList = jobLinkInheritList;
	}

	/**
	 * ジョブ連携メッセージの拡張情報設定を返す。<BR>
	 * @return
	 */
	public ArrayList<JobLinkExpInfo> getJobLinkExpList() {
		return jobLinkExpList;
	}

	/**
	 * ジョブ連携メッセージの拡張情報設定を設定する。<BR>
	 * @param jobLinkJobExpList
	 */
	public void setJobLinkExpList(ArrayList<JobLinkExpInfo> jobLinkExpList) {
		this.jobLinkExpList = jobLinkExpList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((application == null) ? 0 : application.hashCode());
		result = prime * result + ((applicationFlg == null) ? 0 : applicationFlg.hashCode());
		result = prime * result + ((criticalValidFlg == null) ? 0 : criticalValidFlg.hashCode());
		result = prime * result + ((expFlg == null) ? 0 : expFlg.hashCode());
		result = prime * result + ((facilityID == null) ? 0 : facilityID.hashCode());
		result = prime * result + ((infoValidFlg == null) ? 0 : infoValidFlg.hashCode());
		result = prime * result + ((jobLinkExpList == null) ? 0 : jobLinkExpList.hashCode());
		result = prime * result + ((jobLinkInheritList == null) ? 0 : jobLinkInheritList.hashCode());
		result = prime * result + ((joblinkMessageId == null) ? 0 : joblinkMessageId.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((messageFlg == null) ? 0 : messageFlg.hashCode());
		result = prime * result + ((monitorAllEndValue == null) ? 0 : monitorAllEndValue.hashCode());
		result = prime * result + ((monitorAllEndValueFlg == null) ? 0 : monitorAllEndValueFlg.hashCode());
		result = prime * result + ((monitorCriticalEndValue == null) ? 0 : monitorCriticalEndValue.hashCode());
		result = prime * result + ((monitorDetailId == null) ? 0 : monitorDetailId.hashCode());
		result = prime * result + ((monitorDetailIdFlg == null) ? 0 : monitorDetailIdFlg.hashCode());
		result = prime * result + ((monitorInfoEndValue == null) ? 0 : monitorInfoEndValue.hashCode());
		result = prime * result + ((monitorUnknownEndValue == null) ? 0 : monitorUnknownEndValue.hashCode());
		result = prime * result + ((failureEndFlg == null) ? 0 : failureEndFlg.hashCode());
		result = prime * result + ((monitorWaitEndValue == null) ? 0 : monitorWaitEndValue.hashCode());
		result = prime * result + ((monitorWaitTime == null) ? 0 : monitorWaitTime.hashCode());
		result = prime * result + ((monitorWarnEndValue == null) ? 0 : monitorWarnEndValue.hashCode());
		result = prime * result + ((pastFlg == null) ? 0 : pastFlg.hashCode());
		result = prime * result + ((pastMin == null) ? 0 : pastMin.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		result = prime * result + ((unknownValidFlg == null) ? 0 : unknownValidFlg.hashCode());
		result = prime * result + ((warnValidFlg == null) ? 0 : warnValidFlg.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof JobLinkRcvInfo)) {
			return false;
		}
		JobLinkRcvInfo o1 = this;
		JobLinkRcvInfo o2 = (JobLinkRcvInfo) obj;

		boolean ret = false;
		ret = equalsSub(o1.getFacilityID(), o2.getFacilityID()) &&
				equalsSub(o1.getScope(), o2.getScope()) &&
				equalsSub(o1.getMonitorInfoEndValue(), o2.getMonitorInfoEndValue()) &&
				equalsSub(o1.getMonitorWarnEndValue(), o2.getMonitorWarnEndValue()) &&
				equalsSub(o1.getMonitorCriticalEndValue(), o2.getMonitorCriticalEndValue()) &&
				equalsSub(o1.getMonitorUnknownEndValue(), o2.getMonitorUnknownEndValue()) &&
				equalsSub(o1.getFailureEndFlg(), o2.getFailureEndFlg()) &&
				equalsSub(o1.getMonitorWaitTime(), o2.getMonitorWaitTime()) &&
				equalsSub(o1.getMonitorWaitEndValue(), o2.getMonitorWaitEndValue()) &&
				equalsSub(o1.getJoblinkMessageId(), o2.getJoblinkMessageId()) &&
				equalsSub(o1.getMessage(), o2.getMessage()) &&
				equalsSub(o1.getPastFlg(), o2.getPastFlg()) &&
				equalsSub(o1.getPastMin(), o2.getPastMin()) &&
				equalsSub(o1.getInfoValidFlg(), o2.getInfoValidFlg()) &&
				equalsSub(o1.getWarnValidFlg(), o2.getWarnValidFlg()) &&
				equalsSub(o1.getCriticalValidFlg(), o2.getCriticalValidFlg()) &&
				equalsSub(o1.getUnknownValidFlg(), o2.getUnknownValidFlg()) &&
				equalsSub(o1.getApplicationFlg(), o2.getApplicationFlg()) &&
				equalsSub(o1.getApplication(), o2.getApplication()) &&
				equalsSub(o1.getMonitorDetailIdFlg(), o2.getMonitorDetailIdFlg()) &&
				equalsSub(o1.getMonitorDetailId(), o2.getMonitorDetailId()) &&
				equalsSub(o1.getMessageFlg(), o2.getMessageFlg()) &&
				equalsSub(o1.getExpFlg(), o2.getExpFlg()) &&
				equalsSub(o1.getMonitorAllEndValueFlg(), o2.getMonitorAllEndValueFlg()) &&
				equalsSub(o1.getMonitorAllEndValue(), o2.getMonitorAllEndValue()) &&
				equalsArray(o1.getJobLinkInheritList(), o2.getJobLinkInheritList()) &&
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
}