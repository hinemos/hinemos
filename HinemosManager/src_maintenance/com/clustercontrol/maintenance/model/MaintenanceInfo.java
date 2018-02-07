/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.bean.Schedule;
import com.clustercontrol.notify.model.NotifyRelationInfo;


/**
 * The persistent class for the cc_maintenance_info database table.
 * 
 */
@XmlType(namespace = "http://maintenance.ws.clustercontrol.com")
@Entity
@Table(name="cc_maintenance_info", schema="setting")
@Cacheable(true)
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.SYSYTEM_MAINTENANCE,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="maintenance_id", insertable=false, updatable=false))
public class MaintenanceInfo extends ObjectPrivilegeTargetInfo {
	private static final long serialVersionUID = 1L;
	private String maintenanceId;
	private String application;
	private Integer dataRetentionPeriod;
	private String description;
	private String notifyGroupId;
	private Long regDate;
	private String regUser;
	private Long updateDate;
	private String updateUser;
	private Boolean validFlg;
	private String calendarId;
	private MaintenanceTypeMst maintenanceTypeMstEntity;

	/** スケジュール */
	private Schedule schedule;
	
	/**通知*/
	private Collection<NotifyRelationInfo> notifyId;

	private String typeId;

	public MaintenanceInfo() {
	}

	public MaintenanceInfo(String maintenanceId,
			MaintenanceTypeMst maintenanceTypeMstEntity) {
		this.setMaintenanceId(maintenanceId);
		this.setObjectId(this.getMaintenanceId());
	}


	@Id
	@Column(name="maintenance_id")
	public String getMaintenanceId() {
		return this.maintenanceId;
	}

	public void setMaintenanceId(String maintenanceId) {
		this.maintenanceId = maintenanceId;
		setObjectId(maintenanceId);
	}


	@Column(name="application")
	public String getApplication() {
		return this.application;
	}

	public void setApplication(String application) {
		this.application = application;
	}


	@Column(name="data_retention_period")
	public Integer getDataRetentionPeriod() {
		return this.dataRetentionPeriod;
	}

	public void setDataRetentionPeriod(Integer dataRetentionPeriod) {
		this.dataRetentionPeriod = dataRetentionPeriod;
	}

	@Column(name="description")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name="notify_group_id")
	public String getNotifyGroupId() {
		return this.notifyGroupId;
	}

	public void setNotifyGroupId(String notifyGroupId) {
		this.notifyGroupId = notifyGroupId;
	}
	
	@Embedded
	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
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


	@Column(name="valid_flg")
	public Boolean getValidFlg() {
		return this.validFlg;
	}

	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}

	@Column(name="calendar_id")
	public String getCalendarId() {
		return this.calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}
	
	@Transient
	public String getTypeId() {
		if (typeId == null && maintenanceTypeMstEntity != null)
			typeId = maintenanceTypeMstEntity.getType_id();
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}
	
	@Transient
	public Collection<NotifyRelationInfo> getNotifyId() {
		return notifyId;
	}

	public void setNotifyId(Collection<NotifyRelationInfo> notifyId) {
		this.notifyId = notifyId;
	}
	
	//bi-directional many-to-one association to MaintenanceTypeMstEntity
	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="type_id")
	public MaintenanceTypeMst getMaintenanceTypeMstEntity() {
		return this.maintenanceTypeMstEntity;
	}

	@Deprecated
	public void setMaintenanceTypeMstEntity(MaintenanceTypeMst maintenanceTypeMstEntity) {
		this.maintenanceTypeMstEntity = maintenanceTypeMstEntity;
	}

	/**
	 * MaintenanceTypeMstEntityオブジェクト参照設定<BR>
	 * 
	 * MaintenanceTypeMstEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToMaintenanceTypeMstEntity(MaintenanceTypeMst maintenanceTypeMstEntity) {
		this.setMaintenanceTypeMstEntity(maintenanceTypeMstEntity);
		if (maintenanceTypeMstEntity != null) {
			List<MaintenanceInfo> list = maintenanceTypeMstEntity.getMaintenanceInfoEntities();
			if (list == null) {
				list = new ArrayList<MaintenanceInfo>();
			} else {
				for(MaintenanceInfo entity : list){
					if (entity.getMaintenanceId().equals(this.maintenanceId)) {
						return;
					}
				}
			}
			list.add(this);
			maintenanceTypeMstEntity.setMaintenanceInfoEntities(list);
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

		// MaintenanceTypeMstEntity
		if (this.maintenanceTypeMstEntity != null) {
			List<MaintenanceInfo> list = this.maintenanceTypeMstEntity.getMaintenanceInfoEntities();
			if (list != null) {
				Iterator<MaintenanceInfo> iter = list.iterator();
				while(iter.hasNext()) {
					MaintenanceInfo entity = iter.next();
					if (entity.getMaintenanceId().equals(this.getMaintenanceId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}
}