/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

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
	private Boolean sessionPremakeFlg;
	private Integer sessionPremakeScheduleType;
	private Integer sessionPremakeWeek;
	private Integer sessionPremakeHour;
	private Integer sessionPremakeMinute;
	private Integer sessionPremakeEveryXHour;
	private Long sessionPremakeDate;
	private Long sessionPremakeToDate;
	private Boolean sessionPremakeInternalFlg;
	private String joblinkMessageId;
	private Boolean infoValidFlg;
	private Boolean warnValidFlg;
	private Boolean criticalValidFlg;
	private Boolean unknownValidFlg;
	private Boolean applicationFlg;
	private String application;
	private Boolean monitorDetailIdFlg;
	private String monitorDetailId;
	private Boolean messageFlg;
	private String message;
	private Boolean expFlg;
	private Long joblinkRcvCheckedPosition;
	private Boolean carryOverJudgementFlg;
	private Boolean validFlg;
	private Long regDate;
	private String regUser;
	private Long updateDate;
	private String updateUser;
	private List<JobRuntimeParamEntity> jobRuntimeParamEntities;
	private List<JobLinkJobkickExpInfoEntity> jobLinkJobkickExpInfoEntities;

	@Deprecated
	public JobKickEntity() {
	}

	public JobKickEntity(String jobkickId) {
		this.setJobkickId(jobkickId);
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

	@Column(name="session_premake_flg")
	public Boolean getSessionPremakeFlg() {
		return sessionPremakeFlg;
	}
	public void setSessionPremakeFlg(Boolean sessionPremakeFlg) {
		this.sessionPremakeFlg = sessionPremakeFlg;
	}

	@Column(name="session_premake_schedule_type")
	public Integer getSessionPremakeScheduleType() {
		return sessionPremakeScheduleType;
	}
	public void setSessionPremakeScheduleType(Integer sessionPremakeScheduleType) {
		this.sessionPremakeScheduleType = sessionPremakeScheduleType;
	}

	@Column(name="session_premake_week")
	public Integer getSessionPremakeWeek() {
		return sessionPremakeWeek;
	}
	public void setSessionPremakeWeek(Integer sessionPremakeWeek) {
		this.sessionPremakeWeek = sessionPremakeWeek;
	}

	@Column(name="session_premake_hour")
	public Integer getSessionPremakeHour() {
		return sessionPremakeHour;
	}
	public void setSessionPremakeHour(Integer sessionPremakeHour) {
		this.sessionPremakeHour = sessionPremakeHour;
	}

	@Column(name="session_premake_minute")
	public Integer getSessionPremakeMinute() {
		return sessionPremakeMinute;
	}
	public void setSessionPremakeMinute(Integer sessionPremakeMinute) {
		this.sessionPremakeMinute = sessionPremakeMinute;
	}

	@Column(name="session_premake_every_x_hour")
	public Integer getSessionPremakeEveryXHour() {
		return sessionPremakeEveryXHour;
	}
	public void setSessionPremakeEveryXHour(Integer sessionPremakeEveryXHour) {
		this.sessionPremakeEveryXHour = sessionPremakeEveryXHour;
	}

	@Column(name="session_premake_date")
	public Long getSessionPremakeDate() {
		return sessionPremakeDate;
	}
	public void setSessionPremakeDate(Long sessionPremakeDate) {
		this.sessionPremakeDate = sessionPremakeDate;
	}

	@Column(name="session_premake_to_date")
	public Long getSessionPremakeToDate() {
		return sessionPremakeToDate;
	}
	public void setSessionPremakeToDate(Long sessionPremakeToDate) {
		this.sessionPremakeToDate = sessionPremakeToDate;
	}

	@Column(name="session_premake_internal_flg")
	public Boolean getSessionPremakeInternalFlg() {
		return sessionPremakeInternalFlg;
	}
	public void setSessionPremakeInternalFlg(Boolean sessionPremakeInternalFlg) {
		this.sessionPremakeInternalFlg = sessionPremakeInternalFlg;
	}

	@Column(name="joblink_message_id")
	public String getJoblinkMessageId() {
		return joblinkMessageId;
	}
	public void setJoblinkMessageId(String joblinkMessageId) {
		this.joblinkMessageId = joblinkMessageId;
	}

	@Column(name="info_valid_flg")
	public Boolean getInfoValidFlg() {
		return infoValidFlg;
	}
	public void setInfoValidFlg(Boolean infoValidFlg) {
		this.infoValidFlg = infoValidFlg;
	}

	@Column(name="warn_valid_flg")
	public Boolean getWarnValidFlg() {
		return warnValidFlg;
	}
	public void setWarnValidFlg(Boolean warnValidFlg) {
		this.warnValidFlg = warnValidFlg;
	}

	@Column(name="critical_valid_flg")
	public Boolean getCriticalValidFlg() {
		return criticalValidFlg;
	}
	public void setCriticalValidFlg(Boolean criticalValidFlg) {
		this.criticalValidFlg = criticalValidFlg;
	}

	@Column(name="unknown_valid_flg")
	public Boolean getUnknownValidFlg() {
		return unknownValidFlg;
	}
	public void setUnknownValidFlg(Boolean unknownValidFlg) {
		this.unknownValidFlg = unknownValidFlg;
	}

	@Column(name="application_flg")
	public Boolean getApplicationFlg() {
		return applicationFlg;
	}
	public void setApplicationFlg(Boolean applicationFlg) {
		this.applicationFlg = applicationFlg;
	}

	@Column(name="application")
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}

	@Column(name="monitor_detail_id_flg")
	public Boolean getMonitorDetailIdFlg() {
		return monitorDetailIdFlg;
	}
	public void setMonitorDetailIdFlg(Boolean monitorDetailIdFlg) {
		this.monitorDetailIdFlg = monitorDetailIdFlg;
	}

	@Column(name="monitor_detail_id")
	public String getMonitorDetailId() {
		return monitorDetailId;
	}
	public void setMonitorDetailId(String monitorDetailId) {
		this.monitorDetailId = monitorDetailId;
	}

	@Column(name="message_flg")
	public Boolean getMessageFlg() {
		return messageFlg;
	}
	public void setMessageFlg(Boolean messageFlg) {
		this.messageFlg = messageFlg;
	}

	@Column(name="message")
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

	@Column(name="exp_flg")
	public Boolean getExpFlg() {
		return expFlg;
	}
	public void setExpFlg(Boolean expFlg) {
		this.expFlg = expFlg;
	}

	@Column(name="joblink_rcv_checked_position")
	public Long getJoblinkRcvCheckedPosition() {
		return joblinkRcvCheckedPosition;
	}
	public void setJoblinkRcvCheckedPosition(Long joblinkRcvCheckedPosition) {
		this.joblinkRcvCheckedPosition = joblinkRcvCheckedPosition;
	}

	@Column(name="carry_over_judgement_flg")
	public Boolean getCarryOverJudgementFlg() {
		return carryOverJudgementFlg;
	}
	public void setCarryOverJudgementFlg(Boolean carryOverJudgementFlg) {
		this.carryOverJudgementFlg = carryOverJudgementFlg;
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

	//bi-directional many-to-one association to JobLinkJobkickExpInfoEntity
	@OneToMany(mappedBy="jobKickEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<JobLinkJobkickExpInfoEntity> getJobLinkJobkickExpInfoEntities() {
		return this.jobLinkJobkickExpInfoEntities;
	}

	public void setJobLinkJobkickExpInfoEntities(List<JobLinkJobkickExpInfoEntity> jobLinkJobkickExpInfoEntities) {
		if (jobLinkJobkickExpInfoEntities != null && jobLinkJobkickExpInfoEntities.size() > 0) {
			Collections.sort(jobLinkJobkickExpInfoEntities, new Comparator<JobLinkJobkickExpInfoEntity>() {
				@Override
				public int compare(JobLinkJobkickExpInfoEntity o1, JobLinkJobkickExpInfoEntity o2) {
					return o1.getId().getKey().compareTo(o2.getId().getKey());
				}
			});
		}
		this.jobLinkJobkickExpInfoEntities = jobLinkJobkickExpInfoEntities;
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
	 * JobLinkJobkickExpInfoEntity削除<BR>
	 * 
	 * 指定されたPK以外の子Entityを削除する。
	 * 
	 */
	public void deleteJobLinkJobkickExpInfoEntities(List<JobLinkJobkickExpInfoEntityPK> notDelPkList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<JobLinkJobkickExpInfoEntity> list = this.getJobLinkJobkickExpInfoEntities();
			Iterator<JobLinkJobkickExpInfoEntity> iter = list.iterator();
			while(iter.hasNext()) {
				JobLinkJobkickExpInfoEntity entity = iter.next();
				if (!notDelPkList.contains(entity.getId())) {
					iter.remove();
					em.remove(entity);
				}
			}
		}
	}

	/**
	 * JobRuntimeParamEntity削除<BR>
	 * 
	 * 指定されたPK以外の子Entityを削除する。
	 * 
	 */
	public void deleteJobRuntimeParamEntities(List<JobRuntimeParamEntityPK> notDelPkList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
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
}