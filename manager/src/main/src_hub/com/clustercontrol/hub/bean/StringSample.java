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
	private String facilityId = null;
	private String targetName = null;
	private String value = null;
	private ArrayList<StringSampleTag> tagList = new ArrayList<>();

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
	 * 性能値のセット
	 * @param facilityId 
	 * @param targetName 
	 * @param value 収集対象の文字列
	 */
	public void set(String facilityId, String targetName, String value){
		this.facilityId = facilityId;
		this.targetName = targetName;
		this.value = value;
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

	public String getFacilityId() {
		return facilityId;
	}

	public String getTargetName() {
		return targetName;
	}

	public String getValue(){
		return value;
	}

	/**
	 * 文字情報に紐づくタグ
	 * @return
	 */
	public List<StringSampleTag> getTagList() {
		if(tagList == null)
			tagList = new ArrayList<StringSampleTag>();
		
		return tagList;
	}
}
