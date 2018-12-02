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
	TYPE_SEC_30(30),
	TYPE_MIN_01(60),
	TYPE_MIN_05(300),
	TYPE_MIN_10(600),
	TYPE_MIN_30(1800),
	TYPE_MIN_60(3600);
	
	private int intervalSec;
	private RunInterval(int _intervalSec) {
		this.intervalSec = _intervalSec;
	}

	public int toSec() {
		return intervalSec;
	}
	
	private static final int MAX;
	/**
	 * 最大の監視間隔（秒）を返す
	 */
	public static int max() {
		return MAX;
	}
	private static final int MIN;
	/**
	 * 最小の監視間隔（秒）を返す
	 */
	public static int min() {
		return MIN;
	}
	private static final List<Integer> INT_VALUES;
	/**
	 * 全ての監視間隔（秒）を返す
	 * @return
	 */
	public static Collection<Integer> intValues() {
		return new ArrayList<Integer>( INT_VALUES );
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
			if (val.toString().equals(readableString)) {
				return val;
			}
		}
		return null;
	}
	
	/**
	 * "1分" などの可読文字列を返す
	 */
	public String toString() {
		if(60 > this.intervalSec) {
			return this.intervalSec + Messages.getString("second");
		} else {
			return this.intervalSec/60 + Messages.getString("minute");
		}
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
		MAX = _max;
		MIN = _min;
		List<Integer> _intValues = new ArrayList<Integer>();
		for (RunInterval interval : values()) {
			_intValues.add(interval.toSec());
		}
		INT_VALUES = Collections.unmodifiableList(new CopyOnWriteArrayList<Integer>(_intValues));
	}
}
