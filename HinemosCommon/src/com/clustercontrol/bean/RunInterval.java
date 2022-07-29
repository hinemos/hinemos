/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.clustercontrol.util.Messages;

/**
 * 定義する値は監視の実装の制約により、次の3つの条件を満たす必要がある
 * 
 * - 1以上、3600以下であること
 * - その値で3600を割り切れること
 * - その値を、最小の値で割り切れること
 *   
 */
public enum RunInterval {
	
	TYPE_SEC_30(30,   30 + Messages.getString("second")),
	TYPE_MIN_01(60,   1 + Messages.getString("minute")),
	TYPE_MIN_05(300,  5 + Messages.getString("minute")),
	TYPE_MIN_10(600,  10 + Messages.getString("minute")),
	TYPE_MIN_30(1800, 30 + Messages.getString("minute")),
	TYPE_MIN_60(3600, 60 + Messages.getString("minute"));

	/** Enum判定用のName */
	private static final String ENUM_NAME_NONE = "NONE";
	private static final String ENUM_NAME_SEC_30 = "SEC_30";
	private static final String ENUM_NAME_MIN_01 = "MIN_01";
	private static final String ENUM_NAME_MIN_05 = "MIN_05";
	private static final String ENUM_NAME_MIN_10 = "MIN_10";
	private static final String ENUM_NAME_MIN_30 = "MIN_30";
	private static final String ENUM_NAME_MIN_60 = "MIN_60";

	private final int intervalSec;
	private final String readableString;
	private RunInterval(int _intervalSec, String _readableSuffix) {
		this.intervalSec = _intervalSec;
		this.readableString = _readableSuffix;
	}
	public int toSec() {
		return intervalSec;
	}
	
	private static final int max;
	/**
	 * 最大の監視間隔（秒）を返す
	 */
	public static int max() {
		return max;
	}
	private static final int min;
	/**
	 * 最小の監視間隔（秒）を返す
	 */
	public static int min() {
		return min;
	}
	private static final List<Integer> intValues;
	/**
	 * 全ての監視間隔（秒）を返す
	 * @return
	 */
	public static Collection<Integer> intValues() {
		return intValues;
	}
	
	public static RunInterval valueOf(int sec) {
		for (RunInterval val : values()) {
			if (val.intervalSec == sec ) {
				return val;
			}
		}
		return null;
	}
	
	/**
	 * "1分" などの可読文字列からRunIntervalを返す
	 */
	public static RunInterval stringToType(String readableString) {
		for (RunInterval val : values()) {
			if (val.readableString.equals(readableString)) {
				return val;
			}
		}
		return null;
	}

	/**
	 * Enumから文字列に変換します。<BR>
	 * ※Enumの型は引数で指定できますが、列挙子のNameは統一されている必要があります。<BR>
	 * 
	 * @param value 変換するEnum
	 * @param enumType Enumの型
	 * @return 文字列
	 */
	public static <T extends Enum<T>> String enumToString(T value, Class<T> enumType) {
		String name = value.name();

		if (name.equals(ENUM_NAME_NONE)) {
			return "-";
		} else if (name.equals(ENUM_NAME_SEC_30)) {
			return TYPE_SEC_30.toString();
		} else if (name.equals(ENUM_NAME_MIN_01)) {
			return TYPE_MIN_01.toString();
		} else if (name.equals(ENUM_NAME_MIN_05)) {
			return TYPE_MIN_05.toString();
		} else if (name.equals(ENUM_NAME_MIN_10)) {
			return TYPE_MIN_10.toString();
		} else if (name.equals(ENUM_NAME_MIN_30)) {
			return TYPE_MIN_30.toString();
		} else if (name.equals(ENUM_NAME_MIN_60)) {
			return TYPE_MIN_60.toString();
		}
		return "";
	}

	/**
	 * "1分" などの可読文字列を返す
	 */
	public String toString() {
		return readableString;
	}
	
	static {
		int _max = 0;
		int _min = Integer.MAX_VALUE;
		for (RunInterval val : values()) {
			int current = val.toSec();
			if (_max < current) {
				_max = current;
			}
			if (current < _min) {
				_min = current;
			}
		}
		max = _max;
		min = _min;
		final List<Integer> _intValues = new ArrayList<Integer>();
		for (RunInterval interval : values()) {
			_intValues.add(interval.toSec());
		}
		intValues = Collections.unmodifiableList(new CopyOnWriteArrayList<Integer>(_intValues));
	}
}
