/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.hub.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 収集したログデータを保持するクラス<BR>
 * 
 * @version 6.0.0
 * @since 6.0.0
 */
public class StringSample implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8964972103033830851L;
	private Date dateTime = null;
	private String monitorId = null;
	private List<StringSampleData> stringSampleList = null;

	/**
	 * コンストラクタ
	 * @param dateTime
	 * @param monitorId
	 */
	public StringSample(Date dateTime, String monitorId) {
		super();
		this.dateTime = dateTime;
		this.monitorId = monitorId;
	}

	/**
	 * 文字列値 の追加
	 * @param facilityId 
	 * @param targetName 
	 * @param value 収集対象の文字列
	 */
	public void set(String facilityId, String targetName, String value){
		set(facilityId, targetName, value, Collections.emptyList());
	}

	/**
	 * 文字列値 の追加
	 * @param facilityId 
	 * @param targetName 
	 * @param value 収集対象の文字列
	 */
	public void set(String facilityId, String targetName, String value, List<StringSampleTag> tags){
		if(stringSampleList == null)
			stringSampleList = new ArrayList<>();

		stringSampleList.add(new StringSampleData(facilityId, targetName, value, tags));
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
	 * 文字列収集値のリストを取得
	 * @return
	 */
	public List<StringSampleData> getStringSampleList() {
		if(stringSampleList == null)
			stringSampleList = new ArrayList<>();

		return stringSampleList;
	}
}