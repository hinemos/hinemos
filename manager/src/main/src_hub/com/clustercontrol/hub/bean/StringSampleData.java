/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

/**
 * 監視結果を性能値を保持するクラス<BR>
 * 
 * @version 4.0.0
 * @since 4.0.0
 */
@XmlType(namespace = "http://collect.ws.clustercontrol.com")
public class StringSampleData implements Serializable {

	private static final long serialVersionUID = -9053625437686726677L;

	private String facilityId = null;
	private String targetName = null;
	private String value = null;
	private List<StringSampleTag> tagList = null;

	/**
	 * コンストラクタ
	 * @param facilityId
	 * @param itemName
	 * @param value
	 * @param errorType
	 * @param displayName
	 */
	public StringSampleData(String facilityId, String targetName, String value, List<StringSampleTag> tagList) {
		super();

		this.facilityId = facilityId;
		this.targetName = targetName;
		this.value = value;
		this.tagList = new ArrayList<>(tagList);
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
	public String getTargetName() {
		return targetName;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * 文字情報に紐づくタグ
	 * @return
	 */
	public List<StringSampleTag> getTagList() {
		return tagList;
	}
}
