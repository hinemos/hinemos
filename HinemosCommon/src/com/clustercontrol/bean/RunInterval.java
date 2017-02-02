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
	
	TYPE_MIN_01(60,   1 + Messages.getString("minute")),
	TYPE_MIN_05(300,  5 + Messages.getString("minute")),
	TYPE_MIN_10(600,  10 + Messages.getString("minute")),
	TYPE_MIN_30(1800, 30 + Messages.getString("minute")),
	TYPE_MIN_60(3600, 60 + Messages.getString("minute"));
	
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
