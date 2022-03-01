/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * 実行状態の定数クラス<BR>
 * 
 * @since 1.0.0
 */
public class StatusConstant {
	/** 実行予定(状態の種別) */
	public static final int TYPE_SCHEDULED = 9;

	/** 待機(状態の種別) */
	public static final int TYPE_WAIT = 0;

	/** 保留中(状態の種別) */
	public static final int TYPE_RESERVING = 1;

	/** スキップ(状態の種別) */
	public static final int TYPE_SKIP = 2;

	/** 実行中(状態の種別) */
	public static final int TYPE_RUNNING = 100;

	/** 停止処理中(状態の種別) */
	public static final int TYPE_STOPPING = 101;

	/**
	 * 実行中(キュー待機) (状態の種別)
	 * @since 6.2.0
	 */
	public static final int TYPE_RUNNING_QUEUE = 102;
	
	/** 中断(状態の種別) */
	public static final int TYPE_SUSPEND = 200;

	/** コマンド停止(状態の種別) */
	public static final int TYPE_STOP = 201;

	/**
	 * 中断(キュー待機) (状態の種別)
	 * @since 6.2.0
	 */
	public static final int TYPE_SUSPEND_QUEUE = 202;

	/** 終了(状態の種別) */
	public static final int TYPE_END = 300;

	/** 変更済(状態の種別) */
	public static final int TYPE_MODIFIED = 301;

	/** 終了(条件未達成) (状態の種別) */
	public static final int TYPE_END_UNMATCH = 302;

	/** 終了(カレンダ) (状態の種別) */
	public static final int TYPE_END_CALENDAR = 303;

	/** 終了(スキップ) (状態の種別) */
	public static final int TYPE_END_SKIP = 304;

	/** 終了(開始遅延) (状態の種別) */
	public static final int TYPE_END_START_DELAY = 305;

	/** 終了(終了遅延) (状態の種別) */
	public static final int TYPE_END_END_DELAY = 306;
	
	/** 終了(排他条件分岐) (状態の種別) */
	public static final int TYPE_END_EXCLUSIVE_BRANCH = 307;

	/**
	 * 終了(キューサイズ超過) (状態の種別)
	 * @since 6.2.0
	 */
	public static final int TYPE_END_QUEUE_LIMIT = 308;

	/** 未実行(管理対象外) */
	public static final int TYPE_NOT_MANAGED = 309;

	/** 終了(ファイル出力失敗) (状態の種別) */
	public static final int TYPE_END_FAILED_OUTPUT = 310;

	/** 起動失敗(状態の種別) */
	public static final int TYPE_ERROR = 400;
	
	public static List<Integer> getStatusList() {
		return new ArrayList<>(Arrays.asList(
				TYPE_SCHEDULED
				, TYPE_WAIT
				, TYPE_RESERVING
				, TYPE_SKIP
				, TYPE_RUNNING
				, TYPE_STOPPING
				, TYPE_RUNNING_QUEUE
				, TYPE_SUSPEND
				, TYPE_STOP
				, TYPE_SUSPEND_QUEUE
				, TYPE_END
				, TYPE_MODIFIED
				, TYPE_END_UNMATCH
				, TYPE_END_CALENDAR
				, TYPE_END_SKIP
				, TYPE_END_START_DELAY
				, TYPE_END_END_DELAY
				, TYPE_END_EXCLUSIVE_BRANCH
				, TYPE_END_QUEUE_LIMIT
				, TYPE_END_FAILED_OUTPUT
				, TYPE_ERROR
				, TYPE_NOT_MANAGED
				));
	}

	/**
	 * 「終了」状態に属する場合は true を返す。
	 * @param type
	 * @return
	 */
	public static boolean isEndGroup(int type) {
		return (type / 100 == StatusConstant.TYPE_END / 100);
	}
	
	/**
	 * ジョブ操作の判定に使用される
	 * 
	 * 未実行(管理対象外)はジョブ操作を行わないため対象から外す。
	 * 
	 * @return 終了ステータス
	 */
	public static ArrayList<Integer> getEndList() {
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(StatusConstant.TYPE_END);
		list.add(StatusConstant.TYPE_MODIFIED);
		list.add(StatusConstant.TYPE_END_UNMATCH);
		list.add(StatusConstant.TYPE_END_CALENDAR);
		list.add(StatusConstant.TYPE_END_SKIP);
		list.add(StatusConstant.TYPE_END_START_DELAY);
		list.add(StatusConstant.TYPE_END_END_DELAY);
		list.add(StatusConstant.TYPE_END_QUEUE_LIMIT);
		list.add(StatusConstant.TYPE_END_FAILED_OUTPUT);
		return list;
	}
	
	public static ArrayList<Integer> getUnendList() {
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(StatusConstant.TYPE_WAIT);
		list.add(StatusConstant.TYPE_RESERVING);
		list.add(StatusConstant.TYPE_SKIP);
		list.add(StatusConstant.TYPE_RUNNING);
		list.add(StatusConstant.TYPE_STOPPING);
		list.add(StatusConstant.TYPE_RUNNING_QUEUE);
		list.add(StatusConstant.TYPE_SUSPEND);
		list.add(StatusConstant.TYPE_STOP);
		list.add(StatusConstant.TYPE_SUSPEND_QUEUE);
		list.add(StatusConstant.TYPE_ERROR);
		return list;
	}
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToMessageCode(int type) {
		if (type == TYPE_RUNNING) {
			return "RUNNING";
		} else if (type == TYPE_END) {
			return "END";
		} else if (type == TYPE_WAIT) {
			return "WAIT";
		} else if (type == TYPE_STOPPING) {
			return "STOPPING";
		} else if (type == TYPE_STOP) {
			return "STOP_AT_ONCE";
		} else if (type == TYPE_SCHEDULED) {
			return "SCHEDULED";
		} else if (type == TYPE_RESERVING) {
			return "RESERVING";
		} else if (type == TYPE_MODIFIED) {
			return "MODIFIED";
		} else if (type == TYPE_END_CALENDAR) {
			return "END_CALENDAR";
		} else if (type == TYPE_END_UNMATCH) {
			return "END_UNMATCH";
		} else if (type == TYPE_END_SKIP) {
			return "END_SKIP";
		} else if (type == TYPE_END_START_DELAY) {
			return "END_START_DELAY";
		} else if (type == TYPE_END_END_DELAY) {
			return "END_END_DELAY";
		} else if (type == TYPE_ERROR) {
			return "START_ERROR";
		} else if (type == TYPE_SKIP) {
			return "SKIP";
		} else if (type == TYPE_SUSPEND) {
			return "SUSPEND";
		} else if (type == TYPE_END_EXCLUSIVE_BRANCH) {
			return "END_EXCLUSIVE_BRANCH";
		} else if (type == TYPE_RUNNING_QUEUE) {
			return "RUNNING_QUEUE";
		} else if (type == TYPE_SUSPEND_QUEUE) {
			return "SUSPEND_QUEUE";
		} else if (type == TYPE_END_QUEUE_LIMIT) {
			return "END_QUEUE_LIMIT";
		} else if (type == TYPE_NOT_MANAGED) {
			return "NOT_MANAGED";
		} else if (type == TYPE_END_FAILED_OUTPUT) {
			return "END_FAILED_OUTPUT";
		}
		return "";
	}
}