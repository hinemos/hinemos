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

package com.clustercontrol.bean;

import java.util.ArrayList;


/**
 * 実行状態の定数クラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class StatusConstant {
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

	/** 中断(状態の種別) */
	public static final int TYPE_SUSPEND = 200;

	/** コマンド停止(状態の種別) */
	public static final int TYPE_STOP = 201;

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

	/** 起動失敗(状態の種別) */
	public static final int TYPE_ERROR = 400;
	
	/**
	 * TYPE_END(300), TYPE_MODIFY(301),
	 * TYPE_END_UNMATCH(302), TYPE_END_CALENDAR(303), TYPE_END_SKIP(304),
	 * TYPE_END_START_DELAY(305), TYPE_END_END_DELAY(306)
	 * の場合はtrueを返す。
	 * @param type
	 * @return
	 */
	public static boolean isEndGroup(int type) {
		return (type / 100 == StatusConstant.TYPE_END / 100);
	}
	
	public static ArrayList<Integer> getEndList() {
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(StatusConstant.TYPE_END);
		list.add(StatusConstant.TYPE_MODIFIED);
		list.add(StatusConstant.TYPE_END_UNMATCH);
		list.add(StatusConstant.TYPE_END_CALENDAR);
		list.add(StatusConstant.TYPE_END_SKIP);
		list.add(StatusConstant.TYPE_END_START_DELAY);
		list.add(StatusConstant.TYPE_END_END_DELAY);
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
		}
		return "";
	}
}