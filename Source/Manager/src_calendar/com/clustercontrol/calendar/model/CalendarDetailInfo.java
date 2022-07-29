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
import java.util.Iterator;
import java.util.List;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import com.clustercontrol.calendar.util.QueryUtil;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.InvalidRole;

/**
 * The persistent class for the cc_cal_detail_info database table.
 * 
 */
@XmlType(namespace = "http://calendar.ws.clustercontrol.com")
@Entity
@Table(name="cc_cal_detail_info", schema="setting")
@Cacheable(true)
public class CalendarDetailInfo implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	private CalendarDetailInfoPK id;
	private String description;
	private Integer yearNo;
	private Integer monthNo;
	private Integer dayType;
	private Integer weekNo;
	private Integer weekXth;
	private Integer dayNo;
	private String calPatternId;
	private Integer afterDay;
	private Boolean substituteFlg;
	private Integer substituteTime;
	private Integer substituteLimit;
	private Long startTime;
	private Long endTime;
	private Boolean executeFlg;
	private CalendarInfo calInfoEntity;

	private CalendarPatternInfo calPatternInfo;

	public CalendarDetailInfo() {
	}

	public CalendarDetailInfo(CalendarDetailInfoPK pk) {
		this.setId(pk);
	}

	public CalendarDetailInfo(String calendarId, Integer orderNo) {
		this(new CalendarDetailInfoPK(calendarId, orderNo));
	}

	@XmlTransient
	@EmbeddedId
	public CalendarDetailInfoPK getId() {
		if (id == null)
			id = new CalendarDetailInfoPK();
		return this.id;
	}

	public void setId(CalendarDetailInfoPK id) {
		this.id = id;
	}

	@Transient
	public String getCalendarId() {
		return getId().getCalendarId();
	}
	public void setCalendarId(String calendarId) {
		getId().setCalendarId(calendarId);
	}

	@XmlTransient
	@Transient
	public Integer getOrderNo(){
		return getId().getOrderNo();
	}
	public void setOrderNo(Integer orderNo){
		getId().setOrderNo(orderNo);
	}
	
	@Column(name="description")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	@Column(name="year_no")
	public Integer getYear() {
		return yearNo;
	}
	public void setYear(Integer yearNo) {
		this.yearNo = yearNo;
	}

	@Column(name="month_no")
	public Integer getMonth() {
		return monthNo;
	}
	public void setMonth(Integer monthNo) {
		this.monthNo = monthNo;
	}

	@Column(name="day_type")
	public Integer getDayType() {
		return dayType;
	}
	public void setDayType(Integer dayType) {
		this.dayType = dayType;
	}

	@Column(name="week_no")
	public Integer getDayOfWeek() {
		return weekNo;
	}
	public void setDayOfWeek(Integer weekNo) {
		this.weekNo = weekNo;
	}

	@Column(name="week_xth")
	public Integer getDayOfWeekInMonth() {
		return weekXth;
	}
	public void setDayOfWeekInMonth(Integer weekXth) {
		this.weekXth = weekXth;
	}

	@Column(name="day_no")
	public Integer getDate() {
		return dayNo;
	}
	public void setDate(Integer dayNo) {
		this.dayNo = dayNo;
	}

	@Column(name="calendar_pattern_id")
	public String getCalPatternId() {
		if (calPatternId == null && calPatternInfo != null)
			calPatternId = calPatternInfo.getCalPatternId();
		return calPatternId;
	}
	public void setCalPatternId(String calPatternId) {
		this.calPatternId = calPatternId;
	}

	@Transient
	public CalendarPatternInfo getCalPatternInfo() {
		if (calPatternInfo == null && calPatternId != null) {
			try {
				calPatternInfo = QueryUtil.getCalPatternInfoPK(getCalPatternId());
			} catch (CalendarNotFound | InvalidRole e) {
				Logger.getLogger(this.getClass()).debug(e.getMessage(), e);
			}
		}
		return calPatternInfo;
	}
	
	public void setCalPatternInfo(CalendarPatternInfo calPatternInfo) {
		this.calPatternInfo = calPatternInfo;
	}
	
	@Column(name="after_day")
	public Integer getAfterday() {
		return afterDay;
	}
	public void setAfterday(Integer afterDay) {
		this.afterDay = afterDay;
	}
	
	@Column(name="substitute_flg")
	public Boolean isSubstituteFlg() {
		return substituteFlg;
	}
	public void setSubstituteFlg(Boolean substituteFlg) {
		this.substituteFlg = substituteFlg;
	}
	
	@Column(name="substitute_time")
	public Integer getSubstituteTime() {
		return substituteTime;
	}
	public void setSubstituteTime(Integer substituteTime) {
		this.substituteTime = substituteTime;
	}
	
	@Column(name="substitute_limit")
	public Integer getSubstituteLimit() {
		return substituteLimit;
	}
	public void setSubstituteLimit(Integer substituteLimit) {
		this.substituteLimit = substituteLimit;
	}

	@Column(name="start_time")
	public Long getTimeFrom() {
		return startTime;
	}
	public void setTimeFrom(Long startTime) {
		this.startTime = startTime;
	}

	@Column(name="end_time")
	public Long getTimeTo() {
		return endTime;
	}
	public void setTimeTo(Long endTime) {
		this.endTime = endTime;
	}

	@Column(name="execute_flg")
	public Boolean isOperateFlg() {
		return this.executeFlg;
	}
	public void setOperateFlg(Boolean executeFlg) {
		this.executeFlg = executeFlg;
	}
	
	//bi-directional many-to-one association to CalInfoEntity
	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="calendar_id", insertable=false, updatable=false)
	public CalendarInfo getCalInfoEntity() {
		return this.calInfoEntity;
	}

	@Deprecated
	public void setCalInfoEntity(CalendarInfo calInfoEntity) {
		this.calInfoEntity = calInfoEntity;
	}

	/**
	 * CalInfoEntityオブジェクト参照設定<BR>
	 * 
	 * CalInfoEntity設定時はSetterに代わりこちらを使用すること。
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void relateToCalInfoEntity(CalendarInfo calInfoEntity) {
		this.setCalInfoEntity(calInfoEntity);
		if (calInfoEntity != null) {
			List<CalendarDetailInfo> list = calInfoEntity.getCalendarDetailList();
			if (list == null) {
				list = new ArrayList<CalendarDetailInfo>();
			} else {
				for(CalendarDetailInfo entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			calInfoEntity.setCalendarDetailList(list);
		}
	}


	/**
	 * 削除前処理<BR>
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void unchain() {

		// CalInfoEntity
		if (this.calInfoEntity != null) {
			List<CalendarDetailInfo> list = this.calInfoEntity.getCalendarDetailList();
			if (list != null) {
				Iterator<CalendarDetailInfo> iter = list.iterator();
				while(iter.hasNext()) {
					CalendarDetailInfo entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}
	
	@Override
	public CalendarDetailInfo clone() {
		try {
			CalendarDetailInfo newInfo = (CalendarDetailInfo)super.clone();
			newInfo.setDescription(this.description);
			newInfo.setYear(this.yearNo);
			newInfo.setMonth(this.monthNo);
			newInfo.setDayType(this.dayType);
			newInfo.setDayOfWeekInMonth(this.weekXth);
			newInfo.setDayOfWeek(this.weekNo);
			newInfo.setDate(this.dayNo);
			newInfo.setCalPatternId(this.calPatternId);
			newInfo.setCalPatternInfo(this.calPatternInfo);
			newInfo.setAfterday(this.afterDay);
			newInfo.setSubstituteFlg(this.substituteFlg);
			newInfo.setSubstituteTime(this.substituteTime);
			newInfo.setSubstituteLimit(this.substituteLimit);
			newInfo.setTimeFrom(this.startTime);
			newInfo.setTimeTo(this.endTime);
			newInfo.setOperateFlg(this.executeFlg);
			return newInfo;
		} catch(CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (id != null) {
			sb.append("id.calendarId = " + this.id.getCalendarId());
			sb.append(", orderNo =" + this.id.getOrderNo());
		}
		sb.append(", yearNo = " + this.yearNo);
		sb.append(", monthNo = " + this.monthNo);
		sb.append(", dayType = " + this.dayType);
		sb.append(", weekXth = " + this.weekXth);
		sb.append(", weekNo = " + this.weekNo);
		sb.append(", dayNo = " + this.dayNo);
		sb.append(", calPatternId = " + this.calPatternId);
		sb.append(", calPatternInfo = " + this.calPatternInfo);
		sb.append(", afterDay = " + this.afterDay);
		sb.append(", substituteFlg = " + this.substituteFlg);
		sb.append(", substituteTime = " + this.substituteTime);
		sb.append(", substituteLimit = " + this.substituteLimit);
		sb.append(", startTime = " + this.startTime);
		sb.append(", endTime = " + this.endTime);
		sb.append(", executeFlg = " + this.executeFlg);
		return sb.toString(); 
	}
}