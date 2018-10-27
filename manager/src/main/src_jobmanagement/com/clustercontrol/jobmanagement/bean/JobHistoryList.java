/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
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
 * ジョブ履歴の一覧情報を保持するクラス<BR>
 * session beanからこのオブジェクトが渡される。
 * 
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobHistoryList implements java.io.Serializable{

	private static final long serialVersionUID = 3682607391287154127L;

	private Integer total = 0;			//合計数

	/**
	 *
	 * イベント・ステータスの一覧
	 * 
	 * 
	 */
	private ArrayList<JobHistory> list;


	/**
	 * @return
	 */
	public ArrayList<JobHistory> getList() {
		return list;
	}

	/**
	 * @param eventList
	 */
	public void setList(ArrayList<JobHistory> list) {
		this.list = list;
	}


	/**
	 * @return
	 */
	public Integer getTotal() {
		return total;
	}

	/**
	 * @param total
	 */
	public void setTotal(Integer total) {
		this.total = total;
	}

}