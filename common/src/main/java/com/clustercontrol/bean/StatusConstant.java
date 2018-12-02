/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.bean;

import java.util.ArrayList;


/**
 * 実行状態の定数クラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class StatusConstant {

	public enum Status{
		// 待機(状態の種別)
		TYPE_WAIT(0, "WAIT"),
		// 保留中(状態の種別)
		TYPE_RESERVING(1, "RESERVING"),
		// スキップ(状態の種別)
		TYPE_SKIP(2, "SKIP"),
		// 実行中(状態の種別)
		TYPE_RUNNING(100, "RUNNING"),
		// 停止処理中(状態の種別)
		TYPE_STOPPING(101, "STOPPING"),
		// 中断(状態の種別)
		TYPE_SUSPEND(200, "SUSPEND"),
		// コマンド停止(状態の種別)
		TYPE_STOP(201, "STOP_AT_ONCE"),
		// 終了(状態の種別)
		TYPE_END(300, "END"),
		// 変更済(状態の種別)
		TYPE_MODIFIED(301, "MODIFIED"),
		// 終了(条件未達成) (状態の種別)
		TYPE_END_UNMATCH(302, "END_UNMATCH"),
		// 終了(カレンダ) (状態の種別)
		TYPE_END_CALENDAR(303, "END_CALENDAR"),
		// 終了(スキップ) (状態の種別)
		TYPE_END_SKIP(304, "END_SKIP"),
		// 終了(開始遅延) (状態の種別)
		TYPE_END_START_DELAY(305, "END_START_DELAY"),
		// 終了(終了遅延) (状態の種別)
		TYPE_END_END_DELAY(306, "END_END_DELAY"),

		// 終了(排他条件分岐) (状態の種別)
		TYPE_END_EXCLUSIVE_BRANCH(307, "END_EXCLUSIVE_BRANCH"),
		// 起動失敗(状態の種別)
		TYPE_ERROR(400, "START_ERROR");

		private int code;
		private String msg;

		private Status(int code, String msg) {
			this.code = code;
			this.msg = msg;
		}

		public int getCode() {
			return this.code;
		}
		public String getMsg() {
			return this.msg;
		}

		public static Status fromInt(int code) {
			for(Status status: Status.values()){
				if(status.getCode() == code) {
					return status;
				}
			}
			throw new IllegalArgumentException("Unknown code: " + code);
		}
	}

	/**
	 * @deprecated (Should use enum instead)
	 */
	@Deprecated public static final int TYPE_WAIT = Status.TYPE_WAIT.getCode();

	/**
	 * @deprecated (Should use enum instead)
	 */
	@Deprecated public static final int TYPE_RESERVING = Status.TYPE_RESERVING.getCode();

	/**
	 * @deprecated (Should use enum instead)
	 */
	@Deprecated public static final int TYPE_SKIP = Status.TYPE_SKIP.getCode();

	/**
	 * @deprecated (Should use enum instead)
	 */
	@Deprecated public static final int TYPE_RUNNING = Status.TYPE_RUNNING.getCode();

	/**
	 * @deprecated (Should use enum instead)
	 */
	@Deprecated public static final int TYPE_STOPPING = Status.TYPE_STOPPING.getCode();

	/**
	 * @deprecated (Should use enum instead)
	 */
	@Deprecated public static final int TYPE_SUSPEND = Status.TYPE_SUSPEND.getCode();

	/**
	 * @deprecated (Should use enum instead)
	 */
	@Deprecated public static final int TYPE_STOP = Status.TYPE_STOP.getCode();

	/**
	 * @deprecated (Should use enum instead)
	 */
	@Deprecated public static final int TYPE_END = Status.TYPE_END.getCode();

	/**
	 * @deprecated (Should use enum instead)
	 */
	@Deprecated public static final int TYPE_MODIFIED = Status.TYPE_MODIFIED.getCode();

	/**
	 * @deprecated (Should use enum instead)
	 */
	@Deprecated public static final int TYPE_END_UNMATCH = Status.TYPE_END_UNMATCH.getCode();

	/**
	 * @deprecated (Should use enum instead)
	 */
	@Deprecated public static final int TYPE_END_CALENDAR = Status.TYPE_END_CALENDAR.getCode();

	/**
	 * @deprecated (Should use enum instead)
	 */
	@Deprecated public static final int TYPE_END_SKIP = Status.TYPE_END_SKIP.getCode();

	/**
	 * @deprecated (Should use enum instead)
	 */
	@Deprecated public static final int TYPE_END_START_DELAY = Status.TYPE_END_START_DELAY.getCode();

	/**
	 * @deprecated (Should use enum instead)
	 */
	@Deprecated public static final int TYPE_END_END_DELAY = Status.TYPE_END_END_DELAY.getCode();
	
	/**
	 * @deprecated (Should use enum instead)
	 */
	@Deprecated public static final int TYPE_END_EXCLUSIVE_BRANCH = Status.TYPE_END_EXCLUSIVE_BRANCH.getCode();

	/**
	 * @deprecated (Should use enum instead)
	 */
	@Deprecated public static final int TYPE_ERROR = Status.TYPE_ERROR.getCode();

	/**
	 * TYPE_END(300), TYPE_MODIFY(301),
	 * TYPE_END_UNMATCH(302), TYPE_END_CALENDAR(303), TYPE_END_SKIP(304),
	 * TYPE_END_START_DELAY(305), TYPE_END_END_DELAY(306)
	 * の場合はtrueを返す。
	 * @param type
	 * @return
	 */
	public static boolean isEndGroup(int type) {
		return (type / 100 == Status.TYPE_END.getCode() / 100);
	}
	
	public static ArrayList<Integer> getEndList() {
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(Status.TYPE_END.getCode());
		list.add(Status.TYPE_MODIFIED.getCode());
		list.add(Status.TYPE_END_UNMATCH.getCode());
		list.add(Status.TYPE_END_CALENDAR.getCode());
		list.add(Status.TYPE_END_SKIP.getCode());
		list.add(Status.TYPE_END_START_DELAY.getCode());
		list.add(Status.TYPE_END_END_DELAY.getCode());
		return list;
	}
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToMessageCode(int type) {
		try {
			return Status.fromInt(type).getMsg();
		} catch(IllegalArgumentException e) {
			return "";
		}
	}

	private StatusConstant() {
		throw new IllegalStateException("ConstClass");
	}
}