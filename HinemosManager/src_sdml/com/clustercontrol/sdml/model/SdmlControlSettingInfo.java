/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.model;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * The persistent class for the cc_sdml_control_setting_info database table.
 *
 */
@Entity
@Table(name="cc_sdml_control_setting_info", schema="setting")
@Cacheable(true)
@HinemosObjectPrivilege( 
		objectType=HinemosModuleConstant.SDML_CONTROL,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="application_id", insertable=false, updatable=false))
public class SdmlControlSettingInfo extends ObjectPrivilegeTargetInfo {
	private static final long serialVersionUID = 1L;

	private String applicationId;
	private String description;
	private String facilityId;
	private String controlLogDirectory;
	private String controlLogFilename;
	private Boolean controlLogCollectFlg;
	private String notifyGroupId;
	private String application;
	private Boolean validFlg;
	private Boolean autoMonitorDeleteFlg;
	private String autoMonitorCalendarId;
	private String autoMonitorCommonNotifyGroupId;
	private Integer earlyStopThresholdSecond;
	private Integer earlyStopNotifyPriority;
	private Integer autoCreateSuccessPriority;
	private Integer autoEnableSuccessPriority;
	private Integer autoDisableSuccessPriority;
	private Integer autoUpdateSuccessPriority;
	private Integer autoControlFailedPriority;
	private String regUser;
	private Long regDate;
	private String updateUser;
	private Long updateDate;
	private String version;

	private List<SdmlMonitorNotifyRelation> sdmlMonitorNotifyRelationList;

	private List<NotifyRelationInfo> notifyRelationList;
	private List<NotifyRelationInfo> autoMonitorCommonNotifyRelationList;
	private String scope;

	public SdmlControlSettingInfo() {
	}

	public SdmlControlSettingInfo(String applicationId) {
		this.setApplicationId(applicationId);
	}

