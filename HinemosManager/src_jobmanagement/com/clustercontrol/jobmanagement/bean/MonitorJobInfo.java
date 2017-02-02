/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 監視ジョブに関する情報を保持するクラス
 *
 * @version 5.1.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class MonitorJobInfo implements Serializable {
	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = 1L;

	private static Log m_log = LogFactory.getLog( MonitorJobInfo.class );

	/** ファシリティID */
	private String m_facilityID;

	/** スコープ */
	private String m_scope;

	/** スコープ処理 */
	private Integer m_processingMethod = 0;

	/** 正常終了するまでリトライフラグ */
	private Boolean m_commandRetryFlg = false;

	/** 正常終了するまでリトライ回数 */
	private Integer m_commandRetry;

	/** 監視ID */
	private String m_monitorId = "";

	/** 監視結果戻り値(情報) */
	private Integer m_monitorInfoEndValue = MonitorJobConstant.INITIAL_END_VALUE_INFO;

	/** 監視結果戻り値（警告） */
	private Integer m_monitorWarnEndValue = MonitorJobConstant.INITIAL_END_VALUE_WARN;

	/** 監視結果戻り値（危険） */
	private Integer m_monitorCriticalEndValue = MonitorJobConstant.INITIAL_END_VALUE_CRITICAL;

	/** 監視結果戻り値（不明） */
	private Integer m_monitorUnknownEndValue = MonitorJobConstant.INITIAL_END_VALUE_UNKNOWN;

	/** 監視結果が得られない場合の待ち時間（分） */
	private Integer m_monitorWaitTime = 1;

	/** 監視結果が得られない場合の終了値 */
	private Integer m_monitorWaitEndValue = MonitorJobConstant.INITIAL_END_VALUE_UNKNOWN;

	/**
	 * スコープを返す。<BR>
	 * @return スコープ
	 */
	public String getScope() {
		return m_scope;
	}

	/**
	 * スコープを設定する。<BR>
	 * @param scope スコープ
	 */
	public void setScope(String scope) {
		this.m_scope = scope;
	}

	/**
	 * ファシリティIDを返す。<BR>
	 * @return ファシリティID
	 */
	public String getFacilityID() {
		return m_facilityID;
	}

	/**
	 * ファシリティIDを設定する。<BR>
	 * @param facilityID ファシリティID
	 */
	public void setFacilityID(String facilityID) {
		this.m_facilityID = facilityID;
	}

	/**
	 * スコープ処理を返す。<BR>
	 * @return スコープ処理
	 * @see com.clustercontrol.bean.ProcessingMethodConstant
	 */
	public Integer getProcessingMethod() {
		return m_processingMethod;
	}

	/**
	 * スコープ処理を設定する。<BR>
	 * @param processingMethod スコープ処理
	 * @see com.clustercontrol.bean.ProcessingMethodConstant
	 */
	public void setProcessingMethod(Integer processingMethod) {
		this.m_processingMethod = processingMethod;
	}

	/**
	 * 正常終了するまでリトライフラグを返す。<BR>
	 * @return 正常終了するまでリトライフラグ
	 */
	public Boolean isCommandRetryFlg() {
		return m_commandRetryFlg;
	}

	/**
	 * 正常終了するまでリトライフラグを設定する。<BR>
	 * @param errorRetryFlg 正常終了するまでリトライフラグ
	 */
	public void setCommandRetryFlg(Boolean commandRetryFlg) {
		this.m_commandRetryFlg = commandRetryFlg;
	}

	/**
	 * 正常終了するまでリトライ回数を返す。<BR>
	 * @return 正常終了するまでリトライ回数
	 */
	public Integer getCommandRetry() {
		return m_commandRetry;
	}
	/**
	 * 正常終了するまでリトライ回数を設定する。<BR>
	 * @param commandRetry 正常終了するまでリトライ回数
	 */
	public void setCommandRetry(Integer commandRetry) {
		this.m_commandRetry = commandRetry;
	}

	/**
	 * 監視IDを返す。<BR>
	 * @return 監視ID
	 */
	public String getMonitorId() {
		return m_monitorId;
	}
	/**
	 * 監視IDを設定する。<BR>
	 * @param monitorId 監視ID
	 */
	public void setMonitorId(String monitorId) {
		this.m_monitorId = monitorId;
	}

	/**
	 * 監視結果終了値（情報）を返す。<BR>
	 * @return 監視結果終了値（情報）
	 */
	public Integer getMonitorInfoEndValue() {
		return m_monitorInfoEndValue;
	}
	/**
	 * 監視結果終了値（情報）を設定する。<BR>
	 * @param monitorInfoEndValue 監視結果終了値（情報）
	 */
	public void setMonitorInfoEndValue(Integer monitorInfoEndValue) {
		this.m_monitorInfoEndValue = monitorInfoEndValue;
	}

	/**
	 * 監視結果終了値（警告）を返す。<BR>
	 * @return 監視結果終了値（警告）
	 */
	public Integer getMonitorWarnEndValue() {
		return m_monitorWarnEndValue;
	}
	/**
	 * 監視結果終了値（警告）を設定する。<BR>
	 * @param monitorWarnEndValue 監視結果終了値（警告）
	 */
	public void setMonitorWarnEndValue(Integer monitorWarnEndValue) {
		this.m_monitorWarnEndValue = monitorWarnEndValue;
	}

	/**
	 * 監視結果終了値（危険）を返す。<BR>
	 * @return 監視結果終了値（危険）
	 */
	public Integer getMonitorCriticalEndValue() {
		return m_monitorCriticalEndValue;
	}
	/**
	 * 監視結果終了値（危険）を設定する。<BR>
	 * @param monitorCriticalEndValue 監視結果終了値（危険）
	 */
	public void setMonitorCriticalEndValue(Integer monitorCriticalEndValue) {
		this.m_monitorCriticalEndValue = monitorCriticalEndValue;
	}

	/**
	 * 監視結果終了値（不明）を返す。<BR>
	 * @return 監視結果終了値（不明）
	 */
	public Integer getMonitorUnknownEndValue() {
		return m_monitorUnknownEndValue;
	}
	/**
	 * 監視結果終了値（不明）を設定する。<BR>
	 * @param monitorUnknownEndValue 監視結果終了値（不明）
	 */
	public void setMonitorUnknownEndValue(Integer monitorUnknownEndValue) {
		this.m_monitorUnknownEndValue = monitorUnknownEndValue;
	}

	/**
	 * 監視結果が得られない場合の待ち時間を返す。<BR>
	 * @return 監視結果が得られない場合の待ち時間
	 */
	public Integer getMonitorWaitTime() {
		return m_monitorWaitTime;
	}
	/**
	 * 監視結果が得られない場合の待ち時間を設定する。<BR>
	 * @param monitorWaitTime 監視結果が得られない場合の待ち時間
	 */
	public void setMonitorWaitTime(Integer monitorWaitTime) {
		this.m_monitorWaitTime = monitorWaitTime;
	}

	/**
	 * 監視結果が得られない場合の終了値を返す。<BR>
	 * @return 監視結果が得られない場合の終了値
	 */
	public Integer getMonitorWaitEndValue() {
		return m_monitorWaitEndValue;
	}
	/**
	 * 監視結果が得られない場合の終了値を設定する。<BR>
	 * @param monitorWaitEndValue 監視結果が得られない場合の終了値
	 */
	public void setMonitorWaitEndValue(Integer monitorWaitEndValue) {
		this.m_monitorWaitEndValue = monitorWaitEndValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((m_commandRetryFlg == null) ? 0 : m_commandRetryFlg.hashCode());
		result = prime * result
				+ ((m_commandRetry == null) ? 0 : m_commandRetry.hashCode());
		result = prime * result
				+ ((m_facilityID == null) ? 0 : m_facilityID.hashCode());
		result = prime * result
				+ ((m_processingMethod == null) ? 0 : m_processingMethod.hashCode());
		result = prime * result + ((m_scope == null) ? 0 : m_scope.hashCode());
		result = prime * result + ((m_monitorId == null) ? 0 : m_monitorId.hashCode());
		result = prime * result + ((m_monitorInfoEndValue == null) ? 0 : m_monitorInfoEndValue.hashCode());
		result = prime * result + ((m_monitorWarnEndValue == null) ? 0 : m_monitorWarnEndValue.hashCode());
		result = prime * result + ((m_monitorCriticalEndValue == null) ? 0 : m_monitorCriticalEndValue.hashCode());
		result = prime * result + ((m_monitorUnknownEndValue == null) ? 0 : m_monitorUnknownEndValue.hashCode());
		result = prime * result + ((m_monitorWaitTime == null) ? 0 : m_monitorWaitTime.hashCode());
		result = prime * result + ((m_monitorWaitEndValue == null) ? 0 : m_monitorWaitEndValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof MonitorJobInfo)) {
			return false;
		}
		MonitorJobInfo o1 = this;
		MonitorJobInfo o2 = (MonitorJobInfo)o;

		boolean ret = false;
		ret = 	equalsSub(o1.getFacilityID(), o2.getFacilityID()) &&
				equalsSub(o1.isCommandRetryFlg(), o2.isCommandRetryFlg()) &&
				equalsSub(o1.getCommandRetry(), o2.getCommandRetry()) &&
				equalsSub(o1.getProcessingMethod(), o2.getProcessingMethod()) &&
				equalsSub(o1.getMonitorId(), o2.getMonitorId()) &&
				equalsSub(o1.getMonitorInfoEndValue(), o2.getMonitorInfoEndValue()) &&
				equalsSub(o1.getMonitorWarnEndValue(), o2.getMonitorWarnEndValue()) &&
				equalsSub(o1.getMonitorCriticalEndValue(), o2.getMonitorCriticalEndValue()) &&
				equalsSub(o1.getMonitorUnknownEndValue(), o2.getMonitorUnknownEndValue()) &&
				equalsSub(o1.getMonitorWaitTime(), o2.getMonitorWaitTime()) &&
				equalsSub(o1.getMonitorWarnEndValue(), o2.getMonitorWarnEndValue());
		return ret;
	}

	private boolean equalsSub(Object o1, Object o2) {
		if (o1 == o2)
			return true;
		
		if (o1 == null)
			return false;
		
		boolean ret = o1.equals(o2);
		if (!ret) {
			if (m_log.isTraceEnabled()) {
				m_log.trace("equalsSub : " + o1 + "!=" + o2);
			}
		}
		return ret;
	}
}