/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.model;


import java.io.Serializable;
import java.util.Arrays;

import jakarta.persistence.*;

/**
 * The primary key class for the cc_cal_pattern_detail_info database table.
 * 
 */
@Embeddable
public class YMDPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String calPatternId;
	private Integer yearNo;
	private Integer monthNo;
	private Integer dayNo;

	public YMDPK() {
	}

	public YMDPK(String calPatternId,
			Integer yearNo, Integer monthNo, Integer dayNo) {
		this.setCalPatternId(calPatternId);
		this.setYear(yearNo);
		this.setMonth(monthNo);
		this.setDay(dayNo);
	}

	@Column(name="calendar_pattern_id")
	public String getCalPatternId() {
		return this.calPatternId;
	}
	public void setCalPatternId(String calPatternId) {
		this.calPatternId = calPatternId;
	}

	@Column(name="year_no")
	public Integer getYear(){
		return this.yearNo;
	}
	public void setYear(Integer yearNo){
		this.yearNo = yearNo;
	}

	@Column(name="month_no")
	public Integer getMonth(){
		return this.monthNo;
	}
	public void setMonth(Integer monthNo){
		this.monthNo = monthNo;
	}

	@Column(name="day_no")
	public Integer getDay(){
		return this.dayNo;
	}
	public void setDay(Integer dayNo){
		this.dayNo = dayNo;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof YMDPK)) {
			return false;
		}
		YMDPK castOther = (YMDPK)other;
		return
				this.calPatternId.equals(castOther.calPatternId)
				&& this.yearNo.equals(castOther.yearNo)
				&& this.monthNo.equals(castOther.monthNo)
				&& this.dayNo.equals(castOther.dayNo);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.calPatternId.hashCode();
		hash = hash * prime + this.yearNo.hashCode();
		hash = hash * prime + this.monthNo.hashCode();
		hash = hash * prime + this.dayNo.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"calendarId",
				"yearNo",
				"monthNo",
				"dayNo"
		};
		String[] values = {
				this.calPatternId,
				this.yearNo.toString(),
				this.monthNo.toString(),
				this.dayNo.toString()
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}