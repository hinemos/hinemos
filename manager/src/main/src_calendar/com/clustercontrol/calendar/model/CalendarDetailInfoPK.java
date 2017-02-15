package com.clustercontrol.calendar.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

/**
 * The primary key class for the cc_cal_detail_info database table.
 * 
 */
@Embeddable
public class CalendarDetailInfoPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String calendarId;
	private Integer orderNo;

	public CalendarDetailInfoPK() {
	}

	public CalendarDetailInfoPK(String calendarId, Integer orderNo) {
		this.setCalendarId(calendarId);
		this.setOrderNo(orderNo);
	}

	@Column(name="calendar_id")
	public String getCalendarId() {
		return this.calendarId;
	}
	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	@Column(name="order_no")
	public Integer getOrderNo(){
		return this.orderNo;
	}
	public void setOrderNo(Integer orderNo){
		this.orderNo = orderNo;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof CalendarDetailInfoPK)) {
			return false;
		}
		CalendarDetailInfoPK castOther = (CalendarDetailInfoPK)other;
		return
				this.calendarId.equals(castOther.calendarId)
				&& this.orderNo.equals(castOther.orderNo);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.calendarId.hashCode();
		hash = hash * prime + this.orderNo.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"calendarId",
				"orderNo"
		};
		String[] values = {
				this.calendarId,
				this.orderNo.toString()
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}