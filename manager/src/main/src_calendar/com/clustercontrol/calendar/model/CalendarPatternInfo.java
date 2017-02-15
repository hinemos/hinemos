package com.clustercontrol.calendar.model;


import java.util.ArrayList;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


/**
 * The persistent class for the cc_cal_pattern_info database table.
 * 
 */
@XmlType(namespace = "http://calendar.ws.clustercontrol.com")
@Entity
@Table(name="cc_cal_pattern_info", schema="setting")
@Cacheable(true)
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.PLATFORM_CALENDAR_PATTERN,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="calendar_pattern_id", insertable=false, updatable=false))
public class CalendarPatternInfo extends ObjectPrivilegeTargetInfo {
	
	private static Log m_log = LogFactory.getLog( CalendarPatternInfo.class );
	private static final long serialVersionUID = 1L;
	private String calendarPatternId;
	private String calendarPatternName;
	private Long regDate;
	private String regUser;
	private Long updateDate;
	private String updateUser;
	private List<YMD> calPatternDetailInfoEntities = new ArrayList<>();

	@Deprecated
	public CalendarPatternInfo() {
	}

	public CalendarPatternInfo(String calPatternId) {
		this.setCalPatternId(calPatternId);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.setObjectId(this.getCalPatternId());
	}

	@Id
	@Column(name="calendar_pattern_id")
	public String getCalPatternId() {
		return this.calendarPatternId;
	}

	public void setCalPatternId(String calPatternId) {
		this.calendarPatternId = calPatternId;
	}

	@Column(name="calendar_pattern_name")
	public String getCalPatternName() {
		return this.calendarPatternName;
	}

	public void setCalPatternName(String calPatternName) {
		this.calendarPatternName = calPatternName;
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

	public boolean isRun(int year, int month, int day) {
		boolean match = false;
		for (YMD ymd: calPatternDetailInfoEntities) {
			if (ymd.getYear() == year &&
				ymd.getMonth() == month &&
				ymd.getDay() == day) {
				match = true;
				break;
			}
		}

		if (m_log.isTraceEnabled()) {
			m_log.trace("isRun() : contains(" + calPatternDetailInfoEntities.size() + ")=" + match +
					" [" + year + "," + month + "," + day + "]");
		}

		return match;
	}
	
	//bi-directional many-to-one association to CalPatternDetailInfoEntity
	@OneToMany(mappedBy="calPatternInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<YMD> getYmd() {
		return this.calPatternDetailInfoEntities;
	}

	public void setYmd(List<YMD> calPatternDetailInfoEntities) {
		this.calPatternDetailInfoEntities = calPatternDetailInfoEntities;
	}


	/**
	 * CalDetailInfoEntity削除<BR>
	 * 
	 * 指定されたPK以外の子Entityを削除する。
	 * 
	 */
	public void deleteCalPatternDetailInfoEntities(List<YMDPK> notDelPkList) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<YMD> list = this.getYmd();
		Iterator<YMD> iter = list.iterator();
		while(iter.hasNext()) {
			YMD entity = iter.next();
			if (!notDelPkList.contains(entity.getId())) {
				iter.remove();
				em.remove(entity);
			}
		}
	}

}