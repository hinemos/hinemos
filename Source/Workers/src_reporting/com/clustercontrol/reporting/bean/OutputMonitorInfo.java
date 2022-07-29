/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * グラフ出力する監視設定を保持するクラス
 * 
 * @version 1.1.0
 * @since 1.1.0
 */
public class OutputMonitorInfo implements java.io.Serializable {

	private static final long serialVersionUID = -614226620255135763L;
	
	private String monitorId;
	private String itemName;
	private String measure;
	private List<String> displayName = new LinkedList<String>();
	private int runInterval;
	private boolean deviceFlg;
	private double maxValue;
	
	public String getMonitorId() {
		return monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}
	
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	
	public String getMeasure() {
		return measure;
	}
	public void setMeasure(String measure) {
		this.measure = measure;
	}
	
	public List<String> getDisplayName() {
		return displayName;
	}
	public void setDisplayName(List<String> displayName) {
		this.displayName = displayName;
	}
	
	public int getRunInterval() {
		return runInterval;
	}
	public void setRunInterval(int runInterval) {
		this.runInterval = runInterval;
	}
	
	public boolean isDeviceFlg() {
		return deviceFlg;
	}
	public void setDeviceFlg(boolean deviceFlg) {
		this.deviceFlg = deviceFlg;
	}
	
	public double getMaxValue() {
		return maxValue;
	}
	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	/**
	 * 監視設定情報のコレクションを、監視間隔の短 -> 長 の順番にソートして返します。
	 */
	public static List<OutputMonitorInfo> sortByRunInterval(Collection<OutputMonitorInfo> monitorInfos) {
		List<OutputMonitorInfo> sorted = new ArrayList<>(monitorInfos);
		Collections.sort(sorted, new Comparator<OutputMonitorInfo>() {
			@Override
			public int compare(OutputMonitorInfo o1, OutputMonitorInfo o2) {
				// o1 の監視間隔が o2 より短い場合は、負の値(＝o1 は o2 より小さい)
				int cmp = o1.getRunInterval() - o2.getRunInterval();
				if (cmp != 0) return cmp;
				// 監視間隔が同じなら 監視項目 ID 昇順
				return o1.getMonitorId().compareTo(o2.getMonitorId());
			}
		});
		return sorted;
	}

}