	@Id
	@Column(name="application_id")
	public String getApplicationId() {
		return applicationId;
	}
	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	@Column(name="description")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name="control_log_directory")
	public String getControlLogDirectory() {
		return controlLogDirectory;
	}
	public void setControlLogDirectory(String controlLogDirectory) {
		this.controlLogDirectory = controlLogDirectory;
	}

	@Column(name="control_log_filename")
	public String getControlLogFilename() {
		return controlLogFilename;
	}
	public void setControlLogFilename(String controlLogFilename) {
		this.controlLogFilename = controlLogFilename;
	}

	@Column(name="control_log_collect_flg")
	public Boolean getControlLogCollectFlg() {
		return controlLogCollectFlg;
	}
	public void setControlLogCollectFlg(Boolean controlLogCollectFlg) {
		this.controlLogCollectFlg = controlLogCollectFlg;
	}

	@Column(name="notify_group_id")
	public String getNotifyGroupId() {
		return notifyGroupId;
	}
	public void setNotifyGroupId(String notifyGroupId) {
		this.notifyGroupId = notifyGroupId;
	}

	@Column(name="application")
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}

	@Column(name="valid_flg")
	public Boolean getValidFlg() {
		return validFlg;
	}
	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}

	@Column(name="auto_monitor_delete_flg")
	public Boolean getAutoMonitorDeleteFlg() {
		return autoMonitorDeleteFlg;
	}
	public void setAutoMonitorDeleteFlg(Boolean autoMonitorDeleteFlg) {
		this.autoMonitorDeleteFlg = autoMonitorDeleteFlg;
	}

	@Column(name="auto_monitor_calendar_id")
	public String getAutoMonitorCalendarId() {
		return autoMonitorCalendarId;
	}
	public void setAutoMonitorCalendarId(String autoMonitorCalendarId) {
		this.autoMonitorCalendarId = autoMonitorCalendarId;
	}

	@Column(name="auto_monitor_common_notify_group_id")
	public String getAutoMonitorCommonNotifyGroupId() {
		return autoMonitorCommonNotifyGroupId;
	}
	public void setAutoMonitorCommonNotifyGroupId(String autoMonitorCommonNotifyGroupId) {
		this.autoMonitorCommonNotifyGroupId = autoMonitorCommonNotifyGroupId;
	}

	@Column(name="early_stop_threshold_second")
	public Integer getEarlyStopThresholdSecond() {
		return earlyStopThresholdSecond;
	}
	public void setEarlyStopThresholdSecond(Integer earlyStopThresholdSecond) {
		this.earlyStopThresholdSecond = earlyStopThresholdSecond;
	}

	@Column(name="early_stop_notify_priority")
	public Integer getEarlyStopNotifyPriority() {
		return earlyStopNotifyPriority;
	}
	public void setEarlyStopNotifyPriority(Integer earlyStopNotifyPriority) {
		this.earlyStopNotifyPriority = earlyStopNotifyPriority;
	}

	@Column(name="auto_create_success_priority")
	public Integer getAutoCreateSuccessPriority() {
		return autoCreateSuccessPriority;
	}
	public void setAutoCreateSuccessPriority(Integer autoCreateSuccessPriority) {
		this.autoCreateSuccessPriority = autoCreateSuccessPriority;
	}

	@Column(name="auto_enable_success_priority")
	public Integer getAutoEnableSuccessPriority() {
		return autoEnableSuccessPriority;
	}
	public void setAutoEnableSuccessPriority(Integer autoEnableSuccessPriority) {
		this.autoEnableSuccessPriority = autoEnableSuccessPriority;
	}

	@Column(name="auto_disable_success_priority")
	public Integer getAutoDisableSuccessPriority() {
		return autoDisableSuccessPriority;
	}
	public void setAutoDisableSuccessPriority(Integer autoDisableSuccessPriority) {
		this.autoDisableSuccessPriority = autoDisableSuccessPriority;
	}

	@Column(name="auto_update_success_priority")
	public Integer getAutoUpdateSuccessPriority() {
		return autoUpdateSuccessPriority;
	}
	public void setAutoUpdateSuccessPriority(Integer autoUpdateSuccessPriority) {
		this.autoUpdateSuccessPriority = autoUpdateSuccessPriority;
	}

	@Column(name="auto_control_failed_priority")
	public Integer getAutoControlFailedPriority() {
		return autoControlFailedPriority;
	}
	public void setAutoControlFailedPriority(Integer autoUpdateFailedPriority) {
		this.autoControlFailedPriority = autoUpdateFailedPriority;
	}

	@Column(name="reg_user")
	public String getRegUser() {
		return regUser;
	}
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	@Column(name="reg_date")
	public Long getRegDate() {
		return regDate;
	}
	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}

	@Column(name="update_user")
	public String getUpdateUser() {
		return updateUser;
	}
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	@Column(name="update_date")
	public Long getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}

	@Column(name="version")
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}

	//bi-directional many-to-one association to SdmlMonitorNotifyRelation
	@OneToMany(mappedBy="sdmlControlSettingInfo", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<SdmlMonitorNotifyRelation> getSdmlMonitorNotifyRelationList() {
		return sdmlMonitorNotifyRelationList;
	}
	public void setSdmlMonitorNotifyRelationList(List<SdmlMonitorNotifyRelation> sdmlMonitorNotifyRelationList) {
		this.sdmlMonitorNotifyRelationList = sdmlMonitorNotifyRelationList;
	}

	/**
	 * SdmlMonitorNotifyRelation削除<BR>
	 * 
	 * 指定されたPK以外の子Entityを削除する。
	 * 
	 */
	public void deleteSdmlMonitorNotifyRelationList(List<SdmlMonitorNotifyRelationPK> notDelPkList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<SdmlMonitorNotifyRelation> list = this.getSdmlMonitorNotifyRelationList();
			Iterator<SdmlMonitorNotifyRelation> iter = list.iterator();
			while(iter.hasNext()) {
				SdmlMonitorNotifyRelation entity = iter.next();
				if (!notDelPkList.contains(entity.getId())) {
					iter.remove();
					em.remove(entity);
				}
			}
		}
	}

	@Transient
	public List<NotifyRelationInfo> getNotifyRelationList() {
		return this.notifyRelationList;
	}
	public void setNotifyRelationList(List<NotifyRelationInfo> notifyRelationList) {
		this.notifyRelationList = notifyRelationList;
	}

	@Transient
	public List<NotifyRelationInfo> getAutoMonitorCommonNotifyRelationList() {
		return autoMonitorCommonNotifyRelationList;
	}
	public void setAutoMonitorCommonNotifyRelationList(List<NotifyRelationInfo> autoMonitorCommonNotifyRelationList) {
		this.autoMonitorCommonNotifyRelationList = autoMonitorCommonNotifyRelationList;
	}

	@Transient
	public String getScope() {
		if (scope == null) {
			try {
				scope = new RepositoryControllerBean().getFacilityPath(getFacilityId(), null);
			} catch (HinemosUnknown e) {
				Logger.getLogger(this.getClass()).debug(e.getMessage(), e);
			}
		}
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
}
