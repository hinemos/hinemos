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
	private final int errorType;        //値が不正な場合のエラーパターン
	private final String displayName;   //ディスプレイ名

	/**
	 * コンストラクタ
	 * @param facilityId
	 * @param itemName
	 * @param value
	 * @param errorType
	 * @param displayName
	 */
	public PerfData(String facilityId, String itemName, Double value, int errorType, String displayName) {
		super();

		this.facilityId = facilityId;
		this.itemName = itemName;
		this.value = value;
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
