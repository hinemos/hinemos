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

package com.clustercontrol.monitor.bean;

import java.util.ArrayList;

/**
 * 
 * イベント一覧情報を保持するクラス<BR>
 * イベント一覧は、それぞれ下記のオブジェクトが格納されたArrayListである<BR>
 * 仮想化操作の一覧情報も保持する<BR>
 * 
 * 
 * @version 3.0
 * @since 2.1.1
 */
public class ViewListInfo implements java.io.Serializable{

	private static final long serialVersionUID = 859993588207224113L;

	private Long fromOutputDate = null;
	private Long toOutputDate = null;
	
	private Integer critical = 0;			//重要度:危険数
	private Integer warning = 0;			//重要度:警告数
	private Integer info = 0;				//重要度:通知数
	private Integer unKnown = 0;			//重要度:不明数
	private Integer total = 0;			//合計数

	/**
	 *
	 * イベント一覧
	 * 
	 * @see com.clustercontrol.monitor.bean.EventDataInfo
	 * 
	 */
	private ArrayList<EventDataInfo> eventList;


	/**
	 * コンストラクタ。
	 */
	public ViewListInfo() {
		super();
	}

	/**
	 * @return the fromOutputDate
	 */
	public Long getFromOutputDate() {
		return fromOutputDate;
	}

	/**
	 * @param fromOutputDate the fromOutputDate to set
	 */
	public void setFromOutputDate(Long fromOutputDate) {
		this.fromOutputDate = fromOutputDate;
	}

	/**
	 * @return the toOutputDate
	 */
	public Long getToOutputDate() {
		return toOutputDate;
	}

	/**
	 * @param toOutputDate the toOutputDate to set
	 */
	public void setToOutputDate(Long toOutputDate) {
		this.toOutputDate = toOutputDate;
	}

	/**
	 * @return
	 */
	public Integer getCritical() {
		return critical;
	}

	/**
	 * @param critical
	 */
	public void setCritical(Integer critical) {
		this.critical = critical;
	}

	public ArrayList<EventDataInfo> getEventList() {
		return eventList;
	}

	public void setEventList(ArrayList<EventDataInfo> eventList) {
		this.eventList = eventList;
	}

	/**
	 * @return
	 */
	public Integer getInfo() {
		return info;
	}

	/**
	 * @param info
	 */
	public void setInfo(Integer info) {
		this.info = info;
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

	/**
	 * @return
	 */
	public Integer getUnKnown() {
		return unKnown;
	}

	/**
	 * @param unKnown
	 */
	public void setUnKnown(Integer unKnown) {
		this.unKnown = unKnown;
	}

	/**
	 * @return
	 */
	public Integer getWarning() {
		return warning;
	}

	/**
	 * @param warning
	 */
	public void setWarning(Integer warning) {
		this.warning = warning;
	}
}