/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityExistsException;
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
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.NotifyDuplicate;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;

@XmlType(namespace = "http://infra.ws.clustercontrol.com")
@Entity
@Table(name="cc_infra_management_info", schema="setting")
@HinemosObjectPrivilege(objectType=HinemosModuleConstant.INFRA, isModifyCheck=true)
@AttributeOverride(name="objectId", column=@Column(name="management_id", insertable=false, updatable=false))
@Cacheable(true)
public class InfraManagementInfo extends ObjectPrivilegeTargetInfo {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String managementId;
	private String name;
	private String description;
	private String facilityId;
	private String notifyGroupId;
	private Boolean validFlg;
	
	private Integer startPriority;
	private Integer normalPriorityRun;
	private Integer abnormalPriorityRun;
	private Integer normalPriorityCheck;
	private Integer abnormalPriorityCheck;
	
	private List<InfraModuleInfo<?>> infraModuleInfoEntities = new ArrayList<>();
	
	private List<InfraManagementParamInfo> infraManagementParamInfoEntities = new ArrayList<>();

	private Long regDate;
	private String regUser;
	private Long updateDate;
	private String updateUser;
	
	private List<NotifyRelationInfo> notifyRelationList;
	
	private String scope;
	
	public InfraManagementInfo(String managementId) {
		this.setManagementId(managementId);
	}

	public InfraManagementInfo() {
	}
	
	@Id
	@Column(name="management_id")
	public String getManagementId() {
		return managementId;
	}
	public void setManagementId(String managementId) {
		this.managementId = managementId;
		this.setObjectId(this.managementId);
	}
	
	@Column(name="name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	
	@Column(name="start_priority")
	public Integer getStartPriority() {
		return startPriority;
	}
	public void setStartPriority(Integer startPriority) {
		this.startPriority = startPriority;
	}
	
	@Column(name="normal_priority_run")
	public Integer getNormalPriorityRun() {
		return normalPriorityRun;
	}
	public void setNormalPriorityRun(Integer normalPriorityRun) {
		this.normalPriorityRun = normalPriorityRun;
	}
	
	@Column(name="abnormal_priority_run")
	public Integer getAbnormalPriorityRun() {
		return abnormalPriorityRun;
	}
	public void setAbnormalPriorityRun(Integer abnormalPriorityRun) {
		this.abnormalPriorityRun = abnormalPriorityRun;
	}
	
	@Column(name="normal_priority_check")
	public Integer getNormalPriorityCheck() {
		return normalPriorityCheck;
	}
	public void setNormalPriorityCheck(Integer normalPriorityCheck) {
		this.normalPriorityCheck = normalPriorityCheck;
	}
	
	@Column(name="abnormal_priority_check")
	public Integer getAbnormalPriorityCheck() {
		return abnormalPriorityCheck;
	}
	public void setAbnormalPriorityCheck(Integer abnormalPriorityCheck) {
		this.abnormalPriorityCheck = abnormalPriorityCheck;
	}

	@OneToMany(mappedBy="infraManagementInfo", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<InfraManagementParamInfo> getInfraManagementParamList() {
		return this.infraManagementParamInfoEntities;
	}
	public void setInfraManagementParamList(List<InfraManagementParamInfo> infraManagementParamInfoEntities) {
		this.infraManagementParamInfoEntities = infraManagementParamInfoEntities;
	}

	@OneToMany(mappedBy="infraManagementInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<InfraModuleInfo<?>> getModuleList() {
		return this.infraModuleInfoEntities;
	}
	public void setModuleList(List<InfraModuleInfo<?>> infraModuleInfoEntities) {
		if (infraModuleInfoEntities != null && infraModuleInfoEntities.size() > 0) {
			Collections.sort(infraModuleInfoEntities, new Comparator<InfraModuleInfo<?>>() {
				@Override
				public int compare(InfraModuleInfo<?> o1, InfraModuleInfo<?> o2) {
					return o1.getOrderNo().compareTo(o2.getOrderNo());
				}
			});
		}
		this.infraModuleInfoEntities = infraModuleInfoEntities;
	}
	
	@Transient
	public List<NotifyRelationInfo> getNotifyRelationList() {
		return this.notifyRelationList;
	}
	public void setNotifyRelationList(List<NotifyRelationInfo> notifyRelationList) {
		this.notifyRelationList = notifyRelationList;
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

	/**
	 * MonitorNumericValueInfoEntity削除<BR>
	 * 
	 * 指定されたPK以外の子Entityを削除する。
	 * 
	 */
	public void deleteInfraManagementParamList(List<InfraManagementParamInfoPK> notDelPkList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<InfraManagementParamInfo> list = this.getInfraManagementParamList();
			Iterator<InfraManagementParamInfo> iter = list.iterator();
			while(iter.hasNext()) {
				InfraManagementParamInfo entity = iter.next();
				if (!notDelPkList.contains(entity.getId())) {
					iter.remove();
					em.remove(entity);
				}
			}
		}
	}

	public void persistSelf(HinemosEntityManager em) throws HinemosUnknown, NotifyDuplicate, InvalidRole, InvalidSetting, EntityExistsException {
		em.persist(this);
		for (InfraModuleInfo<?> module: getModuleList()) {
			module.onPersist(em);
		}
	}

	public void removeSelf(HinemosEntityManager em) throws InvalidRole, HinemosUnknown {
		for (InfraModuleInfo<?> module: getModuleList()) {
			module.onRemove(em);
		}
		
		em.remove(this);
	}
	
	@Transient
	public String getScope() {
		if (scope == null)
			try {
				scope = new RepositoryControllerBean().getFacilityPath(getFacilityId(), null);
			} catch (HinemosUnknown e) {
				Logger.getLogger(this.getClass()).debug(e.getMessage(), e);
			}
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}
}
