/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlType;

/**
 * 
 * ジョブ連携メッセージ一覧情報を保持するクラス<BR>
 * 
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobLinkMessageList implements java.io.Serializable {

	private static final long serialVersionUID = -194434869219746197L;

	// 合計数
	private Integer total = 0;

	// ジョブ連携メッセージ一覧
	private ArrayList<JobLinkMessageInfo> list;

	public ArrayList<JobLinkMessageInfo> getList() {
		return list;
	}

	public void setList(ArrayList<JobLinkMessageInfo> list) {
		this.list = list;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

}