/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.bean;

import org.openapitools.client.model.JobDetailInfoResponse;

import com.clustercontrol.util.Messages;

/**
 * 実行状態の定数クラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class StatusMessage {
	/** 実行予定(状態の種別) */
	public static final String STRING_SCHEDULED = Messages.getString("scheduled");

	/** 待機(状態の種別) */
	public static final String STRING_WAIT = Messages.getString("wait");

	/** 保留中(状態の種別) */
	public static final String STRING_RESERVING = Messages.getString("reserving");

	/** スキップ(状態の種別) */
	public static final String STRING_SKIP = Messages.getString("skip");

	/** 実行中(キュー待機) (状態の種別) */
	public static final String STRING_RUNNING_QUEUE = Messages.getString("running.queue");
	
	/** 実行中(状態の種別) */
	public static final String STRING_RUNNING = Messages.getString("running");

	/** 停止処理中(状態の種別) */
	public static final String STRING_STOPPING = Messages.getString("stopping");

	/** 中断(状態の種別) */
	public static final String STRING_SUSPEND = Messages.getString("suspend");

	/** コマンド停止(状態の種別) */
	public static final String STRING_STOP = Messages.getString("stop.at.once");

	/** 中断(キュー待機) (状態の種別) */
	public static final String STRING_SUSPEND_QUEUE = Messages.getString("suspend.queue");

	/** 終了(状態の種別) */
	public static final String STRING_END = Messages.getString("end");

	/** 変更済(状態の種別) */
	public static final String STRING_MODIFIED = Messages.getString("modified");

	/** 終了(条件未達成) (状態の種別) */
	public static final String STRING_END_UNMATCH = Messages.getString("end.unmatch");

	/** 終了(カレンダ) (状態の種別) */
	public static final String STRING_END_CALENDAR = Messages.getString("end.calendar");

	/** 終了(スキップ) (状態の種別) */
	public static final String STRING_END_SKIP = Messages.getString("end.skip");

	/** 終了(開始遅延) (状態の種別) */
	public static final String STRING_END_START_DELAY = Messages.getString("end.start.delay");

	/** 終了(終了遅延) (状態の種別) */
	public static final String STRING_END_END_DELAY = Messages.getString("end.end.delay");

	/** 終了(排他分岐) (状態の種別) */
	public static final String STRING_END_EXCLUSIVE_BRANCH = Messages.getString("end.exclusive.branch");

	/** 終了(キューサイズ超過) (状態の種別) */
	public static final String STRING_END_QUEUE_LIMIT = Messages.getString("end.queue.limit");

	/** 終了(ファイル出力失敗) (状態の種別) */
	public static final String STRING_END_FAILED_OUTPUT = Messages.getString("end.failed.output");

	/** 未実行(管理対象外)(状態の種別) */
	public static final String STRING_NOT_MANAGED = Messages.getString("not.managed");

	/** 起動失敗(状態の種別) */
	public static final String STRING_ERROR = Messages.getString("start.error");

	/**
	 * 種別から文字列に変換する
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToString(int type) {
		if (type == StatusConstant.TYPE_RUNNING) {
			return STRING_RUNNING;
		} else if (type == StatusConstant.TYPE_END) {
			return STRING_END;
		} else if (type == StatusConstant.TYPE_SCHEDULED) {
			return STRING_SCHEDULED;
		} else if (type == StatusConstant.TYPE_WAIT) {
			return STRING_WAIT;
		} else if (type == StatusConstant.TYPE_STOPPING) {
			return STRING_STOPPING;
		} else if (type == StatusConstant.TYPE_STOP) {
			return STRING_STOP;
		} else if (type == StatusConstant.TYPE_RESERVING) {
			return STRING_RESERVING;
		} else if (type == StatusConstant.TYPE_MODIFIED) {
			return STRING_MODIFIED;
		} else if (type == StatusConstant.TYPE_END_CALENDAR) {
			return STRING_END_CALENDAR;
		} else if (type == StatusConstant.TYPE_END_UNMATCH) {
			return STRING_END_UNMATCH;
		} else if (type == StatusConstant.TYPE_END_EXCLUSIVE_BRANCH) {
			return STRING_END_EXCLUSIVE_BRANCH;
		} else if (type == StatusConstant.TYPE_END_SKIP) {
			return STRING_END_SKIP;
		} else if (type == StatusConstant.TYPE_END_START_DELAY) {
			return STRING_END_START_DELAY;
		} else if (type == StatusConstant.TYPE_END_END_DELAY) {
			return STRING_END_END_DELAY;
		} else if (type == StatusConstant.TYPE_ERROR) {
			return STRING_ERROR;
		} else if (type == StatusConstant.TYPE_SKIP) {
			return STRING_SKIP;
		} else if (type == StatusConstant.TYPE_SUSPEND) {
			return STRING_SUSPEND;
		} else if (type == StatusConstant.TYPE_RUNNING_QUEUE) {
			return STRING_RUNNING_QUEUE;
		} else if (type == StatusConstant.TYPE_SUSPEND_QUEUE) {
			return STRING_SUSPEND_QUEUE;
		} else if (type == StatusConstant.TYPE_END_QUEUE_LIMIT) {
			return STRING_END_QUEUE_LIMIT;
		} else if (type == StatusConstant.TYPE_NOT_MANAGED) {
			return STRING_NOT_MANAGED;
		} else if (type == StatusConstant.TYPE_END_FAILED_OUTPUT) {
			return STRING_END_FAILED_OUTPUT;
		}
		return "";
	}

	/**
	 * 文字列から種別に変換する
	 * 
	 * @param type
	 * @return
	 */
	public static int stringToType(String string) {
		if (string.equals(STRING_RUNNING)) {
			return StatusConstant.TYPE_RUNNING;
		} else if (string.equals(STRING_END)) {
			return StatusConstant.TYPE_END;
		} else if (string.equals(STRING_SCHEDULED)) {
			return StatusConstant.TYPE_SCHEDULED;
		} else if (string.equals(STRING_WAIT)) {
			return StatusConstant.TYPE_WAIT;
		} else if (string.equals(STRING_STOPPING)) {
			return StatusConstant.TYPE_STOPPING;
		} else if (string.equals(STRING_STOP)) {
			return StatusConstant.TYPE_STOP;
		} else if (string.equals(STRING_RESERVING)) {
			return StatusConstant.TYPE_RESERVING;
		} else if (string.equals(STRING_MODIFIED)) {
			return StatusConstant.TYPE_MODIFIED;
		} else if (string.equals(STRING_END_UNMATCH)) {
			return StatusConstant.TYPE_END_UNMATCH;
		} else if (string.equals(STRING_END_EXCLUSIVE_BRANCH)) {
			return StatusConstant.TYPE_END_EXCLUSIVE_BRANCH;
		} else if (string.equals(STRING_END_CALENDAR)) {
			return StatusConstant.TYPE_END_CALENDAR;
		} else if (string.equals(STRING_END_SKIP)) {
			return StatusConstant.TYPE_END_SKIP;
		} else if (string.equals(STRING_END_START_DELAY)) {
			return StatusConstant.TYPE_END_SKIP;
		} else if (string.equals(STRING_END_END_DELAY)) {
			return StatusConstant.TYPE_END_SKIP;
		} else if (string.equals(STRING_ERROR)) {
			return StatusConstant.TYPE_ERROR;
		} else if (string.equals(STRING_SKIP)) {
			return StatusConstant.TYPE_SKIP;
		} else if (string.equals(STRING_SUSPEND)) {
			return StatusConstant.TYPE_SUSPEND;
		} else if (string.equals(STRING_RUNNING_QUEUE)) {
			return StatusConstant.TYPE_RUNNING_QUEUE;
		} else if (string.equals(STRING_SUSPEND_QUEUE)) {
			return StatusConstant.TYPE_SUSPEND_QUEUE;
		} else if (string.equals(STRING_END_QUEUE_LIMIT)) {
			return StatusConstant.TYPE_END_QUEUE_LIMIT;
		} else if (string.equals(STRING_NOT_MANAGED)) {
			return StatusConstant.TYPE_NOT_MANAGED;
		} else if (string.equals(STRING_END_FAILED_OUTPUT)) {
			return StatusConstant.TYPE_END_FAILED_OUTPUT;
		}
		return -1;
	}

	/**
	 * 種別(enum)から文字列に変換します。<BR>
	 * 
	 * @param type
	 * @return
	 */
	public static String typeEnumValueToString(String type) {
		//findbugs対応 String比較方法をequelsに統一
		if (type.equals(JobDetailInfoResponse.StatusEnum.RUNNING.getValue())) {
			return STRING_RUNNING;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.END.getValue())) {
			return STRING_END;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.SCHEDULED.getValue())) {
			return STRING_SCHEDULED;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.WAIT.getValue())) {
			return STRING_WAIT;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.STOPPING.getValue())) {
			return STRING_STOPPING;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.STOP.getValue())) {
			return STRING_STOP;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.RESERVING.getValue())) {
			return STRING_RESERVING;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.MODIFIED.getValue())) {
			return STRING_MODIFIED;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.END_CALENDAR.getValue())) {
			return STRING_END_CALENDAR;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.END_UNMATCH.getValue())) {
			return STRING_END_UNMATCH;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.END_EXCLUSIVE_BRANCH.getValue())) {
			return STRING_END_EXCLUSIVE_BRANCH;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.END_SKIP.getValue())) {
			return STRING_END_SKIP;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.END_START_DELAY.getValue())) {
			return STRING_END_START_DELAY;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.END_END_DELAY.getValue())) {
			return STRING_END_END_DELAY;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.ERROR.getValue())) {
			return STRING_ERROR;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.SKIP.getValue())) {
			return STRING_SKIP;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.SUSPEND.getValue())) {
			return STRING_SUSPEND;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.RUNNING_QUEUE.getValue())) {
			return STRING_RUNNING_QUEUE;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.SUSPEND_QUEUE.getValue())) {
			return STRING_SUSPEND_QUEUE;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.END_QUEUE_LIMIT.getValue())) {
			return STRING_END_QUEUE_LIMIT;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.NOT_MANAGED.getValue())) {
			return STRING_NOT_MANAGED;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.END_FAILED_OUTPUT.getValue())) {
			return STRING_END_FAILED_OUTPUT;
		}
		return "";
	}
	
	/**
	 * 文字列から種別(enum)に変換します。<BR>
	 * 
	 * @param type
	 * @return typeEnumValue
	 */
	public static String stringTotypeEnumValue(String string) {
		if (string.equals(STRING_RUNNING)) {
			return JobDetailInfoResponse.StatusEnum.RUNNING.getValue();
		} else if (string.equals(STRING_END)) {
			return JobDetailInfoResponse.StatusEnum.END.getValue();
		} else if (string.equals(STRING_SCHEDULED)) {
			return JobDetailInfoResponse.StatusEnum.SCHEDULED.getValue();
		} else if (string.equals(STRING_WAIT)) {
			return JobDetailInfoResponse.StatusEnum.WAIT.getValue();
		} else if (string.equals(STRING_STOPPING)) {
			return JobDetailInfoResponse.StatusEnum.STOPPING.getValue();
		} else if (string.equals(STRING_STOP)) {
			return JobDetailInfoResponse.StatusEnum.STOP.getValue();
		} else if (string.equals(STRING_RESERVING)) {
			return JobDetailInfoResponse.StatusEnum.RESERVING.getValue();
		} else if (string.equals(STRING_MODIFIED)) {
			return JobDetailInfoResponse.StatusEnum.MODIFIED.getValue();
		} else if (string.equals(STRING_END_UNMATCH)) {
			return JobDetailInfoResponse.StatusEnum.END_UNMATCH.getValue();
		} else if (string.equals(STRING_END_EXCLUSIVE_BRANCH)) {
			return JobDetailInfoResponse.StatusEnum.END_EXCLUSIVE_BRANCH.getValue();
		} else if (string.equals(STRING_END_CALENDAR)) {
			return JobDetailInfoResponse.StatusEnum.END_CALENDAR.getValue();
		} else if (string.equals(STRING_END_SKIP)) {
			return JobDetailInfoResponse.StatusEnum.END_SKIP.getValue();
		} else if (string.equals(STRING_END_START_DELAY)) {
			return JobDetailInfoResponse.StatusEnum.END_SKIP.getValue();
		} else if (string.equals(STRING_END_END_DELAY)) {
			return JobDetailInfoResponse.StatusEnum.END_SKIP.getValue();
		} else if (string.equals(STRING_ERROR)) {
			return JobDetailInfoResponse.StatusEnum.ERROR.getValue();
		} else if (string.equals(STRING_SKIP)) {
			return JobDetailInfoResponse.StatusEnum.SKIP.getValue();
		} else if (string.equals(STRING_SUSPEND)) {
			return JobDetailInfoResponse.StatusEnum.SUSPEND.getValue();
		} else if (string.equals(STRING_RUNNING_QUEUE)) {
			return JobDetailInfoResponse.StatusEnum.RUNNING_QUEUE.getValue();
		} else if (string.equals(STRING_SUSPEND_QUEUE)) {
			return JobDetailInfoResponse.StatusEnum.SUSPEND_QUEUE.getValue();
		} else if (string.equals(STRING_END_QUEUE_LIMIT)) {
			return JobDetailInfoResponse.StatusEnum.END_QUEUE_LIMIT.getValue();
		} else if (string.equals(STRING_NOT_MANAGED)) {
			return JobDetailInfoResponse.StatusEnum.NOT_MANAGED.getValue();
		} else if (string.equals(STRING_END_FAILED_OUTPUT)) {
			return JobDetailInfoResponse.StatusEnum.END_FAILED_OUTPUT.getValue();
		}
		return null;
	}


}