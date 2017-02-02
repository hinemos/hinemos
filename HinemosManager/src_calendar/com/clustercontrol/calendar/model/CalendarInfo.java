package com.clustercontrol.calendar.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


/**
 * The persistent class for the cc_cal_info database table.
 * 
 */
@XmlType(namespace = "http://calendar.ws.clustercontrol.com")
@Entity
@Table(name="cc_cal_info", schema="setting")
@Cacheable(true)
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.PLATFORM_CALENDAR,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="calendar_id", insertable=false, updatable=false))
public class CalendarInfo extends ObjectPrivilegeTargetInfo {
	private static final long serialVersionUID = 1L;
	private String calendarId;
	private String calendarName;
	private String description;
	private Long regDate;
	private String regUser;
	private Long updateDate;
	private String updateUser;
	private Long validTimeFrom;
	private Long validTimeTo;
	private List<CalendarDetailInfo> calDetailInfoEntities = new ArrayList<>();

	public CalendarInfo() {
	}

	@Id
	@Column(name="calendar_id")
	public String getCalendarId() {
		return this.calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
		this.setObjectId(calendarId);
	}

	@Column(name="calendar_name")
	public String getCalendarName() {
		return this.calendarName;
	}

	public void setCalendarName(String calendarName) {
		this.calendarName = calendarName;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name="reg_date")
	public Long getRegDate() {
		return this.regDate;
	}

	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}

	@Column(name="reg_user")
	public String getRegUser() {
		return this.regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	@Column(name="update_date")
	public Long getUpdateDate() {
		return this.updateDate;
	}

	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}

	@Column(name="update_user")
	public String getUpdateUser() {
		return this.updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	@Column(name="valid_time_from")
	public Long getValidTimeFrom() {
		return this.validTimeFrom;
	}

	public void setValidTimeFrom(Long validTimeFrom) {
		this.validTimeFrom = validTimeFrom;
	}

	@Column(name="valid_time_to")
	public Long getValidTimeTo() {
		return this.validTimeTo;
	}

	public void setValidTimeTo(Long validTimeTo) {
		this.validTimeTo = validTimeTo;
	}

	/**
	 * CalDetailInfoEntity削除<BR>
	 * 
	 * 指定されたPK以外の子Entityを削除する。
	 * 
	 */
	public void deleteCalDetailInfoEntities(List<CalendarDetailInfoPK> notDelPkList) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CalendarDetailInfo> list = this.getCalendarDetailList();
		Iterator<CalendarDetailInfo> iter = list.iterator();
		while(iter.hasNext()) {
			CalendarDetailInfo entity = iter.next();
			if (!notDelPkList.contains(entity.getId())) {
				iter.remove();
				em.remove(entity);
			}
		}
	}

	//bi-directional many-to-one association to CalDetailInfoEntity
	@OneToMany(mappedBy="calInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<CalendarDetailInfo> getCalendarDetailList() {
		return this.calDetailInfoEntities;
	}

	public void setCalendarDetailList(List<CalendarDetailInfo> calDetailInfoEntities) {
		if (calDetailInfoEntities != null && calDetailInfoEntities.size() > 0) {
			Collections.sort(calDetailInfoEntities, new Comparator<CalendarDetailInfo>() {
				@Override
				public int compare(CalendarDetailInfo o1, CalendarDetailInfo o2) {
					return o1.getId().getOrderNo().compareTo(o2.getId().getOrderNo());
				}
			});
		}
		this.calDetailInfoEntities = calDetailInfoEntities;
	}

}