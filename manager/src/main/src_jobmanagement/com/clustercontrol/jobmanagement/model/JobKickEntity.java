package com.clustercontrol.jobmanagement.model;

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

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;



/**
 * The persistent class for the cc_job_kick database table.
 * 
 */
@Entity
@Table(name="cc_job_kick", schema="setting")
@Cacheable(true)
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.JOB_KICK,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="jobkick_id", insertable=false, updatable=false))
public class JobKickEntity extends ObjectPrivilegeTargetInfo {
	private static final long serialVersionUID = 1L;
	private String jobkickId;
	private String jobkickName;
	private Integer jobkickType;
	private String jobunitId;
	private String jobId;
	private String calendarId;
	private Integer scheduleType;
	private Integer hour;
	private Integer minute;
	private Integer week;
	private Integer fromXMinutes;
	private Integer everyXMinutes;
	private String facilityId;
	private String directory;
	private String fileName;
	private Integer eventType;
	private Integer modifyType;
	private Boolean validFlg;
	private Long regDate;
	private String regUser;
	private Long updateDate;
	private String updateUser;
	private List<JobRuntimeParamEntity> jobRuntimeParamEntities;

	@Deprecated
	public JobKickEntity() {
	}

	public JobKickEntity(String jobkickId) {
		this.setJobkickId(jobkickId);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.setObjectId(this.getJobkickId());
	}

	@Id
	@Column(name="jobkick_id")
	public String getJobkickId() {
		return this.jobkickId;
	}
	public void setJobkickId(String jobkickId) {
		this.jobkickId = jobkickId;
	}

	@Column(name="jobkick_name")
	public String getJobkickName() {
		return this.jobkickName;
	}
	public void setJobkickName(String jobkickName) {
		this.jobkickName = jobkickName;
	}

	@Column(name="jobkick_type")
	public Integer getJobkickType() {
		return jobkickType;
	}
	public void setJobkickType(Integer jobkickType) {
		this.jobkickType = jobkickType;
	}

	@Column(name="jobunit_id")
	public String getJobunitId() {
		return this.jobunitId;
	}
	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}

	@Column(name="job_id")
	public String getJobId() {
		return this.jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	@Column(name="calendar_id")
	public String getCalendarId() {
		return calendarId;
	}
	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	@Column(name="schedule_type")
	public Integer getScheduleType() {
		return this.scheduleType;
	}
	public void setScheduleType(Integer scheduleType) {
		this.scheduleType = scheduleType;
	}

	@Column(name="hour")
	public Integer getHour() {
		return this.hour;
	}
	public void setHour(Integer hour) {
		this.hour = hour;
	}

	@Column(name="minute")
	public Integer getMinute() {
		return this.minute;
	}
	public void setMinute(Integer minute) {
		this.minute = minute;
	}

	@Column(name="week")
	public Integer getWeek() {
		return this.week;
	}
	public void setWeek(Integer week) {
		this.week = week;
	}

	@Column(name="from_x_minutes")
	public Integer getFromXMinutes() {
		return this.fromXMinutes;
	}
	public void setFromXMinutes(Integer fromXMinutes) {
		this.fromXMinutes = fromXMinutes;
	}

	@Column(name="every_x_minutes")
	public Integer getEveryXMinutes() {
		return this.everyXMinutes;
	}
	public void setEveryXMinutes(Integer everyXMinutes) {
		this.everyXMinutes = everyXMinutes;
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name="directory")
	public String getDirectory() {
		return directory;
	}
	public void setDirectory(String directory) {
		this.directory = directory;
	}

	@Column(name="file_name")
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Column(name="event_type")
	public Integer getEventType() {
		return eventType;
	}
	public void setEventType(Integer eventType) {
		this.eventType = eventType;
	}

	@Column(name="modify_type")
	public Integer getModifyType() {
		return modifyType;
	}
	public void setModifyType(Integer modifyType) {
		this.modifyType = modifyType;
	}

	@Column(name="valid_flg")
	public Boolean getValidFlg() {
		return this.validFlg;
	}
	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}

	@Column(name="reg_date")
	public Long getRegDate() {
		return this.regDate;
	}
	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}

	@Column(name="update_date")
	public Long getUpdateDate() {
		return this.updateDate;
	}
	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}

	@Column(name="reg_user")
	public String getRegUser() {
		return this.regUser;
	}
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	@Column(name="update_user")
	public String getUpdateUser() {
		return this.updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	//bi-directional many-to-one association to JobRuntimeParamEntity
	@OneToMany(mappedBy="jobKickEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<JobRuntimeParamEntity> getJobRuntimeParamEntities() {
		return this.jobRuntimeParamEntities;
	}
	public void setJobRuntimeParamEntities(List<JobRuntimeParamEntity> jobRuntimeParamEntities) {
		if (jobRuntimeParamEntities != null && jobRuntimeParamEntities.size() > 0) {
			Collections.sort(jobRuntimeParamEntities, new Comparator<JobRuntimeParamEntity>() {
				@Override
				public int compare(JobRuntimeParamEntity o1, JobRuntimeParamEntity o2) {
					return o1.getId().getParamId().compareTo(o2.getId().getParamId());
				}
			});
		}
		this.jobRuntimeParamEntities = jobRuntimeParamEntities;
	}

	/**
	 * JobRuntimeParamEntity削除<BR>
	 * 
	 * 指定されたPK以外の子Entityを削除する。
	 * 
	 */
	public void deleteJobRuntimeParamEntities(List<JobRuntimeParamEntityPK> notDelPkList) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<JobRuntimeParamEntity> list = this.getJobRuntimeParamEntities();
		Iterator<JobRuntimeParamEntity> iter = list.iterator();
		while(iter.hasNext()) {
			JobRuntimeParamEntity entity = iter.next();
			if (!notDelPkList.contains(entity.getId())) {
				iter.remove();
				em.remove(entity);
			}
		}
	}
}