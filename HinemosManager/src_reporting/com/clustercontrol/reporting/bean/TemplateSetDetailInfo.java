/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * レポーティング テンプレートセットの詳細情報のデータクラスです。
 * 
 * @version 5.0.a
 * @since 5.0.a
 *
 */
@XmlType(namespace = "http://reporting.ws.clustercontrol.com")
public class TemplateSetDetailInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	private String templateSetId = null;
	private Integer orderNo = Integer.valueOf(0);
	private String description = null;
	private String templateId = null;
	private String titleName = null;
	
	public TemplateSetDetailInfo() {
		super();
	}

	public String getTemplateSetId() {
		return templateSetId;
	}

	public void setTemplateSetId(String templateSetId) {
		this.templateSetId = templateSetId;
	}

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getTitleName() {
		return titleName;
	}

	public void setTitleName(String titleName) {
		this.titleName = titleName;
	}

}
