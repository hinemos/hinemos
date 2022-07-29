/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.collect.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.bind.annotation.XmlType;

/**
 * 監視結果を性能値のセットとして保持するクラス<BR>
 * 
 * @version 4.0.0
 * @since 4.0.0
 */
@XmlType(namespace = "http://collect.ws.clustercontrol.com")
public class Sample implements Serializable {

	private static final long serialVersionUID = 1457415635709692572L;

	private Date dateTime = null;
	private String monitorId = null;
	private ArrayList<PerfData> perfDataList = null;

	/**
	 * コンストラクタ
	 * @param dateTime
	 * @param collectorId
	 */
	public Sample(Date dateTime, String monitorId) {
		super();
		this.dateTime = dateTime;
		this.monitorId = monitorId;
	}

	/**
	 * 性能値のセット
	 * @param facilityId
	 * @param itemName
	 * @param value
	 * @param errorType	//値が不正な場合のエラーパターン
	 * @param displayName
	 */
	public void set(String facilityId, String itemName, Double value, Integer errorType, String displayName){

		if(perfDataList == null)
			perfDataList = new ArrayList<PerfData>();

		perfDataList.add(new PerfData(facilityId, itemName, value, null, null, errorType, displayName));
	}

	/**
	 * 性能値のセット
	 * @param facilityId
	 * @param itemName
	 * @param value
	 * @param average
	 * @param standardDeviation
	 * @param errorType	//値が不正な場合のエラーパターン
	 * @param displayName
	 */
	public void set(String facilityId, String itemName, Double value, Double average,
			 Double standardDeviation, Integer errorType, String displayName){

		if(perfDataList == null)
			perfDataList = new ArrayList<PerfData>();

		perfDataList.add(new PerfData(facilityId, itemName, value, average, standardDeviation, errorType, displayName));
	}

	/**
	 * 性能値のセット
	 * @param facilityId
	 * @param itemCode
	 * @param displayName
	 * @param value
	 * @param errorType	//値が不正な場合のエラーパターン
	 */
	public void set(String facilityId, String itemName, Double value, Integer errorType){

		if(perfDataList == null)
			perfDataList = new ArrayList<PerfData>();

		perfDataList.add(new PerfData(facilityId, itemName, value, null, null, errorType, ""));
	}

	/**
	 * 性能値のセット
	 * @param facilityId
	 * @param itemCode
	 * @param displayName
	 * @param value
	 * @param average
	 * @param standardDeviation
	 * @param errorType	//値が不正な場合のエラーパターン
	 */
	public void set(String facilityId, String itemName, Double value, Double average,
			 Double standardDeviation, Integer errorType){

		if(perfDataList == null)
			perfDataList = new ArrayList<PerfData>();

		perfDataList.add(new PerfData(facilityId, itemName, value, average, standardDeviation, errorType, ""));
	}

	/**
	 * 収集時刻
	 * @return
	 */
	public Date getDateTime() {
		return dateTime;
	}

	public String getMonitorId() {
		return monitorId;
	}

	/**
	 * 性能情報のリスト
	 * @return
	 */
	public ArrayList<PerfData> getPerfDataList() {
		if(perfDataList == null)
			perfDataList = new ArrayList<PerfData>();

		return perfDataList;
	}
}
