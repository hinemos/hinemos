/*

Copyright (C) 2013 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.calendar.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.HinemosTime;

/**
 * 週カレンダビューを表示するため使用する年月日のDTOです。
 * @version 4.1.0
 * @since 4.1.0
 */
@XmlType(namespace = "http://calendar.ws.clustercontrol.com")
public class YMD implements Serializable,Comparable<YMD>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3862841781069057597L;
	private static Log m_log = LogFactory.getLog( YMD.class );
	private Integer year = -1;
	private Integer month = -1;
	private Integer day = -1;

	public Integer getYear() {
		return year;
	}
	public void setYear(Integer year) {
		this.year = year;
	}
	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public Integer getDay() {
		return day;
	}

	public void setDay(Integer day) {
		this.day = day;
	}
	/**
	 * 
	 */
	public YMD(){
	}
	/**
	 * コンストラクタ
	 * @param year
	 * @param month
	 * @param day
	 */
	public YMD(Integer year, Integer month, Integer day){
		this.year = year;
		this.month = month;
		this.day = day;
	}
	/**
	 * コンストラクタ
	 * Long型からYMDを作成する
	 * @param date
	 */
	public YMD(Long date){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		sdf.setTimeZone(HinemosTime.getTimeZone());
		String strYMD = sdf.format(date);
		String[] str = strYMD.split("/");
		this.year = Integer.parseInt(str[0]);
		this.month = Integer.parseInt(str[1]);
		this.day = Integer.parseInt(str[2]);
	}

	/**
	 * 年月日をyyyy/MM/dd形式で返す<BR>
	 * @return yyyy/MM/dd
	 */
	public String yyyyMMdd(){
		String ret = year + "/" + month + "/" + day;
		return ret;
	}

	/**
	 * 
	 */
	@Override
	public String toString() {
		return "YMD ["
				+ "year=" + year
				+ ", month=" + month
				+ ", day=" + day
				+ "]";
	}

	@Override
	public int compareTo(YMD o){
		m_log.debug(this.year + "/" + this.month + "/" + this.day);
		/**
		 * ex)
		 * year = 2013 * 10000
		 * month = 11 * 100
		 * day = 30
		 * int ymd = 20130000 + 1100 + 30 = 20131130
		 */
		int ymd = this.year * 10000 + this.month * 100 + this.day;
		int other = o.getYear() * 10000 + o.getMonth() * 100 + o.getDay();
		return ymd - other;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof YMD)) {
			return false;
		}
		YMD other = (YMD)o;
		if (!this.year.equals(other.year)) {
			return false;
		}
		if (!this.month.equals(other.month)) {
			return false;
		}
		if (!this.day.equals(other.day)) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((day == null) ? 0 : day.hashCode());
		result = prime * result + ((month == null) ? 0 : month.hashCode());
		result = prime * result + ((year == null) ? 0 : year.hashCode());
		return result;
	}
}