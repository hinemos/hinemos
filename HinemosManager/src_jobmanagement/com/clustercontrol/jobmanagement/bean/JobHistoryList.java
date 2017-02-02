/*

 Copyright (C) 2010 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

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