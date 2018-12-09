/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.collect.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * 監視結果を性能値を保持するクラス<BR>
 * 
 * @version 4.0.0
 * @since 4.0.0
 */
@XmlType(namespace = "http://collect.ws.clustercontrol.com")
public class PerfData implements Serializable {

	private static final long serialVersionUID = -9053625437686726677L;

	private final String facilityId;    //ファシリティID
	private final String itemName;      //収集表示名
	private final Double value;         //性能値
	private final Double average;         //平均値
	private final Double standardDeviation;  //標準偏差
	private final int errorType;        //値が不正な場合のエラーパターン
	private final String displayName;   //ディスプレイ名

	/**
	 * コンストラクタ
	 * @param facilityId
	 * @param itemName
	 * @param value
	 * @param average
	 * @param standardDeviation
	 * @param errorType
	 * @param displayName
	 */
	public PerfData(String facilityId, String itemName, Double value, Double average, 
			Double standardDeviation, int errorType, String displayName) {
		super();

		this.facilityId = facilityId;
		this.itemName = itemName;
		this.value = value;
		this.average = average;
		this.standardDeviation = standardDeviation;
		this.errorType = errorType;
		this.displayName = displayName;
	}

	/**
	 * 
	 * @return
	 */
	public String getItemName() {
		return itemName;
	}

	/**
	 * 
	 * @return
	 */
	public String getFacilityId() {
		return facilityId;
	}

	/**
	 * 
	 * @return
	 */
	public Double getValue() {
		return value;
	}

	/**
	 * 
	 * @return
	 */
	public Double getAverage() {
		return average;
	}


	/**
	 * 
	 * @return
	 */
	public Double getStandardDeviation() {
		return standardDeviation;
	}

	/**
	 * 
	 * @return
	 */
	public int getErrorType() {
		return errorType;
	}

	/**
	 * 
	 * @return
	 */
	public String getDisplayName() {
		return displayName;
	}

}
