/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

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
public enum NodeConfigRunInterval {
	
	TYPE_HOUR_6(60 * 60 * 6,   6 + Messages.getString("hour.period")),
	TYPE_HOUR_12(60 * 60 * 12,  12 + Messages.getString("hour.period")),
	TYPE_HOUR_24(60 * 60 * 24,  24 + Messages.getString("hour.period"));
	
	private final int intervalSec;
	private final String readableString;
	private NodeConfigRunInterval(int _intervalSec, String _readableSuffix) {
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
	
	public static NodeConfigRunInterval valueOf(int sec) {
		for (NodeConfigRunInterval val : values()) {
			if (val.intervalSec == sec ) {
				return val;
			}
		}
		return null;
	}
	
	/**
	 * "1分" などの可読文字列からRunIntervalを返す
	 */
	public static NodeConfigRunInterval stringToType(String readableString) {
		for (NodeConfigRunInterval val : values()) {
			if (val.readableString.equals(readableString)) {
				return val;
			}
		}
		return null;
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
		for (NodeConfigRunInterval val : values()) {
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
		for (NodeConfigRunInterval interval : values()) {
			_intValues.add(interval.toSec());
		}
		intValues = Collections.unmodifiableList(new CopyOnWriteArrayList<Integer>(_intValues));
	}
}
