/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 */
@XmlType(namespace = "http://calendar.ws.clustercontrol.com")
public class CalendarPatternInfo  implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6008008398376401587L;

	private static Log m_log = LogFactory.getLog( CalendarPatternInfo.class );

	/** ID*/
	private String id;
	/** 名前*/
	private String name;
	/** オーナーロールID*/
	private String ownerRoleId;
	/** 登録日*/
	private Long reg_date;
	/** 更新日*/
	private Long update_date;
	/** 登録ユーザ*/
	private String reg_user;
	/** 最新更新ユーザ*/
	private String update_user;
	/** 例外日*/
	private List<YMD> dateList = new ArrayList<YMD>();

	/**
	 * IDを返す<BR>
	 * @return
	 */
	public String getId() {
		return id;
	}
	/**
	 * IDを取得する<BR>
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * 名を返す<BR>
	 * @return
	 */
	public String getName() {
		return name;
	}
	/**
	 * 名を取得する<BR>
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * オーナーロールIDを返す<BR>
	 * @return
	 */
	public String getOwnerRoleId() {
		return ownerRoleId;
	}
	/**
	 * オーナーロールIDを取得する<BR>
	 * @param ownerRoleId
	 */
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}
	/**
	 * 登録ユーザを返す<BR>
	 * @return
	 */
	public Long getRegDate() {
		return reg_date;
	}
	/**
	 * 登録ユーザを取得する<BR>
	 * @param reg_date
	 */
	public void setRegDate(Long reg_date) {
		this.reg_date = reg_date;
	}
	/**
	 * 更新日を返す<BR>
	 * @return
	 */
	public Long getUpdateDate() {
		return update_date;
	}
	/**
	 * 更新日を取得する<BR>
	 * @param update_date
	 */
	public void setUpdateDate(Long update_date) {
		this.update_date = update_date;
	}
	/**
	 * 登録ユーザを返す
	 * @return
	 */
	public String getRegUser() {
		return reg_user;
	}
	/**
	 * 登録ユーザを取得する<BR>
	 * @param reg_user
	 */
	public void setRegUser(String reg_user) {
		this.reg_user = reg_user;
	}
	/**
	 * 最終更新ユーザを返す<BR>
	 * @return
	 */
	public String getUpdateUser() {
		return update_user;
	}
	/**
	 * 最終更新ユーザを取得する<BR>
	 * @param update_user
	 */
	public void setUpdateUser(String update_user) {
		this.update_user = update_user;
	}

	public List<YMD> getYmd() {
		return dateList;
	}

	public void setYmd(List<YMD> ymd){
		this.dateList = ymd;
	}

	public boolean isRun(int year, int month, int day) {
		YMD ymd = new YMD();
		ymd.setYear(year);
		ymd.setMonth(month);
		ymd.setDay(day);

		m_log.trace("isRun() : contains =" + dateList.contains(ymd));

		return dateList.contains(ymd);
	}

	/**
	 * 
	 */
	@Override
	public String toString(){
		String ret = "CalendarPatternInfo : ";
		ret = ret + "id =" + id;
		ret = ret + "name =" + name;
		ret = ret + "dateList =" + dateList;
		return ret;
	}
}
