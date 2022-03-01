/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import java.util.ArrayList;
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
import jakarta.persistence.Transient;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosTime;


/**
 * The persistent class for the cc_node_config_setting_info database table.
 *
 * @version 6.2.0
 * @since 6.2.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_node_config_setting_info", schema="setting")
@Cacheable(true)
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.NODE_CONFIG_SETTING,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="setting_id", insertable=false, updatable=false))
public class NodeConfigSettingInfo extends ObjectPrivilegeTargetInfo {
	private static final long serialVersionUID = 1L;
	
	private String settingId = "";
	private String settingName = "";
	private String description = "";
	private String facilityId = "";
	private String scope = "";
	private Integer runInterval = 0;
	private String calendarId = "";
	private String notifyGroupId = "";
	private Boolean validFlg = Boolean.TRUE;
	private Long regDate = HinemosTime.currentTimeMillis();
	private Long updateDate = HinemosTime.currentTimeMillis();
	private String regUser = "";
	private String updateUser = "";
	private List<NotifyRelationInfo> notifyRelationList = new ArrayList<>();
	private List<NodeConfigSettingItemInfo> nodeConfigSettingItemList = new ArrayList<>();
	private List<NodeConfigCustomInfo> nodeConfigCustomList = new ArrayList<>();
	
	public NodeConfigSettingInfo() {
	}

	public NodeConfigSettingInfo(String settingId) {
		this.setSettingId(settingId);
	}
	
	@Id
	@Column(name="setting_id")
	public String getSettingId() {
		return this.settingId;
	}
	public void setSettingId(String settingId) {
		this.settingId = settingId;
		setObjectId(settingId);
	}

	@Column(name="setting_name")
	public String getSettingName() {
		return settingName;
	}
	public void setSettingName(String settingName) {
		this.settingName = settingName;
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
		try {
			scope = new RepositoryControllerBean().getFacilityPath(this.facilityId, null);
		} catch (HinemosUnknown e) {
			Logger.getLogger(this.getClass()).debug(e.getMessage(), e);
		}
	}

	@Column(name="run_interval")
	public Integer getRunInterval() {
		return runInterval;
	}
	public void setRunInterval(Integer runInterval) {
		this.runInterval = runInterval;
	}

	@Column(name="calendar_id")
	public String getCalendarId() {
		return calendarId;
	}
	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	@Column(name="notify_group_id")
	public String getNotifyGroupId() {
		return notifyGroupId;
	}
	public void setNotifyGroupId(String notifyGroupId) {
		this.notifyGroupId = notifyGroupId;
	}

	@Column(name="valid_flg")
	public Boolean getValidFlg() {
		return validFlg;
	}
	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}

	@Column(name="reg_date")
	public Long getRegDate() {
		return regDate;
	}
	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}

	@Column(name="update_date")
	public Long getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}

	@Column(name="reg_user")
	public String getRegUser() {
		return regUser;
	}
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	@Column(name="update_user")
	public String getUpdateUser() {
		return updateUser;
	}
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	/**
	 * 通知情報リストを取得します。
	 * 
	 * @return 通知情報リスト
	 */
	@Transient
	public List<NotifyRelationInfo> getNotifyRelationList(){
		return this.notifyRelationList;
	}

	@Transient
	public String getScope() {
		return this.scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	/**
	 * 通知情報リストを設定します。
	 * 
	 * @param notifyRelationList 通知情報リスト
	 */
	public void setNotifyRelationList(List<NotifyRelationInfo> notifyRelationList){
		this.notifyRelationList = notifyRelationList;
	}

	//bi-directional many-to-one association to NodeConfigSettingItemInfo
	@OneToMany(mappedBy="nodeConfigSettingInfo", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<NodeConfigSettingItemInfo> getNodeConfigSettingItemList() {
		return this.nodeConfigSettingItemList;
	}

	public void setNodeConfigSettingItemList(List<NodeConfigSettingItemInfo> nodeConfigSettingItemList) {
		this.nodeConfigSettingItemList = nodeConfigSettingItemList;
	}

	//bi-directional many-to-one association to NodeConfigCustomInfo
	@OneToMany(mappedBy="nodeConfigSettingInfo", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<NodeConfigCustomInfo> getNodeConfigCustomList() {
		return this.nodeConfigCustomList;
	}

	public void setNodeConfigCustomList(List<NodeConfigCustomInfo> nodeConfigCustomList) {
		this.nodeConfigCustomList = nodeConfigCustomList;
	}

	/**
	 * NodeConfigSettingItemList削除<BR>
	 * 
	 * 指定されたPK以外の子Entityを削除する。
	 * 
	 */
	public void deleteNodeConfigSettingItemList(List<NodeConfigSettingItemInfoPK> notDelPkList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeConfigSettingItemInfo> list = this.getNodeConfigSettingItemList();
			Iterator<NodeConfigSettingItemInfo> iter = list.iterator();
			while(iter.hasNext()) {
				NodeConfigSettingItemInfo entity = iter.next();
				if (!notDelPkList.contains(entity.getId())) {
					iter.remove();
					em.remove(entity);
				}
			}
		}
	}	

	/**
	 * NodeConfigSettingCustomList削除<BR>
	 * 
	 * 指定されたPK以外の子Entityを削除する。
	 * 
	 */
	public void deleteNodeConfigSettingCustomList(List<NodeConfigCustomInfoPK> notDelPkList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeConfigCustomInfo> list = this.getNodeConfigCustomList();
			Iterator<NodeConfigCustomInfo> iter = list.iterator();
			while(iter.hasNext()) {
				NodeConfigCustomInfo entity = iter.next();
				if (!notDelPkList.contains(entity.getId())) {
					iter.remove();
					em.remove(entity);
				}
			}
		}
	}
}