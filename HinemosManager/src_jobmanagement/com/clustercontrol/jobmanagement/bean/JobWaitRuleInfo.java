/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.StatusConstant;

/**
 * ジョブの待ち条件に関する情報を保持するクラス<BR>
 * 
 * @version 2.1.0
 * @since 1.0.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobWaitRuleInfo implements Serializable {
	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = -6362706494732152461L;

	/** ログ出力のインスタンス<BR> */
	private static Log m_log = LogFactory.getLog( JobWaitRuleInfo.class );

	/** 保留 */
	private Boolean suspend = false;

	/** スキップ */
	private Boolean skip = false;

	/** スキップ時終了状態 */
	private Integer skipEndStatus = 0;

	/** スキップ時終了値 */
	private Integer skipEndValue = EndStatusConstant.INITIAL_VALUE_NORMAL;

	/** 判定対象の条件関係 */
	private Integer condition = ConditionTypeConstant.TYPE_AND;

	/** ジョブ判定対象情報 */
	private ArrayList<JobObjectInfo> object;

	/** 条件を満たさなければ終了する */
	private Boolean endCondition = false;

	/** 条件を満たさない時の終了状態 */
	private Integer endStatus = 0;

	/** 条件を満たさない時の終了値 */
	private Integer endValue = EndStatusConstant.INITIAL_VALUE_NORMAL;

	/** 排他分岐 */
	private Boolean exclusiveBranch = false;

	/** 排他分岐の終了状態 */
	private Integer exclusiveBranchEndStatus = 0;

	/** 排他分岐の終了値 */
	private Integer exclusiveBranchEndValue = EndStatusConstant.INITIAL_VALUE_NORMAL;

	/** 後続ジョブ優先度 */
	private List<JobNextJobOrderInfo> exclusiveBranchNextJobOrderList;

	/** カレンダ */
	private Boolean calendar = false;

	/** カレンダID */
	private String calendarId;

	/** カレンダにより未実行時の終了状態 */
	private Integer calendarEndStatus = EndStatusConstant.INITIAL_VALUE_NORMAL;

	/** カレンダにより未実行時の終了値 */
	private Integer calendarEndValue = EndStatusConstant.INITIAL_VALUE_NORMAL;
	
	/** 繰り返し実行フラグ */
	private Boolean jobRetryFlg = false;

	/** 繰り返し完了状態 */
	private Integer jobRetryEndStatus = null;

	/** 繰り返し回数 */
	private Integer jobRetry = 10;

	/** 開始遅延 */
	private Boolean start_delay = false;

	/** 開始遅延セッション開始後の時間 */
	private Boolean start_delay_session = false;

	/** 開始遅延セッション開始後の時間の値 */
	private Integer start_delay_session_value = 1;

	/** 開始遅延時刻 */
	private Boolean start_delay_time = false;

	/** 開始遅延時刻の値 */
	private Long start_delay_time_value = 0l;

	/** 開始遅延判定対象の条件関係 */
	private Integer start_delay_condition_type = ConditionTypeConstant.TYPE_AND;

	/** 開始遅延通知 */
	private Boolean start_delay_notify = false;

	/** 開始遅延通知重要度 */
	private Integer start_delay_notify_priority = 0;

	/** 開始遅延操作 */
	private Boolean start_delay_operation = false;

	/** 開始遅延操作種別 */
	private Integer start_delay_operation_type = OperationConstant.TYPE_STOP_SKIP;

	/** 開始遅延操作終了状態 */
	private Integer start_delay_operation_end_status = EndStatusConstant.INITIAL_VALUE_ABNORMAL;

	/** 開始遅延操作終了値 */
	private Integer start_delay_operation_end_value = EndStatusConstant.INITIAL_VALUE_NORMAL;

	/** 終了遅延 */
	private Boolean end_delay = false;

	/** 終了遅延セッション開始後の時間 */
	private Boolean end_delay_session = false;

	/** 終了遅延セッション開始後の時間の値 */
	private Integer end_delay_session_value = 1;

	/** 終了遅延ジョブ開始後の時間 */
	private Boolean end_delay_job = false;

	/** 終了遅延ジョブ開始後の時間の値 */
	private Integer end_delay_job_value = 1;

	/** 終了遅延時刻 */
	private Boolean end_delay_time = false;

	/** 終了遅延時刻の値 */
	private Long end_delay_time_value;

	/** 終了遅延判定対象の条件関係 */
	private Integer end_delay_condition_type = ConditionTypeConstant.TYPE_AND;

	/** 終了遅延通知 */
	private Boolean end_delay_notify = false;

	/** 終了遅延通知重要度 */
	private Integer end_delay_notify_priority = 0;

	/** 終了遅延操作 */
	private Boolean end_delay_operation = false;

	/** 終了遅延操作種別 */
	private Integer end_delay_operation_type = OperationConstant.TYPE_STOP_AT_ONCE;

	/** 終了遅延操作終了状態 */
	private Integer end_delay_operation_end_status = EndStatusConstant.INITIAL_VALUE_ABNORMAL;

	/** 終了遅延操作終了値 */
	private Integer end_delay_operation_end_value = EndStatusConstant.INITIAL_VALUE_NORMAL;

	/** 終了遅延実行履歴からの変化量 */
	private Boolean end_delay_change_mount = false;

	/** 終了遅延実行履歴からの変化量の値 */
	private Double end_delay_change_mount_value = 1D;

	/** 多重度 */
	private Boolean multiplicity_notify = true;
	private Integer multiplicity_notify_priority = PriorityConstant.TYPE_WARNING;
	private Integer multiplicity_operation = StatusConstant.TYPE_WAIT;
	private Integer multiplicity_end_value = -1;
	
	/** 同時実行制御キュー */
	private Boolean queueFlg = false;
	private String queueId = null;
	
	/**
	 * ジョブのスキップをするかしないかを返す。<BR>
	 * @return スキップをするかしないか
	 */
	public Boolean isSkip() {
		return skip;
	}

	/**
	 * ジョブのスキップをするかしないかを設定する。<BR>
	 * @param skip スキップするかしないか
	 */
	public void setSkip(Boolean skip) {
		this.skip = skip;
	}

	/**
	 * スキップ時の終了状態を返す。<BR>
	 * @return スキップ時終了状態
	 * @see com.clustercontrol.bean.EndStatusConstant
	 */
	public Integer getSkipEndStatus() {
		return skipEndStatus;
	}

	/**
	 * スキップ時の終了状態を設定する。<BR>
	 * @param endStatus スキップ時終了状態
	 * @see com.clustercontrol.bean.EndStatusConstant
	 */
	public void setSkipEndStatus(Integer endStatus) {
		this.skipEndStatus = endStatus;
	}

	/**
	 * スキップ時の終了値を返す。<BR>
	 * @return スキップ時終了値
	 */
	public Integer getSkipEndValue() {
		return skipEndValue;
	}

	/**
	 * スキップ時の終了値を設定する。<BR>
	 * @param endValue スキップ時終了値
	 */
	public void setSkipEndValue(Integer endValue) {
		this.skipEndValue = endValue;
	}

	/**
	 * 判定対象の条件関係を返す。<BR>
	 * @return 判定対象の条件関係
	 * @see com.clustercontrol.jobmanagement.bean.ConditionTypeConstant
	 */
	public Integer getCondition() {
		return condition;
	}

	/**
	 * 判定対象の条件関係を返す。<BR>
	 * @param condition 判定対象の条件関係
	 * @see com.clustercontrol.jobmanagement.bean.ConditionTypeConstant
	 */
	public void setCondition(Integer condition) {
		this.condition = condition;
	}

	/**
	 * 待ち条件を満たさない時の終了状態を返す。<BR>
	 * @return 待ち条件を満たさない時の終了状態
	 * @see com.clustercontrol.bean.EndStatusConstant
	 */
	public Integer getEndStatus() {
		return endStatus;
	}

	/**
	 * 待ち条件を満たさない時の終了状態を設定する。<BR>
	 * @param endStatus 待ち条件を満たさない時の終了状態
	 * @see com.clustercontrol.bean.EndStatusConstant
	 */
	public void setEndStatus(Integer endStatus) {
		this.endStatus = endStatus;
	}

	/**
	 * 待ち条件を満たさない時の終了値を返す。<BR>
	 * @return 待ち条件を満たさない時の終了値
	 */
	public Integer getEndValue() {
		return endValue;
	}

	/**
	 * 待ち条件を満たさない時の終了値を設定する。<BR>
	 * @param endValue 待ち条件を満たさない時の終了値
	 */
	public void setEndValue(Integer endValue) {
		this.endValue = endValue;
	}

	/**
	 * ジョブ判定対象情報を返す。<BR>
	 * @return ジョブ判定対象情報のリスト
	 * @see com.clustercontrol.jobmanagement.bean.JobObjectInfo
	 */
	public ArrayList<JobObjectInfo> getObject() {
		return object;
	}

	/**
	 * ジョブ判定対象情報を設定する。<BR>
	 * @param object ジョブ判定対象情報のリスト
	 * @see com.clustercontrol.jobmanagement.bean.JobObjectInfo
	 */
	public void setObject(ArrayList<JobObjectInfo> object) {
		this.object = object;
	}

	/**
	 * 待ち条件を満たさなければ終了するかどうかを返す。<BR>
	 * 
	 * @return 待ち条件を満たさなければ終了するかどうか
	 */
	public Boolean isEndCondition() {
		return endCondition;
	}

	/**
	 * まち条件を満たさなければ終了するかどうかを設定する。<BR>
	 * @param endCondition 待ち条件を満たさなければ終了するかどうか
	 */
	public void setEndCondition(Boolean endCondition) {
		this.endCondition = endCondition;
	}

	/**
	 * 後続ジョブを1つだけ実行するかどうかを返す。<BR>
	 * 
	 * @return 後続ジョブを1つだけ実行するかどうか
	 */
	public Boolean isExclusiveBranch() {
		return exclusiveBranch;
	}

	/**
	 * 後続ジョブを1つだけ実行するかどうか設定する。<BR>
	 * @param exclusiveBranch 後続ジョブを1つだけ実行するかどうか
	 */
	public void setExclusiveBranch(Boolean exclusiveBranch) {
		this.exclusiveBranch = exclusiveBranch;
	}

	/**
	 * 実行されなかった後続ジョブの終了状態を返す。<BR>
	 * 
	 * @return 実行されなかった後続ジョブの終了状態
	 */
	public Integer getExclusiveBranchEndStatus() {
		return exclusiveBranchEndStatus;
	}

	/**
	 * 実行されなかった後続ジョブの終了状態を設定する。<BR>
	 * 
	 * @param exclusiveBranchEndStatus 実行されなかった後続ジョブの終了状態
	 */
	public void setExclusiveBranchEndStatus(Integer exclusiveBranchEndStatus) {
		this.exclusiveBranchEndStatus = exclusiveBranchEndStatus;
	}

	/**
	 * 実行されなかった後続ジョブの終了値を返す。<BR>
	 * 
	 * @return 実行されなかった後続ジョブの終了値
	 */
	public Integer getExclusiveBranchEndValue() {
		return exclusiveBranchEndValue;
	}

	/**
	 * 実行されなかった後続ジョブの終了値を設定する。<BR>
	 * 
	 * @param exclusiveBranchEndStatus 実行されなかった後続ジョブの終了値
	 */
	public void setExclusiveBranchEndValue(Integer exclusiveBranchEndValue) {
		this.exclusiveBranchEndValue = exclusiveBranchEndValue;
	}

	/**
	 * 実行する後続ジョブの優先度リストを返す。<BR>
	 * 
	 * @return  実行する後続ジョブの優先度リスト
	 */
	public List<JobNextJobOrderInfo> getExclusiveBranchNextJobOrderList() {
		return this.exclusiveBranchNextJobOrderList;
	}

	/**
	 * 実行する後続ジョブの優先度リストを設定する。<BR>
	 * 
	 * @param exclusiveBranchNextJobOrderList 実行する後続ジョブの優先度リスト
	 */
	public void setExclusiveBranchNextJobOrderList(List<JobNextJobOrderInfo> exclusiveBranchNextJobOrderList) {
		this.exclusiveBranchNextJobOrderList = exclusiveBranchNextJobOrderList;
	}

	/**
	 * 保留するかどうかを返す。<BR>
	 * @return 保留するかどうか
	 */
	public Boolean isSuspend() {
		return suspend;
	}

	/**
	 * 保留するかどうかを設定する。<BR>
	 * @param suspend 保留するかどうか
	 */
	public void setSuspend(Boolean suspend) {
		this.suspend = suspend;
	}

	/**
	 * カレンダを返す。<BR>
	 * @return カレンダ
	 */
	public Boolean isCalendar() {
		return calendar;
	}

	/**
	 * カレンダを設定する。<BR>
	 * @param calendar カレンダ
	 */
	public void setCalendar(Boolean calendar) {
		this.calendar = calendar;
	}

	/**
	 * カレンダIDを返す。<BR>
	 * @return カレンダID
	 */
	public String getCalendarId() {
		return calendarId;
	}

	/**
	 * カレンダIDを設定する。<BR>
	 * @param calendarId カレンダID
	 */
	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	/**
	 * カレンダにより未実行となった場合の終了状態を返す。<BR>
	 * @return カレンダにより未実行となった場合の終了状態
	 */
	public Integer getCalendarEndStatus() {
		return calendarEndStatus;
	}

	/**
	 * カレンダにより未実行となった場合の終了状態を設定する。<BR>
	 * @param calendarEndValue カレンダにより未実行となった場合の終了状態
	 */
	public void setCalendarEndStatus(Integer calendarEndStatus) {
		this.calendarEndStatus = calendarEndStatus;
	}

	/**
	 * カレンダにより未実行となった場合の終了値を返す。<BR>
	 * @return カレンダにより未実行となった場合の終了値
	 */
	public Integer getCalendarEndValue() {
		return calendarEndValue;
	}

	/**
	 * カレンダにより未実行となった場合の終了値を設定する。<BR>
	 * @param calendarEndValue カレンダにより未実行となった場合の終了値
	 */
	public void setCalendarEndValue(Integer calendarEndValue) {
		this.calendarEndValue = calendarEndValue;
	}

	/**
	 * 繰り返し実行フラグを返す。<BR>
	 * @return 繰り返し実行フラグ
	 */
	public Boolean getJobRetryFlg() {
		return jobRetryFlg;
	}

	/**
	 * 繰り返し実行フラグを設定する。<BR>
	 * @param jobRetryFlg 繰り返し実行フラグ
	 */
	public void setJobRetryFlg(Boolean jobRetryFlg) {
		this.jobRetryFlg = jobRetryFlg;
	}

	/**
	 * 繰り返し完了状態を返す。<BR>
	 * @return
	 */
	public Integer getJobRetryEndStatus() {
		return jobRetryEndStatus;
	}

	/**
	 * 繰り返し完了状態を設定する。<BR>
	 * @param jobRetryEndStatus
	 */
	public void setJobRetryEndStatus(Integer jobRetryEndStatus) {
		this.jobRetryEndStatus = jobRetryEndStatus;
	}

	/**
	 * 繰り返し実行回数を返す。<BR>
	 * @return 繰り返し実行回数
	 */
	public Integer getJobRetry() {
		return jobRetry;
	}

	/**
	 * 繰り返し実行回数を設定する。<BR>
	 * @param jobRetry 繰り返し実行回数
	 */
	public void setJobRetry(Integer jobRetry) {
		this.jobRetry = jobRetry;
	}

	/**
	 * 終了遅延を監視するかどうかを返す。<BR>
	 * @return 終了遅延を監視するかどうか
	 */
	public Boolean isEnd_delay() {
		return end_delay;
	}

	/**
	 * 終了遅延を監視するかどうかを設定する。<BR>
	 * @param end_delay 終了遅延を監視するかどうか
	 */
	public void setEnd_delay(Boolean end_delay) {
		this.end_delay = end_delay;
	}

	/**
	 * 終了遅延判定対象の条件関係を返す。<BR>
	 * @return 終了遅延判定対象の条件関係
	 * @see com.clustercontrol.jobmanagement.bean.ConditionTypeConstant
	 */
	public Integer getEnd_delay_condition_type() {
		return end_delay_condition_type;
	}

	/**
	 * 終了遅延判定対象の条件関係を設定する。<BR>
	 * @param end_delay_condition_type 終了遅延判定対象の条件関係
	 * @see com.clustercontrol.jobmanagement.bean.ConditionTypeConstant
	 */
	public void setEnd_delay_condition_type(Integer end_delay_condition_type) {
		this.end_delay_condition_type = end_delay_condition_type;
	}

	/**
	 * 終了遅延の判定条件のうち、<BR>
	 * ジョブ開始後の時間で判定するかどうかを返す。<BR>
	 * @return ジョブ開始後の時間で終了遅延監視を行うかどうか
	 */
	public Boolean isEnd_delay_job() {
		return end_delay_job;
	}

	/**
	 * 終了遅延の判定条件のうち、<BR>
	 * ジョブ開始後の時間で判定するかどうかを設定する。<BR>
	 * @param end_delay_job ジョブ開始後の時間で終了遅延監視を行うかどうか
	 */
	public void setEnd_delay_job(Boolean end_delay_job) {
		this.end_delay_job = end_delay_job;
	}

	/**
	 * 終了遅延の判定条件のうち、<BR>
	 * ジョブ開始後の時間で判定する場合、<BR>
	 * 開始後何分で監視するのかの値を返す。<BR>
	 * @return 終了遅延ジョブ開始後の時間の値
	 */
	public Integer getEnd_delay_job_value() {
		return end_delay_job_value;
	}

	/**
	 * 終了遅延の判定条件のうち、<BR>
	 * ジョブ開始後の時間で判定する場合、<BR>
	 * 開始後何分で監視するかの値を設定する。<BR>
	 * @param end_delay_job_value 終了遅延ジョブ開始後の時間の値
	 */
	public void setEnd_delay_job_value(Integer end_delay_job_value) {
		this.end_delay_job_value = end_delay_job_value;
	}

	/**
	 * 終了遅延を通知するかどうかを返す。<BR>
	 * @return 終了遅延を通知するかどうか
	 */
	public Boolean isEnd_delay_notify() {
		return end_delay_notify;
	}

	/**
	 * 終了遅延を通知するかどうかを設定する。<BR>
	 * @param end_delay_notify 終了遅延を通知するかどうか
	 */
	public void setEnd_delay_notify(Boolean end_delay_notify) {
		this.end_delay_notify = end_delay_notify;
	}

	/**
	 * 終了遅延を通知する場合の重要度を返す。<BR>
	 * @return 終了遅延を通知する場合の重要度
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	public Integer getEnd_delay_notify_priority() {
		return end_delay_notify_priority;
	}

	/**
	 * 終了遅延を通知する場合の重要度を設定する。<BR>
	 * @param end_delay_notify_priority 終了遅延を通知する場合の重要度
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	public void setEnd_delay_notify_priority(Integer end_delay_notify_priority) {
		this.end_delay_notify_priority = end_delay_notify_priority;
	}

	/**
	 * 終了遅延時に操作するかどうかを返す。<BR>
	 * @return 終了遅延時に操作するかどうか
	 */
	public Boolean isEnd_delay_operation() {
		return end_delay_operation;
	}

	/**
	 * 終了遅延時に操作するかどうかを設定する。<BR>
	 * @param end_delay_operation 終了遅延時に操作するかどうか
	 */
	public void setEnd_delay_operation(Boolean end_delay_operation) {
		this.end_delay_operation = end_delay_operation;
	}

	/**
	 * 終了遅延時に操作する場合の終了状態を返す。<BR>
	 * @return 終了遅延時に操作する場合の終了状態
	 */
	public Integer getEnd_delay_operation_end_status() {
		return end_delay_operation_end_status;
	}

	/**
	 * 終了遅延時に操作する場合の終了状態を設定する。<BR>
	 * @param end_delay_operation_end_value 終了遅延時に操作する場合の終了状態
	 */
	public void setEnd_delay_operation_end_status(Integer end_delay_operation_end_status) {
		this.end_delay_operation_end_status = end_delay_operation_end_status;
	}

	/**
	 * 終了遅延時に操作する場合の終了値を返す。<BR>
	 * @return 終了遅延時に操作する場合の終了値
	 */
	public Integer getEnd_delay_operation_end_value() {
		return end_delay_operation_end_value;
	}

	/**
	 * 終了遅延時に操作する場合の終了値を設定する。<BR>
	 * @param end_delay_operation_end_value 終了遅延時に操作する場合の終了値
	 */
	public void setEnd_delay_operation_end_value(Integer end_delay_operation_end_value) {
		this.end_delay_operation_end_value = end_delay_operation_end_value;
	}

	/**
	 * 終了遅延時に操作する場合の操作種別を返す。<BR>
	 * @return 終了遅延時の操作種別
	 * @see com.clustercontrol.jobmanagement.bean.OperationConstant
	 */
	public Integer getEnd_delay_operation_type() {
		return end_delay_operation_type;
	}

	/**
	 * 終了遅延時に操作する場合の操作種別を設定する。<BR>
	 * @param end_delay_operation_type 終了遅延時の操作種別
	 * @see com.clustercontrol.jobmanagement.bean.OperationConstant
	 */
	public void setEnd_delay_operation_type(Integer end_delay_operation_type) {
		this.end_delay_operation_type = end_delay_operation_type;
	}

	/**
	 * 終了遅延の判定条件のうち、<BR>
	 * セッション開始後の時間で判定するかどうかを返す。<BR>
	 * @return セッション開始後の時間で終了遅延監視を行うかどうか
	 */
	public Boolean isEnd_delay_session() {
		return end_delay_session;
	}

	/**
	 * 終了遅延の判定条件のうち、<BR>
	 * セッション開始後の時間で判定するかどうかを設定する。<BR>
	 * @param end_delay_session セッション開始後の時間で終了遅延監視を行うかどうか
	 */
	public void setEnd_delay_session(Boolean end_delay_session) {
		this.end_delay_session = end_delay_session;
	}

	/**
	 * 終了遅延の判定条件のうち、<BR>
	 * セッション開始後の時間で判定する場合、<BR>
	 * 開始後何分で監視するかの値を返す。<BR>
	 * @return 終了遅延セッション開始後の時間の値
	 */
	public Integer getEnd_delay_session_value() {
		return end_delay_session_value;
	}

	/**
	 * 終了遅延の判定条件のうち、<BR>
	 * セッション開始後の時間で判定する場合、<BR>
	 * 開始後何分で監視するかの値を設定する。<BR>
	 * @param end_delay_session_value 終了遅延セッション開始後の時間の値
	 */
	public void setEnd_delay_session_value(Integer end_delay_session_value) {
		this.end_delay_session_value = end_delay_session_value;
	}

	/**
	 * 終了遅延の判定条件のうち、<BR>
	 * 時刻で判定するかどうかを返す。<BR>
	 * @return 終了遅延時刻
	 */
	public Boolean isEnd_delay_time() {
		return end_delay_time;
	}

	/**
	 * 終了遅延の判定条件のうち、<BR>
	 * 時刻で判定するかどうかを設定する。<BR>
	 * @param end_delay_time 終了遅延時刻
	 */
	public void setEnd_delay_time(Boolean end_delay_time) {
		this.end_delay_time = end_delay_time;
	}

	/**
	 * 終了遅延の判定条件のうち、<BR>
	 * 時刻で判定する場合の時刻を返す。<BR>
	 * @return 終了遅延時刻の値
	 */
	public Long getEnd_delay_time_value() {
		return end_delay_time_value;
	}

	/**
	 * 終了遅延の判定条件のうち、<BR>
	 * 時刻の判定する場合の値を設定する。<BR>
	 * @param end_delay_time_value 終了遅延時刻の値
	 */
	public void setEnd_delay_time_value(Long end_delay_time_value) {
		this.end_delay_time_value = end_delay_time_value;
	}

	/**
	 * 終了遅延の判定条件のうち、<BR>
	 * 実行履歴の変化量で判定するかを返す。<BR>
	 * @return 実行履歴の変化量で判定するか
	 */
	public Boolean isEnd_delay_change_mount() {
		return end_delay_change_mount;
	}

	/**
	 * 終了遅延の判定条件のうち、<BR>
	 * 実行履歴の変化量で判定するか設定する。<BR>
	 * @param end_delay_change_mount 実行履歴の変化量で判定するか
	 */
	public void setEnd_delay_change_mount(Boolean end_delay_change_mount) {
		this.end_delay_change_mount = end_delay_change_mount;
	}

	/**
	 * 終了遅延の判定条件のうち、<BR>
	 * 実行履歴の変化量の値を返す。<BR>
	 * @return 実行履歴の変化量の値
	 */
	public Double getEnd_delay_change_mount_value() {
		return end_delay_change_mount_value;
	}

	/**
	 * 終了遅延の判定条件のうち、<BR>
	 * 実行履歴の変化量の値を設定する。<BR>
	 * @param end_delay_change_mount_value 実行履歴の変化量の値
	 */
	public void setEnd_delay_change_mount_value(Double end_delay_change_mount_value) {
		this.end_delay_change_mount_value = end_delay_change_mount_value;
	}

	/**
	 * 開始遅延を監視するかどうかを返す。<BR>
	 * @return 開始遅延を監視するかどうか
	 */
	public Boolean isStart_delay() {
		return start_delay;
	}

	/**
	 * 開始遅延を監視するかどうかを設定する。<BR>
	 * @param start_delay 開始遅延を監視するかどうか
	 */
	public void setStart_delay(Boolean start_delay) {
		this.start_delay = start_delay;
	}

	/**
	 * 開始遅延判定対象の条件関係を返す。<BR>
	 * @return 開始遅延判定対象の条件関係
	 * @see com.clustercontrol.jobmanagement.bean.ConditionTypeConstant
	 */
	public Integer getStart_delay_condition_type() {
		return start_delay_condition_type;
	}

	/**
	 * 開始遅延判定対象の条件関係を設定する。<BR>
	 * @param start_delay_condition_type 開始遅延判定対象の条件関係
	 * @see com.clustercontrol.jobmanagement.bean.ConditionTypeConstant
	 */
	public void setStart_delay_condition_type(Integer start_delay_condition_type) {
		this.start_delay_condition_type = start_delay_condition_type;
	}

	/**
	 * 開始遅延を通知するかどうかを返す。<BR>
	 * @return 開始遅延を通知するかどうか
	 */
	public Boolean isStart_delay_notify() {
		return start_delay_notify;
	}

	/**
	 * 開始遅延を通知するかどうかを設定する。<BR>
	 * @param start_delay_notify 開始遅延通知
	 */
	public void setStart_delay_notify(Boolean start_delay_notify) {
		this.start_delay_notify = start_delay_notify;
	}

	/**
	 * 開始遅延を通知する場合の重要度を返す。<BR>
	 * @return 開始遅延を通知する場合の重要度
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	public Integer getStart_delay_notify_priority() {
		return start_delay_notify_priority;
	}

	/**
	 * 開始遅延を通知する場合の重要度を設定する。<BR>
	 * @param start_delay_notify_priority 開始遅延を通知する場合の重要度
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	public void setStart_delay_notify_priority(Integer start_delay_notify_priority) {
		this.start_delay_notify_priority = start_delay_notify_priority;
	}

	/**
	 * 開始遅延時に操作するかどうかを返す。<BR>
	 * @return 開始遅延時に操作するかどうか
	 */
	public Boolean isStart_delay_operation() {
		return start_delay_operation;
	}

	/**
	 * 開始遅延時に操作するかどうかを設定する。<BR>
	 * @param start_delay_operation 開始遅延時に操作するかどうか
	 */
	public void setStart_delay_operation(Boolean start_delay_operation) {
		this.start_delay_operation = start_delay_operation;
	}

	/**
	 * 開始遅延時に操作する場合の終了状態を返す。<BR>
	 * @return 開始遅延時に操作する場合の終了状態
	 */
	public Integer getStart_delay_operation_end_status() {
		return start_delay_operation_end_status;
	}

	/**
	 * 開始遅延時に操作する場合の終了状態を設定する。<BR>
	 * @param start_delay_operation_end_value 開始遅延時に操作する場合の終了状態
	 */
	public void setStart_delay_operation_end_status(
			Integer start_delay_operation_end_status) {
		this.start_delay_operation_end_status = start_delay_operation_end_status;
	}

	/**
	 * 開始遅延時に操作する場合の終了値を返す。<BR>
	 * @return 開始遅延時に操作する場合の終了値
	 */
	public Integer getStart_delay_operation_end_value() {
		return start_delay_operation_end_value;
	}

	/**
	 * 開始遅延時に操作する場合の終了値を設定する。<BR>
	 * @param start_delay_operation_end_value 開始遅延時に操作する場合の終了値
	 */
	public void setStart_delay_operation_end_value(
			Integer start_delay_operation_end_value) {
		this.start_delay_operation_end_value = start_delay_operation_end_value;
	}

	/**
	 * 開始遅延時に操作する場合の操作種別を返す。<BR>
	 * @return 開始遅延時の操作種別
	 * @see com.clustercontrol.jobmanagement.bean.OperationConstant
	 */
	public Integer getStart_delay_operation_type() {
		return start_delay_operation_type;
	}

	/**
	 * 開始遅延時に操作する場合の操作種別を返す。<BR>
	 * @param start_delay_operation_type 開始遅延時の操作種別
	 * @see com.clustercontrol.jobmanagement.bean.OperationConstant
	 */
	public void setStart_delay_operation_type(Integer start_delay_operation_type) {
		this.start_delay_operation_type = start_delay_operation_type;
	}

	/**
	 * 開始遅延の判定条件のうち、<BR>
	 * セッション開始後の時間で判定するかどうかを返す。<BR>
	 * @return 開始遅延セッション開始後の時間で判定するかどうか
	 */
	public Boolean isStart_delay_session() {
		return start_delay_session;
	}

	/**
	 * 開始遅延の判定条件のうち、<BR>
	 * セッション開始後の時間で判定するかどうかを設定する。<BR>
	 * @param start_delay_session 開始遅延セッション開始後の時間で判定するどうか
	 */
	public void setStart_delay_session(Boolean start_delay_session) {
		this.start_delay_session = start_delay_session;
	}

	/**
	 * 開始遅延の判定条件のうち、<BR>
	 * セッション開始後の時間で判定する場合、<BR>
	 * 開始後何分で監視するかの値を返す。<BR>
	 * @return 開始遅延セッション開始後の時間の値
	 */
	public Integer getStart_delay_session_value() {
		return start_delay_session_value;
	}

	/**
	 * 開始遅延の判定条件のうち、<BR>
	 * セッション開始後の時間で判定する場合、<BR>
	 * 開始後何分で監視するかの値を設定する。<BR>
	 * @param start_delay_session_value 開始遅延セッション開始後の時間の値
	 */
	public void setStart_delay_session_value(Integer start_delay_session_value) {
		this.start_delay_session_value = start_delay_session_value;
	}

	/**
	 * 開始遅延の判定条件のうち、<BR>
	 * 時刻で判定するかどうかを返す。<BR>
	 * @return 開始遅延時刻で判定するかどうか
	 */
	public Boolean isStart_delay_time() {
		return start_delay_time;
	}

	/**
	 * 開始遅延の判定条件のうち、<BR>
	 * 時刻で判定するかどうかを設定する。<BR>
	 * @param start_delay_time 開始遅延時刻で判定するかどうか
	 */
	public void setStart_delay_time(Boolean start_delay_time) {
		this.start_delay_time = start_delay_time;
	}

	/**
	 * 開始遅延の判定条件のうち、<BR>
	 * 時刻で判定する場合の時刻を返す。<BR>
	 * @return 開始遅延時刻の値
	 */
	public Long getStart_delay_time_value() {
		return start_delay_time_value;
	}

	/**
	 * 開始遅延の判定条件のうち、<BR>
	 * 時刻で判定する場合の時刻を設定する。<BR>
	 * @param start_delay_time_value 開始遅延時刻の値
	 */
	public void setStart_delay_time_value(Long start_delay_time_value) {
		this.start_delay_time_value = start_delay_time_value;
	}

	public Boolean isMultiplicityNotify() {
		return this.multiplicity_notify;
	}

	public void setMultiplicityNotify(Boolean multiplicity_notify) {
		this.multiplicity_notify = multiplicity_notify;
	}

	public Integer getMultiplicityNotifyPriority() {
		return this.multiplicity_notify_priority;
	}

	public void setMultiplicityNotifyPriority(Integer multiplicity_notify_priority) {
		this.multiplicity_notify_priority = multiplicity_notify_priority;
	}

	public Integer getMultiplicityOperation() {
		return this.multiplicity_operation;
	}

	public void setMultiplicityOperation(Integer multiplicity_operation) {
		this.multiplicity_operation = multiplicity_operation;
	}

	public Integer getMultiplicityEndValue() {
		return this.multiplicity_end_value;
	}

	public void setMultiplicityEndValue(Integer multiplicity_end_value) {
		this.multiplicity_end_value = multiplicity_end_value;
	}

	public Boolean getQueueFlg() {
		return queueFlg;
	}

	public void setQueueFlg(Boolean queueFlg) {
		this.queueFlg = queueFlg;
	}
	
	public String getQueueId() {
		return queueId;
	}

	public void setQueueId(String queueId) {
		this.queueId = queueId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((calendar == null) ? 0 : calendar.hashCode());
		result = prime
				* result
				+ ((calendarEndStatus == null) ? 0 : calendarEndStatus
						.hashCode());
		result = prime
				* result
				+ ((calendarEndValue == null) ? 0 : calendarEndValue.hashCode());
		result = prime * result
				+ ((calendarId == null) ? 0 : calendarId.hashCode());
		result = prime * result
				+ ((condition == null) ? 0 : condition.hashCode());
		result = prime * result
				+ ((endCondition == null) ? 0 : endCondition.hashCode());
		result = prime * result
				+ ((endStatus == null) ? 0 : endStatus.hashCode());
		result = prime * result
				+ ((endValue == null) ? 0 : endValue.hashCode());
		result = prime * result
				+ ((exclusiveBranch == null) ? 0 : exclusiveBranch.hashCode());
		result = prime * result
				+ ((exclusiveBranchEndStatus == null) ? 0 : exclusiveBranchEndStatus.hashCode());
		result = prime * result
				+ ((exclusiveBranchEndValue == null) ? 0 : exclusiveBranchEndValue.hashCode());
		result = prime * result
				+ ((exclusiveBranchNextJobOrderList == null) ? 0 : exclusiveBranchNextJobOrderList.hashCode());
		result = prime * result
				+ ((end_delay == null) ? 0 : end_delay.hashCode());
		result = prime
				* result
				+ ((end_delay_condition_type == null) ? 0
						: end_delay_condition_type.hashCode());
		result = prime * result
				+ ((end_delay_job == null) ? 0 : end_delay_job.hashCode());
		result = prime
				* result
				+ ((end_delay_job_value == null) ? 0 : end_delay_job_value
						.hashCode());
		result = prime
				* result
				+ ((end_delay_notify == null) ? 0 : end_delay_notify.hashCode());
		result = prime
				* result
				+ ((end_delay_notify_priority == null) ? 0
						: end_delay_notify_priority.hashCode());
		result = prime
				* result
				+ ((end_delay_operation == null) ? 0 : end_delay_operation
						.hashCode());
		result = prime
				* result
				+ ((end_delay_operation_end_status == null) ? 0
						: end_delay_operation_end_status.hashCode());
		result = prime
				* result
				+ ((end_delay_operation_end_value == null) ? 0
						: end_delay_operation_end_value.hashCode());
		result = prime
				* result
				+ ((end_delay_operation_type == null) ? 0
						: end_delay_operation_type.hashCode());
		result = prime
				* result
				+ ((end_delay_session == null) ? 0 : end_delay_session
						.hashCode());
		result = prime
				* result
				+ ((end_delay_session_value == null) ? 0
						: end_delay_session_value.hashCode());
		result = prime * result
				+ ((end_delay_time == null) ? 0 : end_delay_time.hashCode());
		result = prime
				* result
				+ ((end_delay_time_value == null) ? 0 : end_delay_time_value
						.hashCode());
		result = prime
				* result
				+ ((multiplicity_end_value == null) ? 0
						: multiplicity_end_value.hashCode());
		result = prime
				* result
				+ ((multiplicity_notify == null) ? 0 : multiplicity_notify
						.hashCode());
		result = prime
				* result
				+ ((multiplicity_notify_priority == null) ? 0
						: multiplicity_notify_priority.hashCode());
		result = prime
				* result
				+ ((multiplicity_operation == null) ? 0
						: multiplicity_operation.hashCode());
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		result = prime * result + ((skip == null) ? 0 : skip.hashCode());
		result = prime * result
				+ ((skipEndStatus == null) ? 0 : skipEndStatus.hashCode());
		result = prime * result
				+ ((skipEndValue == null) ? 0 : skipEndValue.hashCode());
		result = prime * result + ((jobRetryFlg == null) ? 0 : jobRetryFlg.hashCode());
		result = prime * result
				+ ((jobRetryEndStatus == null) ? 0 : jobRetryEndStatus.hashCode());
		result = prime * result
				+ ((jobRetry== null) ? 0 : jobRetry.hashCode());
		result = prime * result
				+ ((start_delay == null) ? 0 : start_delay.hashCode());
		result = prime
				* result
				+ ((start_delay_condition_type == null) ? 0
						: start_delay_condition_type.hashCode());
		result = prime
				* result
				+ ((start_delay_notify == null) ? 0 : start_delay_notify
						.hashCode());
		result = prime
				* result
				+ ((start_delay_notify_priority == null) ? 0
						: start_delay_notify_priority.hashCode());
		result = prime
				* result
				+ ((start_delay_operation == null) ? 0 : start_delay_operation
						.hashCode());
		result = prime
				* result
				+ ((start_delay_operation_end_status == null) ? 0
						: start_delay_operation_end_status.hashCode());
		result = prime
				* result
				+ ((start_delay_operation_end_value == null) ? 0
						: start_delay_operation_end_value.hashCode());
		result = prime
				* result
				+ ((start_delay_operation_type == null) ? 0
						: start_delay_operation_type.hashCode());
		result = prime
				* result
				+ ((start_delay_session == null) ? 0 : start_delay_session
						.hashCode());
		result = prime
				* result
				+ ((start_delay_session_value == null) ? 0
						: start_delay_session_value.hashCode());
		result = prime
				* result
				+ ((start_delay_time == null) ? 0 : start_delay_time.hashCode());
		result = prime
				* result
				+ ((start_delay_time_value == null) ? 0
						: start_delay_time_value.hashCode());
		result = prime * result + ((suspend == null) ? 0 : suspend.hashCode());
		result = prime * result + ((queueFlg == null) ? 0 : queueFlg.hashCode());
		result = prime * result + ((queueId == null) ? 0 : queueId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof JobWaitRuleInfo)) {
			return false;
		}
		JobWaitRuleInfo o1 = this;
		JobWaitRuleInfo o2 = (JobWaitRuleInfo)o;

		boolean ret = false;
		ret = 	equalsSub(o1.isSuspend(), o2.isSuspend()) &&
				equalsSub(o1.isSkip(), o2.isSkip()) &&
				equalsSub(o1.getSkipEndStatus(), o2.getSkipEndStatus()) &&
				equalsSub(o1.getSkipEndValue(), o2.getSkipEndValue()) &&
				equalsSub(o1.getJobRetryFlg(), o2.getJobRetryFlg()) &&
				equalsSub(o1.getJobRetryEndStatus(), o2.getJobRetryEndStatus()) &&
				equalsSub(o1.getJobRetry(), o2.getJobRetry()) &&
				equalsSub(o1.getCondition(), o2.getCondition()) &&
				equalsComparable(o1.getObject(), o2.getObject()) &&
				equalsSub(o1.isEndCondition(), o2.isEndCondition()) &&
				equalsSub(o1.getEndStatus(), o2.getEndStatus()) &&
				equalsSub(o1.getEndValue(), o2.getEndValue()) &&

				equalsSub(o1.isExclusiveBranch(), o2.isExclusiveBranch()) &&
				equalsSub(o1.getExclusiveBranchEndStatus(), o2.getExclusiveBranchEndStatus()) &&
				equalsSub(o1.getExclusiveBranchEndValue(), o2.getExclusiveBranchEndValue()) &&
				equalsList(o1.getExclusiveBranchNextJobOrderList(), o2.getExclusiveBranchNextJobOrderList()) &&

				equalsSub(o1.isCalendar(), o2.isCalendar()) &&
				equalsSub(o1.getCalendarId(), o2.getCalendarId()) &&
				equalsSub(o1.getCalendarEndStatus(), o2.getCalendarEndStatus()) &&
				equalsSub(o1.getCalendarEndValue(), o2.getCalendarEndValue()) &&

				equalsSub(o1.isStart_delay(), o2.isStart_delay()) &&
				equalsSub(o1.isStart_delay_session(), o2.isStart_delay_session()) &&
				equalsSub(o1.getStart_delay_session_value(), o2.getStart_delay_session_value()) &&
				equalsSub(o1.isStart_delay_time(), o2.isStart_delay_time()) &&
				equalsSub(o1.getStart_delay_time_value(), o2.getStart_delay_time_value()) &&
				equalsSub(o1.getStart_delay_condition_type(), o2.getStart_delay_condition_type()) &&
				equalsSub(o1.isStart_delay_notify(), o2.isStart_delay_notify()) &&
				equalsSub(o1.getStart_delay_notify_priority(), o2.getStart_delay_notify_priority()) &&
				equalsSub(o1.isStart_delay_operation(), o2.isStart_delay_operation()) &&
				equalsSub(o1.getStart_delay_operation_type(), o2.getStart_delay_operation_type()) &&
				equalsSub(o1.getStart_delay_operation_end_status(), o2.getStart_delay_operation_end_status()) &&
				equalsSub(o1.getStart_delay_operation_end_value(), o2.getStart_delay_operation_end_value()) &&

				equalsSub(o1.isEnd_delay(), o2.isEnd_delay()) &&
				equalsSub(o1.isEnd_delay_session(), o2.isEnd_delay_session()) &&
				equalsSub(o1.getEnd_delay_session_value(), o2.getEnd_delay_session_value()) &&
				equalsSub(o1.isEnd_delay_job(), o2.isEnd_delay_job()) &&
				equalsSub(o1.getEnd_delay_job_value(), o2.getEnd_delay_job_value()) &&
				equalsSub(o1.isEnd_delay_time(), o2.isEnd_delay_time()) &&
				equalsSub(o1.getEnd_delay_time_value(), o2.getEnd_delay_time_value()) &&
				equalsSub(o1.getEnd_delay_condition_type(), o2.getEnd_delay_condition_type()) &&
				equalsSub(o1.isEnd_delay_notify(), o2.isEnd_delay_notify()) &&
				equalsSub(o1.getEnd_delay_notify_priority(), o2.getEnd_delay_notify_priority()) &&
				equalsSub(o1.isEnd_delay_operation(), o2.isEnd_delay_operation()) &&
				equalsSub(o1.getEnd_delay_operation_type(), o2.getEnd_delay_operation_type()) &&
				equalsSub(o1.getEnd_delay_operation_end_status(), o2.getEnd_delay_operation_end_status()) &&
				equalsSub(o1.getEnd_delay_operation_end_value(), o2.getEnd_delay_operation_end_value()) &&
				equalsSub(o1.isEnd_delay_change_mount(), o2.isEnd_delay_change_mount()) &&
				equalsSub(o1.getEnd_delay_change_mount_value(), o2.getEnd_delay_change_mount_value()) &&

				equalsSub(o1.isMultiplicityNotify(), o2.isMultiplicityNotify()) &&
				equalsSub(o1.getMultiplicityNotifyPriority(), o2.getMultiplicityNotifyPriority()) &&
				equalsSub(o1.getMultiplicityOperation(), o2.getMultiplicityOperation()) &&
				equalsSub(o1.getMultiplicityEndValue(), o2.getMultiplicityEndValue()) &&

				equalsSub(o1.getQueueFlg(), o2.getQueueFlg()) &&
				equalsSub(o1.getQueueId(), o2.getQueueId());
		m_log.debug("waitRule ret = " + ret);
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
	
	private <T> boolean equalsList(List<T> list1, List<T> list2) {
		if (list1 != null && !list1.isEmpty()) {
			if (list2 != null && list1.size() == list2.size()) {
				for (int i = 0; i < list1.size(); i++) {
					if (!list1.get(i).equals(list2.get(i))) {
						if (m_log.isTraceEnabled()) {
							m_log.trace("equalsList : " + list1.get(i) + "!=" + list2.get(i));
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

	private <T extends Comparable<T>> boolean equalsComparable(List<T> list1, List<T> list2) {
		if (list1 != null && !list1.isEmpty()) {
			if (list2 != null && list1.size() == list2.size()) {
				Collections.sort(list1);
				Collections.sort(list2);
				for (int i = 0; i < list1.size(); i++) {
					if (!list1.get(i).equals(list2.get(i))) {
						if (m_log.isTraceEnabled()) {
							m_log.trace("equalsComparable : " + list1.get(i) + "!=" + list2.get(i));
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
